package me.pari;

import java.net.Socket;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;

public class TestClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("127.0.0.1", 7777)) {
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.writeBytes("{\"ciao\": \"Ciao\"}");
            output.flush();

        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
