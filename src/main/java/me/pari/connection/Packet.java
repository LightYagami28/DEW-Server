package me.pari.connection;

import com.google.gson.annotations.Expose;

public class Packet {

    @Expose
    private final int id;

    public Packet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
