package me.pari.security;

import java.security.SecureRandom;

public class Token {

    public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.!";
    public static final int EXPIRY = 2*60*60;

    private static final SecureRandom random = new SecureRandom();
    private static final char[] symbols = CHARACTERS.toCharArray();
    private static final char[] buf = new char[128];

    public static String genNext() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}
