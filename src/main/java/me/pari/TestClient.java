package me.pari;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

import java.net.SocketException;

public class TestClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("127.0.0.1", 7777)) {
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            sendJson(output, new JSONObject("{\"method\": \"sendMessage\", \"params\": {\"\": \"\"}}"));

            System.out.println(readJson(input));

            Thread.sleep(5*1000);

            output.writeBytes("{\"method\": \"sendMessage\", \"params\": {\"\": \"\"}}");
            output.flush();

            System.out.println(readJson(input));

        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static JSONObject readJson(DataInputStream input) {
        // Create a buffer
        int c;
        StringBuilder buff = new StringBuilder();

        try {
            do {
                c = input.read();

                // The client disconnected
                if (c == -1)
                    throw new SocketException("Connection closed");

                // Append to the buffer
                buff.append((char) c);
            } while (!Utils.isJson(buff.toString()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new JSONObject(buff.toString());
    }

    public static void sendJson(DataOutputStream output, JSONObject json) throws IOException {
        output.writeBytes(json.toString());
        output.flush();
    }
}
