package me.pari.types;

import com.google.gson.Gson;
import me.pari.Server;
import me.pari.connection.Request;
import me.pari.connection.Response;
import org.hydev.logger.HyLogger;

import java.io.*;
import java.util.List;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Thread {

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Client");

    // Synchronized client list
    private static final List<Client> clients = Collections.synchronizedList(new ArrayList<>());

    // Current client is connected
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // Current client socket info
    private final Socket socket;
    private final DataOutputStream output;
    private final DataInputStream input;
    private String authToken;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        synchronized (this) {
            clients.add(this);
            isConnected.set(true);
        }
    }

    @Override
    public void run() {
        Server server = Server.getInstance();
        while (isConnected.get()) {
            try {
                // Read bytes from the client
                byte[] bytes = input.readAllBytes();

                // The client disconnected...
                if (bytes.length == 0) {
                    LOGGER.log("Client disconnected: " + socket.getRemoteSocketAddress());
                    break;
                }

                // Serialize to a readable packet
                Request p = new Gson().fromJson(new String(bytes), Request.class);

                // Handle the packet by the server
                server.handle(this, p);

            } catch (IOException e) {
                LOGGER.log("Error reading bytes: " + e.getMessage());
            }
        }

        // Close client
        close();
    }

    public synchronized void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            LOGGER.log("Error closing client: " + ex.getMessage());
        } finally {
            isConnected.set(false);
            clients.remove(this);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    // Communication methods

    public void sendMessage(String text) {
        // sendData(Packet(text));
    }

    public void sendResponse(int id, int status, String desc) {
        sendResponse(new Response(id, status, desc));
    }

    public void sendResponse(Response r) {

    }

    // Static methods

    public static synchronized List<Client> getClients() {
        return clients;
    }

    public static void sendMessageBroadcast(String text) {
        for (Client c: getClients())
            c.sendMessage(text);
    }
}
