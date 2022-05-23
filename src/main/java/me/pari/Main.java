package me.pari;

import org.hydev.logger.HyLogger;
import sun.misc.Signal;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;


public class Main {

    /*
    *
    * Connection:
    *   Client -> Connection (210) -> Server
    *   Server -> Ok (210) -> Client
    *
    * Auth:
    *   Client -> authUser (211) -> Server
    *   Server -> Ok (211) -> Client
    *
    * Message:
    *   Client -> sendMessage (212) -> Server
    *   Server -> Ok (212) -> Client
    *   Server -> message -> BroadCast
    *
    * */

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Main");

    public static void main(String[] args) {
        // Create server
        Server s = Server.getInstance();

        // Run server
        s.start();

        // Server started
        onStarted();

        // Handle server stop command (console)
        Signal.handle(new Signal("INT"), signal -> s.stopServer());

        // Waiting optimizing...
        while (s.isServerRunning())
            Thread.onSpinWait();

        // Server stopped
        onStopped();
    }

    public static void onStarted() {
        if (!Files.exists(Path.of(Server.DATABASE_NAME)))
            Storage.getInstance().setup();
        else
            Storage.getInstance().connect();
        LOGGER.getFancy().gradient("Server started.", new Color(255, 140, 0), new Color(255, 0, 128));
    }

    public static void onStopped() {
        LOGGER.getFancy().gradient("Server stopped.", new Color(255, 0, 128), new Color(255, 140, 0));
        try {
            Client.sendMessageBroadcast(0, "Server", "Server closed.");
        } catch (SQLException ex) {
            LOGGER.warning("Error during broadcast of closed server: " + ex.getMessage());
        }

        // Get client list
        List<Client> clients = Client.getClients();

        // Disconnect all clients
        while (clients.size() > 0)
            clients.get(0).close();

        // Close database connection
        Storage.getInstance().close();
    }

}
