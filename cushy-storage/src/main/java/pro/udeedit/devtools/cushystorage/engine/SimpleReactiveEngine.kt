package pro.udeedit.devtools.cushystorage.engine

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Global DataStore instance for non-encrypted reactive storage.
 */
private val Context.simpleDataStore by preferencesDataStore("cushy_simple_reactive")

/**
 * Internal engine for non-encrypted, reactive data persistence using Jetpack DataStore.
 */
internal class SimpleReactiveEngine(context: Context) {

    private val appContext = context.applicationContext ?: context

    suspend fun saveString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        appContext.simpleDataStore.edit { it[prefKey] = value }
    }

    suspend fun getStringOnce(key: String, defaultValue: String = ""): String {
        val prefKey = stringPreferencesKey(key)
        return appContext.simpleDataStore.data.first()[prefKey] ?: defaultValue
    }

    fun observeString(key: String, defaultValue: String = ""): Flow<String> {
        val prefKey = stringPreferencesKey(key)
        return appContext.simpleDataStore.data.map { it[prefKey] ?: defaultValue }
    }

    /**
     * Removes a specific key from the simple reactive DataStore.
     *
     * @param key The identifier to remove.
     */
    suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        appContext.simpleDataStore.edit {
            it.remove(prefKey)
        }
    }
}
