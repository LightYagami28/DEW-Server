package me.pari;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.pari.types.tcp.Message;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.hydev.logger.HyLogger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Thread {

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Client");

    // Synchronized client list
    private static final List<Client> clients = Collections.synchronizedList(new ArrayList<>());

    // Json Builder
    public static final Gson json = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    // Client status
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // Data streams
    private final Socket socket;
    private final DataOutputStream output;
    private final DataInputStream input;

    // Cached connection info
    private String authToken;
    private String username;
    private Integer userId;

    /*
    * Client builder, private
    */
    private Client(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        synchronized (this) {
            clients.add(this);
            isConnected.set(true);
        }
    }

    public static void startNew(Socket socket) throws IOException {
        new Client(socket).start();
    }

    /*
    * Yea
    * */

    @Override
    public void run() {
        Server server = Server.getInstance();
        int c;

        while (isConnected.get()) {
            try {
                // Create a buffer
                StringBuilder buff = new StringBuilder();

                do {
                    c = input.read();

                    // The client disconnected
                    if (c == -1)
                        throw new SocketException("Connection closed");

                    // Append to the buffer
                    buff.append((char) c);

                } while (!Utils.isJson(buff.toString()));

                // Serialize to a readable packet
                Request p = json.fromJson(new String(buff), Request.class);
                p.setClient(this);

                // Handle the packet by the server
                server.handle(p);

            } catch (JsonSyntaxException ex) {
                LOGGER.log("Bad Packet sent by " + this.socket.getRemoteSocketAddress());

            } catch (SocketException ex) {
                LOGGER.log("Client disconnected: " + socket.getRemoteSocketAddress() + " due to: " + ex.getMessage());
                break;

            } catch (IOException ex) {
                LOGGER.log("Error reading bytes: " + ex.getMessage());
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

    public boolean isTokenValid() {
        if (authToken == null)
            return false;
        try {
            return !Storage.getInstance().isTokenExpired(authToken);
        } catch (SQLException ex) {
            LOGGER.error("SQL Exception in isTokenValid: " + ex.getMessage());
            return false;
        }
    }

    // Cache editor

    public synchronized void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized String getUsername() {
        return username;
    }

    public synchronized void setUserId(Integer userId) {
        this.userId = userId;
    }

    public synchronized Integer getUserId() {
        return userId;
    }

    public synchronized void resetInfo() {
        setUserId(null);
        setUsername(null);
        setAuthToken(null);
    }

    // Communication methods

    public void sendMessage(Message msg) throws IOException {
        this.output.writeBytes(json.toJson(msg));
        this.output.flush();
    }

    public void sendResponse(int id, int status, String desc) throws IOException {
        sendResponse(new Response(id, status, desc));
    }

    public void sendResponse(Response r) throws IOException {
        this.output.writeBytes(json.toJson(r));
        this.output.flush();
    }

    public static synchronized List<Client> getClients() {
        return clients;
    }

    public void sendMessageBroadcast(String text) throws SQLException {
        int msgId = Storage.getInstance().addMessage(userId, text);
        Message msg = new Message(msgId, userId, username, text);

        for (Client c: getClients())
            try {
                if (c.isTokenValid())
                    c.sendMessage(msg);
            } catch (IOException ignored) {

            }
    }
}
