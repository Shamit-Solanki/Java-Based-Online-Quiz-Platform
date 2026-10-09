package com.quiz.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2 (HMAC-SHA256) and a random salt per password.
 * Stored format: {@code pbkdf2$<iterations>$<salt-base64>$<hash-base64>}.
 * Plain-text passwords are never stored or compared.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 65_536;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return format(ITERATIONS, salt, derive(password, salt, ITERATIONS));
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            // constant-time comparison avoids timing attacks
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available on this JVM", e);
        }
    }

    private static String format(int iterations, byte[] salt, byte[] hash) {
        Base64.Encoder enc = Base64.getEncoder();
        return "pbkdf2$" + iterations + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
    }

    /** Small helper used to generate hashes for database/sample-data.sql. */
    public static void main(String[] args) {
        for (String pw : args) {
            System.out.println(pw + " -> " + hash(pw));
        }
    }
}
