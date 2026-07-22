package pro.udeedit.devtools.cushystorage

import android.app.Application
import android.content.SharedPreferences
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Unit tests to verify library behavior in a standard JVM environment.
 *
 * These tests ensure that the library correctly detects non-Android environments
 * (like JUnit runners on a development machine) and skips hardware-dependent
 * initializations (Android KeyStore) to prevent crashes during testing.
 */
class JvmEnvironmentTest {

    /**
     * Verifies that the library can be initialized in a pure JVM environment
     * without throwing a KeyStoreException.
     *
     * This is critical for D2D support, allowing other developers to use
     * the library in their own unit-tested ViewModels or UseCases.
     */
    @Test
    fun testInitDoesNotCrashOnJVM() {
        // Create the mock objects
        val mockApplication = mock<Application>()
        val mockPrefs = mock<SharedPreferences>()

        // Ensure applicationContext returns the mock itself
        whenever(mockApplication.applicationContext).thenReturn(mockApplication)

        // Ensure getPackageName returns a valid string
        whenever(mockApplication.packageName).thenReturn("pro.udeedit.devtools.cushystorage")

        // Ensure getSharedPreferences returns our mockPrefs instead of null
        whenever(mockApplication.getSharedPreferences(any(), any())).thenReturn(mockPrefs)

        /**
         * Execution: Initializing the library.
         * Now it has a non-null appContext and packageName.
         * The internal 'isPreview' logic will then correctly skip the SecureEngine.
         */
        CushyStorage.init(mockApplication)

        // Assertion
        assertTrue(
            "CushyStorage should report as initialized.",
            CushyStorage.isInitialized
        )
    }

}