package com.example.myapplication;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted, one-way password storage, compatible with Android API 24+. */
public final class PasswordHasher {
    private static final String PREFIX = "pbkdf2-sha1$";
    private static final int ITERATIONS = 1_300_000;
    private PasswordHasher() {}

    public static boolean isEncoded(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static String hash(String password) {
        if (password == null) throw new IllegalArgumentException("Password is required");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return PREFIX + ITERATIONS + "$" + hex(salt) + "$" + hex(derive(password, salt, ITERATIONS));
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || !isEncoded(encoded)) return false;
        try {
            String[] parts = encoded.split("\\$", -1);
            if (parts.length != 4) return false;
            int rounds = Integer.parseInt(parts[1]);
            if (rounds != ITERATIONS) return false;
            byte[] salt = unhex(parts[2]);
            byte[] expected = unhex(parts[3]);
            return salt.length == 16 && expected.length == 32
                    && MessageDigest.isEqual(expected, derive(password, salt, rounds));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int rounds) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        return result.toString();
    }

    private static byte[] unhex(String value) {
        if (value.length() % 2 != 0) throw new IllegalArgumentException();
        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int high = Character.digit(value.charAt(i * 2), 16);
            int low = Character.digit(value.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) throw new IllegalArgumentException();
            bytes[i] = (byte) ((high << 4) | low);
        }
        return bytes;
    }
}
