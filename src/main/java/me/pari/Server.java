package me.pari;

import me.pari.connection.Request;
import me.pari.methods.*;
import org.hydev.logger.HyLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class Server extends Thread {

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Server");

    // Some settings
    public static final int PORT = 7777;
    public static final int MAX_CLIENTS = 200;
    public static final String DATABASE_NAME = "database.db";

    // Singleton
    private static Server INSTANCE;

    // Methods
    public static HashMap<String, Class<? extends Method>> methods = new HashMap<>();

    // Server running atomic
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    // Server socket
    private ServerSocket server;

    public Server() {

        // Auth methods
        methods.put("CreateUser".toLowerCase(), CreateUser.class);
        methods.put("AuthUser".toLowerCase(), AuthUser.class);

        // Send message to server
        methods.put("SendMessage".toLowerCase(), SendMessage.class);

        // Get server info
        methods.put("GetMessages".toLowerCase(), GetMessages.class);
        methods.put("GetUsers".toLowerCase(), GetUsers.class);

        // Ping server connection
        methods.put("Ping".toLowerCase(), Ping.class);
    }

    public static Server getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Server();
        return INSTANCE;
    }

    @Override
    public void run() {
        try (ServerSocket s = new ServerSocket()) {

            // Bind the server
            s.bind(new InetSocketAddress(PORT));
            this.server = s;

            // Accept new connections
            while (isRunning.get()) {
                try {

                    // Max clients reached (improve performance)
                    if (Client.getClients().size() >= MAX_CLIENTS) {
                        Thread.onSpinWait();
                        continue;
                    }

                    // Wrap connections into Thread Clients
                    Socket clientSocket = s.accept();

                    // New connection
                    LOGGER.log("Client connected: " + clientSocket.getRemoteSocketAddress());

                    // Handle new connection
                    new Client(clientSocket).start();

                } catch (IOException ex) {

                    // Exclude socket closed errors
                    if (!ex.getMessage().equalsIgnoreCase("socket closed")) {
                        LOGGER.warning("Error handling new client: " + ex.getMessage());
                    }
                }
            }

        } catch (IOException ex) {
            LOGGER.error("Error during server running: " + ex.getMessage());
        }
    }

    public void handle(Request r) throws IOException {
        LOGGER.log("Handled: " + Client.json.toJson(r));

        // Get method
        String method = r.getMethod();

        // Packet method is empty
        if (method == null) {
            r.sendResponse(400, "BadRequest");
            return;
        }

        // Get class method
        Method t;
        try {
            t = methods.get(method.toLowerCase()).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return;
        }

        // Need to be authorized and Client is not authorized
        if (t.isNeededAuth() && !r.getClient().validateToken()) {
            r.sendResponse(401, "Unauthorized");
            return;
        }

        // Execute the action and respond to the client
        r.sendResponse(t.execute(r));
    }

    public void stopServer() {
        isRunning.set(false);
        try {server.close();} catch (IOException ignored) {}
    }

    public boolean isServerRunning() {
        return isRunning.get();
    }

}
