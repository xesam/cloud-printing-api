package com.github.xesam.printing.api.core;

import java.security.MessageDigest;

public final class ApiMd5Signature implements ApiSignature {
    private String MD5(String message) {
        String md5Str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(message.getBytes());
            md5Str = bytes2Hex(md5Bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5Str.toUpperCase();
    }

    private String bytes2Hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        int temp;
        try {
            for (byte aByte : bytes) {
                temp = aByte;
                if (temp < 0) {
                    temp += 256;
                }
                if (temp < 16) {
                    result.append("0");
                }
                result.append(Integer.toHexString(temp));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    public String getSignature(String... contents) {
        return MD5(String.join("", contents)).toUpperCase();
    }
}
