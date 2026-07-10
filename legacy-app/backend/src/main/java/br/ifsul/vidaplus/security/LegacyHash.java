package br.ifsul.vidaplus.security;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * V-02: hashing de senha com MD5, SEM salt.
 * Fraqueza criptografica (A02 / CWE-916). Os grupos devem substituir por
 * Argon2id (ou bcrypt) com salt individual e migrar os hashes no proximo login.
 */
public class LegacyHash {

    public static String md5(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plain.getBytes("UTF-8"));
            String hex = new BigInteger(1, digest).toString(16);
            while (hex.length() < 32) hex = "0" + hex; // padding
            return hex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
