package me.pari;

import java.awt.Color;
import org.hydev.logger.HyLogger;
import sun.misc.Signal;


public class Main {

    // Logger
    private static final HyLogger LOGGER = new HyLogger("Main");

    public static void main(String[] args) throws InterruptedException {

        // Create server
        Server s = Server.getInstance();
        s.start();

        // Handle server stop command (console)
        Signal.handle(new Signal("INT"), signal -> s.stopServer());

        // Server started
        onStarted();

        // Waiting...
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
    }

}
