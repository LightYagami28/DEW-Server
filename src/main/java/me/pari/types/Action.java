package me.pari.types;

import java.util.HashMap;

public class Action extends Packet {

    private String action;
    private String auth;
    private HashMap<String, Object> params;

    public Action() {
    }

    public String getAction() {
        return action;
    }

    public String getAuth() {
        return auth;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

}
