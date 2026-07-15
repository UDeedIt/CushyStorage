package pro.udeedit.devtools.cushystorage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive Instrumentation Tests for CushyStorage.
 *
 * These tests run on a physical device or emulator to verify that the library
 * correctly interacts with the Android KeyStore, SharedPreferences, and DataStore.
 */
@RunWith(AndroidJUnit4::class)
class CushyStorageTest {

    // Accessing the target context of the instrumentation runner
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Set up the test environment before each test case.
     * We initialize the library here to ensure every test has a fresh engine instance.
     */
    @Before
    fun setup() {
        CushyStorage.init(context)
    }

    /**
     * Verifies the Standard Storage (Simple) layer.
     * Checks if strings are correctly persisted and retrieved via SharedPreferences.
     */
    @Test
    fun testSimpleStoragePersistence() {
        val key = "user_name"
        val expectedValue = "Alex UDeedIt"

        // Perform the save operation
        CushyStorage.saveString(key, expectedValue)

        // Retrieve the value and verify it matches
        val actualValue = CushyStorage.getString(key)
        assertEquals("The simple storage should return the exact string saved.", expectedValue, actualValue)
    }

    /**
     * Verifies the Reactive Storage (Simple) layer.
     * Uses [runTest] to handle the asynchronous nature of Jetpack DataStore.
     */
    @Test
    fun testReactiveStorageOneShot() = runTest {
        val key = "reactive_counter"
        val value = "42"

        // Save asynchronously to the reactive engine
        CushyStorage.saveStringReactive(key, value)

        // Fetch using the 'Once' variant to verify persistence without a Flow
        val result = CushyStorage.getStringReactiveOnce(key)
        assertEquals("The reactive engine should correctly retrieve the persisted value.", value, result)
    }

    /**
     * Verifies the Secure Storage (Encrypted) layer.
     * This is the most critical test as it verifies the AES-GCM encryption
     * and hardware-backed KeyStore integration.
     */
    @Test
    fun testSecureEncryptionDecryption() = runTest {
        val key = "auth_token"
        val sensitiveData = "super_secret_session_token_123"

        // 1. Encrypt and save
        CushyStorage.saveStringEncrypted(key, sensitiveData)

        // 2. Retrieve the raw Base64 string to prove it's actually scrambled on disk
        val rawEncrypted = CushyStorage.getRawStringEncrypted(key)
        assertNotNull("Raw encrypted string should exist", rawEncrypted)
        assertNotEquals("The raw string must be different from the plain text", sensitiveData, rawEncrypted)

        // 3. Decrypt and verify the original data is recovered
        val decryptedData = CushyStorage.getStringEncrypted(key)
        assertEquals("The decrypted data must match the original plain text.", sensitiveData, decryptedData)
    }

    /**
     * Verifies the library's utility functions.
     * Tests the unified [remove] function across all storage layers.
     */
    @Test
    fun testUnifiedRemoveUtility() = runTest {
        val key = "temporary_key"

        // Arrange: Save data to multiple layers
        CushyStorage.saveString(key, "simple_data")
        CushyStorage.saveStringEncrypted(key, "secure_data")

        // Act: Use the unified remove function
        CushyStorage.remove(key)

        // Assert: Verify existence is gone
        val exists = CushyStorage.hasValue(key)
        val secureValue = CushyStorage.getStringEncrypted(key)

        assertFalse("Key should be removed from Simple storage.", exists)
        assertNull("Key should be removed from Secure storage.", secureValue)
    }

    /**
     * Verifies that the library handles non-existent keys gracefully
     * by returning the provided default values.
     */
    @Test
    fun testDefaultValueHandling() {
        val key = "non_existent_key"
        val default = "DefaultValue"

        val result = CushyStorage.getString(key, default)
        assertEquals("Should return the default value when key is missing.", default, result)
    }
}
