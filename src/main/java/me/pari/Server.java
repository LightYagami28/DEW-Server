package me.pari;

import com.google.gson.Gson;
import me.pari.connection.Request;
import me.pari.types.Client;
import org.hydev.logger.HyLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends Thread {
    // private static final String[] actions = new String[] {"authUser", "createUser", "sendMessage", "getMessages", "getUsersCount"};

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Server");

    // Some settings
    private static final int PORT = 7777;
    private static final int MAX_CLIENTS = 200;

    // Singleton
    private static Server INSTANCE;

    // Server running atomic
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    // Server main socket
    private ServerSocket server;

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

    public void handle(Client c, Request r) {
        LOGGER.log("Handled: " + new Gson().toJson(r));

        // Get method
        String method = r.getMethod();

        // Packet method is empty
        if (method == null) {
            c.sendResponse(r.getId(), 400, "BadRequest");
            return;
        }

        // Auth method
        if (method.equalsIgnoreCase("authUser")) {
            // TODO: Authenticate current user
            c.setAuthToken("...");
        }

        // Client is not authenticated
        // TODO: Check with database
        if (c.getAuthToken() == null) {
            c.sendResponse(r.getId(), 401, "Unauthorized");
            return;
        }

        // TODO: Other methods

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
