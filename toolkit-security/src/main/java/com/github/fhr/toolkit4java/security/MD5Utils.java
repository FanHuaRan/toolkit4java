package com.github.fhr.toolkit4java.security;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Fan Huaran
 * created on 2019/11/5
 * @description md5 tool
 */
public class MD5Utils {

    /**
     * do md5 for string
     *
     * @param input
     * @return
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] outBytes = md.digest();
            return HexUtils.hexEncode(outBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("not found md5", e);
        }
    }

}
