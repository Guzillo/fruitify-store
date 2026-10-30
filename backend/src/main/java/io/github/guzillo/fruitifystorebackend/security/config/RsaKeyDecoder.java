package io.github.guzillo.fruitifystorebackend.security.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Decodes RSA keys from base64-encoded PEM/DER material.
 *
 * <p>Accepted inputs, in order of precedence:
 *
 * <ol>
 *   <li>a {@code classpath:} location pointing to a PEM file (local development/tests),
 *   <li>a raw PEM string,
 *   <li>a base64-encoded PEM string,
 *   <li>a base64-encoded DER string.
 * </ol>
 */
final class RsaKeyDecoder {

    private static final String CLASS_PATH_PREFIX = "classpath:";
    private static final String PEM_MARKER = "-----BEGIN";

    private RsaKeyDecoder() {
    }

    static RSAPublicKey decodePublicKey(String material) {
        byte[] der = decodeDer(material, "jwt.public-key");
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException(
                "Could not parse jwt.public-key. Expected a base64-encoded X.509 RSA public key.",
                ex);
        }
    }

    static RSAPrivateKey decodePrivateKey(String material) {
        byte[] der = decodeDer(material, "jwt.private-key");
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException(
                "Could not parse jwt.private-key. Expected a base64-encoded PKCS#8 RSA private key.",
                ex);
        }
    }

    private static byte[] decodeDer(String material, String property) {
        if (!StringUtils.hasText(material)) {
            throw new IllegalStateException(
                property + " must be set. Provide a base64-encoded RSA key through the "
                    + "JWT_PUBLIC_KEY / JWT_PRIVATE_KEY environment variables.");
        }

        String value = material.trim();
        if (value.startsWith(CLASS_PATH_PREFIX)) {
            value = readClassPath(value.substring(CLASS_PATH_PREFIX.length()), property);
        }

        if (value.contains(PEM_MARKER)) {
            return decodeBase64(stripPemArmor(value), property);
        }

        byte[] decoded = decodeBase64(value, property);
        String decodedText = new String(decoded, StandardCharsets.UTF_8);
        if (decodedText.contains(PEM_MARKER)) {
            return decodeBase64(stripPemArmor(decodedText), property);
        }
        return decoded;
    }

    private static String stripPemArmor(String pem) {
        StringBuilder base64 = new StringBuilder();
        for (String line : pem.split("\\R")) {
            if (!line.startsWith("-----")) {
                base64.append(line.trim());
            }
        }
        return base64.toString();
    }

    private static byte[] decodeBase64(String value, String property) {
        try {
            return Base64.getDecoder().decode(value.replaceAll("\\s", ""));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                property + " is not valid base64. Encode the PEM/DER key with "
                    + "`base64 -w0 <key-file>`.",
                ex);
        }
    }

    private static String readClassPath(String location, String property) {
        Resource resource = new ClassPathResource(location);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException(
                "Could not read " + property + " from classpath:" + location, ex);
        }
    }

}
