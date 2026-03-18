package com.example.rentalmanager.shared.infrastructure.security;

import com.example.rentalmanager.shared.domain.SsnEncryptionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Encrypts and decrypts SSN values stored at rest using AES-256/CBC.
 *
 * <p>Storage format: base64( IV[16] || ciphertext )
 *
 * <p>For SSN lookups a separate deterministic HMAC-SHA-256 hash is maintained
 * so queries can be performed without decrypting every row.
 */
@Service
public class SsnEncryptionService implements SsnEncryptionPort {

    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec hmacKey;
    private final SecureRandom  secureRandom = new SecureRandom();

    public SsnEncryptionService(
            @Value("${security.ssn-encryption-key:YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0}") String keyBase64) {
        byte[] raw = Base64.getDecoder().decode(keyBase64);
        // Derive a 32-byte AES key and a 32-byte HMAC key from the supplied material
        byte[] aesKey  = Arrays.copyOf(raw, 32);   // zero-pads if raw < 32 bytes
        byte[] hmacRaw = Arrays.copyOfRange(raw, 0, Math.min(raw.length, 32));
        // XOR-shift hmacRaw to create a distinct HMAC key
        byte[] hmacKeyBytes = new byte[32];
        for (int i = 0; i < 32; i++) hmacKeyBytes[i] = (byte) (hmacRaw[i % hmacRaw.length] ^ 0x5C);
        this.encryptionKey = new SecretKeySpec(aesKey, "AES");
        this.hmacKey       = new SecretKeySpec(hmacKeyBytes, "HmacSHA256");
    }

    // ── Encryption ────────────────────────────────────────────────────────────

    public String encrypt(String plainSsn) {
        if (plainSsn == null || plainSsn.isBlank()) return null;
        try {
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            var cipher = buildCipher(Cipher.ENCRYPT_MODE, iv);
            byte[] ciphertext = cipher.doFinal(plainSsn.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[16 + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, 16);
            System.arraycopy(ciphertext, 0, result, 16, ciphertext.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("SSN encryption failed", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) return null;
        try {
            byte[] decoded    = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv         = Arrays.copyOf(decoded, 16);
            byte[] ciphertext = Arrays.copyOfRange(decoded, 16, decoded.length);
            var cipher = buildCipher(Cipher.DECRYPT_MODE, iv);
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("SSN decryption failed", e);
        }
    }

    // ── Lookup hash (HMAC-SHA-256 of normalised SSN) ──────────────────────────

    /** Returns a deterministic hash used for equality-lookup queries. */
    public String computeLookupHash(String plainSsn) {
        if (plainSsn == null || plainSsn.isBlank()) return null;
        String normalised = normalise(plainSsn);
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            return Base64.getEncoder().encodeToString(mac.doFinal(normalised.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("SSN hash computation failed", e);
        }
    }

    // ── Masking ───────────────────────────────────────────────────────────────

    /** Returns {@code "***-**-XXXX"} where XXXX is the last 4 digits of the SSN. */
    public String mask(String plainSsn) {
        if (plainSsn == null || plainSsn.isBlank()) return null;
        String digits = plainSsn.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return "***-**-****";
        return "***-**-" + digits.substring(digits.length() - 4);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cipher buildCipher(int mode, byte[] iv) {
        try {
            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, encryptionKey, new IvParameterSpec(iv));
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                 | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Cipher initialisation failed", e);
        }
    }

    /** Strips dashes and whitespace from an SSN string. */
    private String normalise(String ssn) {
        return ssn.replaceAll("[^0-9]", "");
    }
}
