package pro.udeedit.devtools.cushystorage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import pro.udeedit.devtools.cushystorage.engine.SecureEngine
import pro.udeedit.devtools.cushystorage.engine.SimpleReactiveEngine

/**
 * The main entry point for the CushyStorage library.
 *
 * Provides a unified and "cushy" API for handling standard [SharedPreferences],
 * reactive non-encrypted data, and secure, encrypted data persistence via Jetpack DataStore.
 */
object CushyStorage {

    private var preferences: SharedPreferences? = null
    private var simpleReactiveEngine: SimpleReactiveEngine? = null
    private var secureEngine: SecureEngine? = null

    /**
     * Checks if the CushyStorage library has been initialized.
     */
    val isInitialized: Boolean
        get() = preferences != null && simpleReactiveEngine != null && secureEngine != null

    /**
     * Initializes the CushyStorage library.
     *
     * This should be called once, typically in your [android.app.Application] class.
     *
     * @param context The context used to initialize storage. Application context is used internally.
     * @param config Optional configuration to customize encryption parameters. If not provided,
     * industry-standard defaults (AES-GCM, 256-bit key) are used.
     */
    fun init(context: Context, config: CushyConfig = CushyConfig()) {
        val appContext = context.applicationContext ?: context

        // Initialize Standard Storage
        if (preferences == null) {
            val name = "${appContext.packageName}_preferences"
            preferences = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        // Initialize Simple Reactive Storage
        if (simpleReactiveEngine == null) {
            simpleReactiveEngine = SimpleReactiveEngine(appContext)
        }

        // Initialize Secure Storage with the provided configuration
        if (secureEngine == null) {
            secureEngine = SecureEngine(appContext, config)
        }
    }

    /**
     * Internal helper to ensure [init] was called before accessing simple storage.
     * @throws IllegalStateException if [init] has not been called.
     */
    private fun getPrefs(): SharedPreferences {
        return preferences ?: throw IllegalStateException(
            "CushyStorage is not initialized. Call CushyStorage.init(context) in your Application class."
        )
    }

    /**
     * Internal helper to ensure [init] was called before accessing reactive storage.
     */
    private fun getSimpleReactive(): SimpleReactiveEngine =
        simpleReactiveEngine ?: throw IllegalStateException("CushyStorage is not initialized.")

    /**
     * Internal helper to ensure [init] was called before accessing secure storage.
     */
    private fun getSecure(): SecureEngine {
        return secureEngine ?: throw IllegalStateException("CushyStorage is not initialized.")
    }

    // --- Standard Storage (Simple) ---

    /**
     * Saves a [String] value to standard storage.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     */
    fun saveString(key: String, value: String) {
        getPrefs().edit { putString(key, value) }
    }

    /**
     * Retrieves a [String] value from standard storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defaultValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or [defaultValue].
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return getPrefs().getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Saves a [Boolean] value to standard storage.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     */
    fun saveBoolean(key: String, value: Boolean) {
        getPrefs().edit { putBoolean(key, value) }
    }

    /**
     * Retrieves a [Boolean] value from standard storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defaultValue Value to return if this preference does not exist.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getPrefs().getBoolean(key, defaultValue)
    }

    /**
     * Saves an [Int] value to standard storage.
     */
    fun saveInt(key: String, value: Int) {
        getPrefs().edit { putInt(key, value) }
    }

    /**
     * Retrieves an [Int] value from standard storage.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getPrefs().getInt(key, defaultValue)
    }

    // --- Reactive Storage (Simple) ---

    /**
     * Saves a [String] value reactively (non-encrypted) via DataStore.
     *
     * @param key The identifier for the data.
     * @param value The string value to store.
     */
    suspend fun saveStringReactive(key: String, value: String) {
        getSimpleReactive().saveString(key, value)
    }

    /**
     * Retrieves a [String] value from reactive storage once, without observing changes.
     *
     * @param key The identifier for the data.
     * @param defaultValue Value to return if the key doesn't exist.
     * @return The current string value.
     */
    suspend fun getStringReactiveOnce(key: String, defaultValue: String = ""): String {
        return getSimpleReactive().getStringOnce(key, defaultValue)
    }

    /**
     * Provides a [Flow] that emits the non-encrypted value whenever it changes.
     *
     * @param key The identifier for the data to observe.
     * @param defaultValue Value to return if the key doesn't exist.
     * @return A [Flow] emitting the current value.
     */
    fun observeString(key: String, defaultValue: String = ""): Flow<String> {
        return getSimpleReactive().observeString(key, defaultValue)
    }

    // --- Secure Storage (Encrypted) ---

    /**
     * Encrypts and saves a [String] value to secure storage.
     *
     * @param key The identifier for the data.
     * @param value The plain-text string to encrypt and store.
     */
    suspend fun saveStringEncrypted(key: String, value: String) {
        getSecure().save(key, value)
    }

    /**
     * Retrieves and decrypts a [String] value from secure storage.
     *
     * @param key The identifier for the data.
     * @return The decrypted plain-text string, or null if the key doesn't exist.
     */
    suspend fun getStringEncrypted(key: String): String? {
        return getSecure().getOnce(key)
    }

    /**
     * Retrieves the raw, encrypted Base64 string from secure storage without decrypting it.
     * Useful for debugging or demonstrating the encryption layer.
     *
     * @param key The identifier for the data.
     * @return The raw Base64 [IV + Ciphertext] string, or null if it doesn't exist.
     */
    suspend fun getRawStringEncrypted(key: String): String? {
        // We bypass the decryption and return the raw string from the engine
        return getSecure().getRawOnce(key)
    }

    /**
     * Provides a [Flow] that emits the decrypted value whenever it changes.
     */
    fun observeStringEncrypted(key: String): Flow<String?> {
        return getSecure().observe(key)
    }

    // --- Utilities ---

    /**
     * Checks whether the standard storage contains a specific key.
     *
     * @param key The name of the preference to check.
     * @return True if the key exists in SharedPreferences.
     */
    fun hasValue(key: String): Boolean = getPrefs().contains(key)

    /**
     * Removes a specific preference from all storage layers (Simple, Reactive, and Secure).
     *
     * @param key The name of the preference to remove.
     */
    suspend fun remove(key: String) {
        // 1. Remove from Simple SharedPreferences (Synchronous)
        getPrefs().edit { remove(key) }

        // 2. Remove from Simple Reactive DataStore (Asynchronous)
        getSimpleReactive().remove(key)

        // 3. Remove from Secure DataStore (Asynchronous)
        getSecure().remove(key)
    }
}
