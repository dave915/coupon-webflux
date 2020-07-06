package com.example.coupon.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

@Slf4j
public class EncryptUtils {
    public static String encrypt(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(string.getBytes());

            return byteToHexString(messageDigest.digest());
        } catch (Exception e) {
            log.error("encrypt error", e);
            throw new RuntimeException("dd");
        }
    }

    private static String byteToHexString(byte[] encodedPassword) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : encodedPassword) {
            if ((b & 0xff) < 0x10) {
                stringBuilder.append("0");
            }

            stringBuilder.append(Long.toString(b & 0xff, 16));
        }
        return stringBuilder.toString();
    }
}
