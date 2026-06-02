package fr.lgdev.admindesk.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Small hashing helper. Used to build content-based cache keys: hashing the
 * description means an edited demande naturally produces a different key, so a
 * stale cached result is never returned.
 */
public final class Hash {

    private Hash() {
    }

    public static String sha256(String input) {
        if (input == null) {
            return "null";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK; this branch is effectively unreachable.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
