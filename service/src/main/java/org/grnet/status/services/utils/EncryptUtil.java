package org.grnet.status.services.utils;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@ApplicationScoped
public class EncryptUtil {

    /**
     * Encrypts plain text with the given secret key.
     *
     * @param plainText text to encrypt
     * @param secretKey secret key
     * @return Base64 encoded encrypted text
     */
    public String encrypt(String plainText, String secretKey) {
        try {
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);

            SecretKeySpec keySpec = getAesKeyFromPassword(secretKey, salt);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[salt.length + encrypted.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(encrypted, 0, combined, salt.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to encrypt secret", e);
        }
    }

    /**
     * Decrypts text encrypted with {@link #encrypt(String, String)}.
     *
     * @param encryptedText Base64 encoded encrypted text
     * @param secretKey secret key
     * @return decrypted plain text
     */
    public String decrypt(String encryptedText, String secretKey) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            if (combined.length < 33) { // must be at least 33 bytes (32 salt + ≥1 encrypted)
                throw new IllegalArgumentException("Invalid encrypted text: insufficient length (" + combined.length + " bytes)");
            }
            byte[] salt = new byte[32];
            System.arraycopy(combined, 0, salt, 0, 32);
            byte[] encrypted = new byte[combined.length - 32];
            System.arraycopy(combined, 32, encrypted, 0, encrypted.length);

            SecretKeySpec keySpec = getAesKeyFromPassword(secretKey, salt);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to decrypt secret", e);
        }
    }

    private SecretKeySpec getAesKeyFromPassword(String password, byte[] salt) throws Exception {
        int iterationCount = 65536;
        int keyLength = 256;

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}
