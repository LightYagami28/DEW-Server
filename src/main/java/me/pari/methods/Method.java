package me.pari.methods;

import me.pari.connection.Request;
import me.pari.connection.Response;

public abstract class Method {

    public Method() {}

    public abstract Response execute(Request r);

    public boolean isNeededAuth() {
        return true;
    }

}
