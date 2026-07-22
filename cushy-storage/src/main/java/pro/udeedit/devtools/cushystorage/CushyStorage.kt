package pro.udeedit.devtools.cushystorage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
     *
     * Note: During IDE Preview mode, this returns true if only the simple layers
     * are ready, as the secure engine is intentionally disabled in non-Android environments.
     */
    val isInitialized: Boolean
        get() = if (isPreview()) {
            preferences != null && simpleReactiveEngine != null
        } else {
            preferences != null && simpleReactiveEngine != null && secureEngine != null
        }

    /**
     * Initializes the CushyStorage library.
     *
     * This should be called once, typically in your [android.app.Application] class.
     *
     * @param context The context used to initialize storage. Application context is used internally.
     * @param config Optional configuration to customize encryption parameters.
     */
    fun init(context: Context, config: CushyConfig = CushyConfig()) {
        val appContext = context.applicationContext

        // Initialize Standard Storage (SharedPreferences works in IDE Previews)
        if (preferences == null) {
            val name = "${appContext.packageName}_preferences"
            preferences = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        // Initialize Simple Reactive Storage (DataStore works in IDE Previews)
        if (simpleReactiveEngine == null) {
            simpleReactiveEngine = SimpleReactiveEngine(appContext)
        }

        /**
         * SECURE INITIALIZATION SAFETY CHECK
         * We skip the SecureEngine initialization if we detect the code is running
         * inside the Android Studio Layout Editor. This prevents a crash caused by
         * the missing "AndroidKeyStore" on desktop JVMs.
         */
        if (!isPreview() && secureEngine == null) {
            secureEngine = SecureEngine(appContext, config)
        }
    }

    /**
     * Internal helper to detect if the code is running inside a non-Android
     * environment (IDE Preview or a local Unit Test).
     */
    private fun isPreview(): Boolean {
        return try {
            val fingerprint = android.os.Build.FINGERPRINT
            val model = android.os.Build.MODEL

            fingerprint.contains("generic") ||
                    model.contains("google_sdk") ||
                    System.getProperty("java.vendor")?.contains("JetBrains") == true ||
                    // This is the key check for JVM Unit Tests
                    // Fix Bug CUSHY-13
                    System.getProperty("java.class.path")?.contains("junit") == true

        } catch (_: Exception) {
            /**
             * If we catch any error trying to read android.os.Build,
             * we are definitely in a standard JVM (Unit Test) environment.
             */
            true
        }
    }


    /**
     * Internal helper for accessing SharedPreferences.
     */
    private fun getPrefs(): SharedPreferences {
        return preferences ?: throw IllegalStateException(
            "CushyStorage is not initialized. Call CushyStorage.init(context) in your Application class."
        )
    }

    /**
     * Internal helper for accessing Reactive Engine.
     */
    private fun getSimpleReactive(): SimpleReactiveEngine =
        simpleReactiveEngine ?: throw IllegalStateException("CushyStorage is not initialized.")

    /**
     * Internal helper for accessing Secure Engine.
     * Returns null during Previews to prevent KeyStore-related crashes.
     */
    private fun getSecure(): SecureEngine? {
        if (secureEngine == null && !isPreview()) {
            throw IllegalStateException("CushyStorage is not initialized.")
        }
        return secureEngine
    }

    // --- Standard Storage (Simple) ---

    /**
     * Saves a [String] value to standard storage.
     */
    fun saveString(key: String, value: String) {
        getPrefs().edit { putString(key, value) }
    }

    /**
     * Retrieves a [String] value from standard storage.
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return getPrefs().getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Saves a [Boolean] value to standard storage.
     */
    fun saveBoolean(key: String, value: Boolean) {
        getPrefs().edit { putBoolean(key, value) }
    }

    /**
     * Retrieves a [Boolean] value from standard storage.
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
     */
    suspend fun saveStringReactive(key: String, value: String) {
        getSimpleReactive().saveString(key, value)
    }

    /**
     * Retrieves a [String] value from reactive storage once.
     */
    suspend fun getStringReactiveOnce(key: String, defaultValue: String = ""): String {
        return getSimpleReactive().getStringOnce(key, defaultValue)
    }

    /**
     * Provides a [Flow] that emits the non-encrypted value whenever it changes.
     */
    fun observeString(key: String, defaultValue: String = ""): Flow<String> {
        return getSimpleReactive().observeString(key, defaultValue)
    }

    // --- Secure Storage (Encrypted) ---

    /**
     * Encrypts and saves a [String] value to secure storage.
     */
    suspend fun saveStringEncrypted(key: String, value: String) {
        getSecure()?.save(key, value)
    }

    /**
     * Retrieves and decrypts a [String] value from secure storage once.
     */
    suspend fun getStringEncrypted(key: String): String? {
        return getSecure()?.getOnce(key)
    }

    /**
     * Retrieves the raw, encrypted Base64 string from secure storage without decrypting it.
     */
    suspend fun getRawStringEncrypted(key: String): String? {
        return getSecure()?.getRawOnce(key)
    }

    /**
     * Provides a [Flow] that emits the decrypted value whenever it changes.
     * Returns an empty flow if called during an IDE Preview.
     */
    fun observeStringEncrypted(key: String): Flow<String?> {
        return getSecure()?.observe(key) ?: flowOf(null)
    }

    // --- Utilities ---

    /**
     * Checks whether the standard storage contains a specific key.
     */
    fun hasValue(key: String): Boolean = getPrefs().contains(key)

    /**
     * Removes a specific preference from all storage layers (Simple, Reactive, and Secure).
     */
    suspend fun remove(key: String) {
        getPrefs().edit { remove(key) }
        getSimpleReactive().remove(key)
        getSecure()?.remove(key)
    }
}
