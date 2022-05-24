package me.pari.types.tcp;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;

public class Message {

    @Expose
    private final int msgId;

    @Expose
    private final int userId;

    @Expose
    private final String username;

    @Expose
    private final String text;

    public Message(
            int msgId,
            int userId,
            @NotNull String username,
            @NotNull String text
    ) {
        this.msgId = msgId;
        this.username = username;
        this.userId = userId;
        this.text = text;
    }

}
