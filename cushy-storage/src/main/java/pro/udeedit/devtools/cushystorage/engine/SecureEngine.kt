package pro.udeedit.devtools.cushystorage.engine

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pro.udeedit.devtools.cushystorage.CushyConfig

/**
 * Extension property to initialize DataStore.
 * Using a specific name to avoid conflicts with other libraries.
 */
private val Context.dataStore by preferencesDataStore("cushy_secure_storage")

/**
 * Internal engine that manages encrypted data persistence using Jetpack DataStore.
 *
 * It coordinates between [CushyEncryptor] for data security and
 * [androidx.datastore.core.DataStore] for thread-safe disk I/O.
 *
 * @param context The application context required for DataStore initialization.
 */
internal class SecureEngine(context: Context, config: CushyConfig) {

    private val appContext = context.applicationContext ?: context
    private val encryptor = CushyEncryptor(config)

    /**
     * Encrypts and saves a [String] value asynchronously.
     *
     * @param key The identifier for the data.
     * @param value The plain-text string to encrypt and store.
     */
    suspend fun save(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        appContext.dataStore.edit { prefs ->
            prefs[prefKey] = encryptor.encrypt(value)
        }
    }

    /**
     * Retrieves an encrypted [String] and decrypts it.
     *
     * @param key The identifier for the data.
     * @return The decrypted plain-text string, or null if the key doesn't exist.
     */
    suspend fun getOnce(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        val encryptedValue = appContext.dataStore.data.first()[prefKey]
        return encryptedValue?.let { encryptor.decrypt(it) }
    }

    /**
     * Fetches the raw string from DataStore without passing it through the decryptor.
     */
    suspend fun getRawOnce(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        return appContext.dataStore.data.first()[prefKey]
    }

    /**
     * Provides a [Flow] that emits the decrypted value whenever it changes.
     *
     * @param key The identifier for the data to observe.
     * @return A [Flow] emitting the decrypted string or null.
     */
    fun observe(key: String): Flow<String?> {
        val prefKey = stringPreferencesKey(key)
        return appContext.dataStore.data.map { prefs ->
            prefs[prefKey]?.let { encryptor.decrypt(it) }
        }
    }

    /**
     * Removes a specific encrypted preference from storage.
     *
     * @param key The identifier for the data to remove.
     */
    suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        appContext.dataStore.edit { it.remove(prefKey) }
    }
}
