package me.pari;

import java.awt.Color;

import me.pari.types.Client;
import org.hydev.logger.HyLogger;
import sun.misc.Signal;


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
        LOGGER.getFancy().gradient("Server started.", new Color(255, 140, 0), new Color(255, 0, 128));
    }

    public static void onStopped() {
        LOGGER.getFancy().gradient("Server stopped.", new Color(255, 0, 128), new Color(255, 140, 0));
        Client.sendMessageBroadcast("Server closed.");
    }

}
