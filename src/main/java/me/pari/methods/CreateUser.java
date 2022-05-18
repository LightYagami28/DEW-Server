package me.pari.methods;

import me.pari.connection.Request;
import me.pari.connection.Response;

import java.util.HashMap;

public class CreateUser extends Method {

    @Override
    public Response execute(Request r) {
        HashMap<String, String> params = r.getParams();


        return null;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
