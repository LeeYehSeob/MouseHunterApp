package com.example.mousehunter;

import android.app.Activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncryption {



    private String pass;
    private String key;

    public PasswordEncryption(String msg, Activity activity){
        try {
            key = activity.getString(R.string.key);
            pass = encryption(msg , key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
    public static String encryption(String pass, String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        pass = key + pass;
        md.update(pass.getBytes());
        return bytesToHex(md.digest());
    }

    private static String bytesToHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (byte b: bytes){
            builder.append(String.format("%02x",b));
        }
        return  builder.toString();
    }
    public String getPass() {
        return pass;
    }
}
