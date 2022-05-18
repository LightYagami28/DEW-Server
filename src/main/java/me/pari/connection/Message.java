package me.pari.connection;

import com.google.gson.annotations.Expose;

public class Message {

    @Expose
    private final int msgId;

    @Expose
    private final String text;

    public Message(int msgId, String text) {
        this.msgId = msgId;
        this.text = text;
    }

}
