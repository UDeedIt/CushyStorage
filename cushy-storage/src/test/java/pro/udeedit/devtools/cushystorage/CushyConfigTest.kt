package pro.udeedit.devtools.cushystorage

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the CushyConfig data class.
 *
 * Verifies that the configuration object correctly maintains
 * the encryption parameters provided by the developer.
 */
class CushyConfigTest {

    @Test
    fun testConfigDefaults() {
        val config = CushyConfig()

        // Verifying that the industry-standard defaults are correctly set
        assertEquals("AES/GCM/NoPadding", config.aesMode)
        assertEquals(12, config.ivSizeBytes)
        assertEquals(128, config.tagSizeBits)
        assertEquals(256, config.keySize)
    }

    @Test
    fun testCustomConfig() {
        // Testing that a developer can actually customize the strength
        val customConfig = CushyConfig(
            keySize = 128,
            ivSizeBytes = 16
        )

        assertEquals(128, customConfig.keySize)
        assertEquals(16, customConfig.ivSizeBytes)
        // Ensure other values remain at their defaults
        assertEquals(128, customConfig.tagSizeBits)
    }
}
