package me.pari.connection;

import java.util.HashMap;

public class Request extends Packet {
    private final String method;
    private final HashMap<String, Object> params;

    /*
    * Client request to the server.
    * */
    public Request(int id, String method, HashMap<String, Object> params) {
        super(id);
        this.method = method;
        this.params = params;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public String getMethod() {
        return method;
    }



}
