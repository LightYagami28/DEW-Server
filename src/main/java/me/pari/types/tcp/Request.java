package me.pari.types.tcp;

import com.google.gson.annotations.Expose;
import me.pari.Client;

import java.io.IOException;
import java.util.HashMap;

public class Request extends Packet {

    @Expose
    private final String method;

    @Expose
    private final HashMap<String, String> params;


    private Client client;

    /*
    * Client request to the server.
    * */
    public Request(Client client, int id, String method, HashMap<String, String> params) {
        super(id);
        this.client = client;
        this.method = method;
        this.params = params;
    }

    public void sendResponse(int status, String desc) throws IOException {
        this.client.sendResponse(this.getId(), status, desc);
    }

    public void sendResponse(Response r) throws IOException {
        this.client.sendResponse(r);
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public String getMethod() {
        return method;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }


}
