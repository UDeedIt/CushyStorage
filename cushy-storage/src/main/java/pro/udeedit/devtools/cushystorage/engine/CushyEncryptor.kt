package pro.udeedit.devtools.cushystorage.engine

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import pro.udeedit.devtools.cushystorage.CushyConfig
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Internal engine responsible for AES-GCM encryption and decryption.
 *
 * It is dynamically configured via [CushyConfig], allowing
 * for custom encryption modes, tag sizes, and key strengths.
 *
 * @param config The user-provided or default configuration for the encryption process.
 */
internal class CushyEncryptor(private val config: CushyConfig) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "cushy_storage_aes_key"
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * Encrypts a plain-text string based on the provided [CushyConfig].
     *
     * @param plainText The raw string to be encrypted.
     * @return A Base64-encoded string containing both IV and encrypted data.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(config.aesMode)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine IV and Ciphertext using the size defined in config
        val combined = ByteBuffer.allocate(iv.size + encryptedBytes.size)
            .put(iv)
            .put(encryptedBytes)
            .array()

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a Base64-encoded string using parameters defined in [CushyConfig].
     *
     * @param encryptedBase64 The Base64 string containing [IV + Ciphertext].
     * @return The original plain-text string.
     */
    fun decrypt(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)

        // Extract IV based on the configured size
        val iv = combined.copyOfRange(0, config.ivSizeBytes)
        val ciphertext = combined.copyOfRange(config.ivSizeBytes, combined.size)

        val cipher = Cipher.getInstance(config.aesMode)
        val spec = GCMParameterSpec(config.tagSizeBits, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Generates or retrieves a SecretKey from the KeyStore.
     * Uses the [CushyConfig.keySize] to determine the cryptographic strength.
     */
    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(config.keySize) // Dynamically set from config
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
