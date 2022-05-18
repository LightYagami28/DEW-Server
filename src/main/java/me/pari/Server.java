package me.pari;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hydev.logger.HyLogger;

import me.pari.methods.*;
import me.pari.types.Storage;
import me.pari.types.Client;
import me.pari.connection.Request;
import me.pari.connection.Response;


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
    }

    public static Server getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Server();
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            // Create server
            server = new ServerSocket();
            server.bind(new InetSocketAddress(PORT));

            // Accept new connections
            while (isServerRunning()) {
                try {

                    // Max clients reached (improve performance)
                    if (Client.getClients().size() >= MAX_CLIENTS) {
                        Thread.onSpinWait();
                        continue;
                    }

                    // Wrap connections into Thread Clients
                    Socket clientSocket = server.accept();

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

        } finally {

            // Close socket no matter what happens
            try {server.close();} catch (IOException ignored) {}
        }
    }

    public void handle(Request r) throws IOException {
        LOGGER.log("Handled: " + Client.json.toJson(r));

        // Get method and client
        String method = r.getMethod();
        Client c = r.getClient();

        // Packet method is empty
        if (method == null) {
            r.sendResponse(400, "BadRequest");
            return;
        }

        // Convert method in lowercase
        method = method.toLowerCase();

        // Get class method
        Method t;
        try {
            t = methods.get(method).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return;
        }

        // Need to be authorized and Client is not authorized
        if (t.isNeededAuth() && !c.validateToken()) {
            r.sendResponse(401, "Unauthorized");
            return;
        }

        // Execute the action
        Response serverResponse = t.execute(r);

        // Respond to the client
        r.sendResponse(serverResponse);
    }

    public void stopServer() {
        isRunning.set(false);
        try {
            server.close();
        } catch (IOException ignored) {

        }
    }

    public boolean isServerRunning() {
        return isRunning.get();
    }

}
