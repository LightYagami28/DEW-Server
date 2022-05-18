package me.pari;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private static final char [] subset = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String generateToken() {
        Random r = ThreadLocalRandom.current();
        char[] buf = new char[128];
        for (int i=0;i<buf.length;i++) {
            int index = r.nextInt(subset.length);
            buf[i] = subset[index];
        }
        return new String(buf);
    }

    public static boolean isJson(String text) {
        try {
            new JSONObject(text);
        } catch (JSONException ex) {
            try {
                new JSONArray(text);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}
