package ru.pnu.sync;

/**
 * Created by ultr0 on 20.05.2016.
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class md5Generate {

    public static String main(String args) {

        String s = args;
        String  res = md5(s);
//        System.out.println(res);
        return res;
    }

    private static String md5(String s) { try {

        // Create MD5 Hash
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
        return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
        return "";

    }


}