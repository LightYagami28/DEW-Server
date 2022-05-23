package me.pari.methods;

import me.pari.Client;
import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.connection.Status;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;

public class GetUsers extends Method {
    @Override
    public Response execute(Request r) {
        Response serverResponse = new Response(r.getId(), Status.OK);

        HashMap<String, String> values = new HashMap<>();

        // Get clients
        List<Client> clients = Client.getClients();

        // Put count
        values.put("count", Integer.toString(clients.size()));

        // Put list
        JSONArray arr = new JSONArray(clients.size());
        for (Client c : clients)
            arr.put(c.getUsername());
        values.put("list", arr.toString());

        serverResponse.setValues(values);

        return serverResponse;
    }
}
