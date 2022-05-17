package me.pari;

import com.google.gson.Gson;
import me.pari.types.Client;
import me.pari.types.Packet;
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

    public void handle(Packet data) {
        LOGGER.log("Handled: " + new Gson().toJson(data));
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
