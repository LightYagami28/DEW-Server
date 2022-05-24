package me.pari.types.tcp;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class Response extends Packet {

    @Expose
    private final int status;

    @Expose
    private final String desc;

    @Expose
    private final HashMap<String, String> values;

    /*
    * Server response to the client request.
    * */
    public Response(int id, int status, String desc) {
        super(id);
        this.status = status;
        this.desc = desc;
        this.values = new HashMap<>();
    }

    public Response(int id, int status) {
        this(id, status, null);
    }

    public void setValue(String key, String value) {
        this.values.put(key, value);
    }
}
