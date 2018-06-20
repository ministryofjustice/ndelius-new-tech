package helpers;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionTest {

    @Test
    public void testEncryptionAndDecryption() {

        val plainText = "Some Plain Text";
        val secretKey = "ThisIsASecretKey";

        val encrypted = Encryption.encrypt(plainText, secretKey).orElse("");
        val decrypted = Encryption.decrypt(encrypted, secretKey).orElse("");

        assertThat(plainText).isEqualTo(decrypted);
        assertThat(plainText).isNotEqualTo(encrypted);
    }

    @Test
    public void doesNotTryToEncryptNull() {
     assertThat(Encryption.encrypt(null, "ThisIsASecretKey").isPresent()).isFalse();
    }

    @Test
    public void doesNotTryToDecryptNull() {
     assertThat(Encryption.decrypt(null, "ThisIsASecretKey").isPresent()).isFalse();
    }
}
