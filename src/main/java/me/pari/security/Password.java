package me.pari.security;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class Password {

    private static final int ROUNDS = 12;

    public static String hash(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(ROUNDS));
    }

    public static boolean check(String pwd, String hash) {
        return BCrypt.checkpw(pwd, hash);
    }

}
