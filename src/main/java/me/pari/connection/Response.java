package me.pari.connection;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class Response extends Packet {
    @Expose
    private final int status;

    @Expose
    private final String desc;

    @Expose
    private HashMap<String, String> values;

    /*
    * Server response to the client request.
    *
    * */
    public Response(int id, int status, String desc) {
        super(id);
        this.status = status;
        this.desc = desc;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }
}
