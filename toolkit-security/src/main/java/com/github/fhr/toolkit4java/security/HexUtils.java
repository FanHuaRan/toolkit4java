package com.github.fhr.toolkit4java.security;

import java.nio.charset.StandardCharsets;

/**
 * @author Fan Huaran
 * created on 2019/11/5
 * @description 十六进制工具
 */
public class HexUtils {

    public static String hexEncode(byte[] input) {
        StringBuilder builder = new StringBuilder();
        for(byte value : input) {
            String hex = Integer.toHexString(value & 0xFF);
            if(hex.length() < 2){
                builder.append("0");
            }
            builder.append(hex);
        }
        return builder.toString();
    }


    public static byte[] hexEncode4Bytes(byte[] input) {
        return hexEncode(input).getBytes(StandardCharsets.UTF_8);
    }
}
