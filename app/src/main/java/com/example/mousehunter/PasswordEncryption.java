package com.example.mousehunter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncryption {



    private String pass;
    private static  String key = "iotmit";


    public PasswordEncryption(String msg){
        try {
            pass = encryption(msg);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public static String encryption(String msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        msg += key;
        md.update(msg.getBytes());
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
