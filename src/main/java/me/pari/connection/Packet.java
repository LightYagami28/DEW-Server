package me.pari.connection;

public class Packet {

    private final int id;

    public Packet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
