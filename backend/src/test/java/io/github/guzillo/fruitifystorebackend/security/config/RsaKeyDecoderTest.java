package io.github.guzillo.fruitifystorebackend.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class RsaKeyDecoderTest {

    @Test
    void decodesBase64EncodedPemKeys() throws Exception {
        KeyPair keyPair = generateKeyPair();

        String encodedPublicKey = base64Pem("PUBLIC KEY", keyPair.getPublic().getEncoded());
        String encodedPrivateKey = base64Pem("PRIVATE KEY", keyPair.getPrivate().getEncoded());

        RSAPublicKey publicKey = RsaKeyDecoder.decodePublicKey(encodedPublicKey);
        RSAPrivateKey privateKey = RsaKeyDecoder.decodePrivateKey(encodedPrivateKey);

        assertThat(publicKey).isEqualTo(keyPair.getPublic());
        assertThat(privateKey).isEqualTo(keyPair.getPrivate());
    }

    @Test
    void decodesBase64EncodedDerKeys() throws Exception {
        KeyPair keyPair = generateKeyPair();

        String encodedPublicKey = encode(keyPair.getPublic().getEncoded());
        String encodedPrivateKey = encode(keyPair.getPrivate().getEncoded());

        assertThat(RsaKeyDecoder.decodePublicKey(encodedPublicKey))
            .isEqualTo(keyPair.getPublic());
        assertThat(RsaKeyDecoder.decodePrivateKey(encodedPrivateKey))
            .isEqualTo(keyPair.getPrivate());
    }

    @Test
    void rejectsBlankKeyMaterial() {
        assertThatThrownBy(() -> RsaKeyDecoder.decodePublicKey(" "))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("jwt.public-key must be set");
    }

    @Test
    void rejectsInvalidBase64() {
        assertThatThrownBy(() -> RsaKeyDecoder.decodePrivateKey("not base64!"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not valid base64");
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String base64Pem(String type, byte[] der) {
        String pem = "-----BEGIN " + type + "-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(der)
            + "\n-----END " + type + "-----\n";
        return encode(pem.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

}
