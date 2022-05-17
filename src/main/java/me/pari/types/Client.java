package me.pari.types;

import com.google.gson.Gson;
import me.pari.Server;
import netscape.javascript.JSObject;
import org.hydev.logger.HyLogger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                System.out.println(Arrays.toString(bytes));

                // The client disconnected...
                if (bytes.length == 0) {
                    LOGGER.log("Client disconnected: " + socket.getRemoteSocketAddress());
                    close();
                    return;
                }

                // Serialize to a readable packet
                Packet p = new Gson().fromJson(new String(bytes), Packet.class);

                // Handle the packet by the server
                server.handle(p);

            } catch (IOException | InterruptedException e) {
                LOGGER.log("Error reading bytes: " + e.getMessage());
            }
        }
    }

    public void sendData(Packet data) throws IOException {
        output.writeBytes(data.toString());
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

    public static synchronized List<Client> getClients() {
        return clients;
    }

}
