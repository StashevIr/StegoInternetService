package com.stego_api.helper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static String getHash(String prop) throws NoSuchAlgorithmException {
        // Get hash of properties
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(prop.getBytes());
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }

    public  static int[] getDecimalHashArray(String hash) {
        //int[] decHash = new int[hash.length()];
        int[] decHash = new int[GeometryHelper.COUNT_OF_EDGE];
        for (int i = 0; i < decHash.length; i++) {
            decHash[i] = Integer.parseInt(String.valueOf(hash.charAt(i)), 16);
        }
        return decHash;
    }

}
