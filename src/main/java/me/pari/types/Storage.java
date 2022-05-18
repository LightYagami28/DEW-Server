package me.pari.types;

import me.pari.Server;
import me.pari.Utils;

public class Storage {

    private final String fileName;

    private static Storage INSTANCE;

    public Storage(final String fileName) {
        this.fileName = fileName;
    }

    public static Storage getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Storage(Server.DATABASE_NAME);
        return INSTANCE;
    }

    public String addUser(String username, String password) {
        return Utils.generateToken();
    }

    public void updateUserToken(int userId, String authToken) {

    }

    public int getUserIdByToken(String authToken) {
        return 1;
    }

    public boolean isTokenExpired(String authToken) {

    }



}
