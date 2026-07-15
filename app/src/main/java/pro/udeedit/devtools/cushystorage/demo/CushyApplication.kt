package pro.udeedit.devtools.cushystorage.demo

import android.app.Application
import pro.udeedit.devtools.cushystorage.CushyStorage
import pro.udeedit.devtools.cushystorage.CushyConfig

/**
 * Custom [Application] class for the CushyStorage demo project.
 *
 * This class serves as the central initialization point for the library.
 * Initializing [CushyStorage] here ensures that all storage layers (Simple, Reactive,
 * and Secure) are prepared and ready for use before any other app components
 * (like Activities or Services) are started.
 */
class CushyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * Initialize CushyStorage with default settings.
         * This sets up Simple, Reactive, and Secure (AES-GCM) storage layers.
         */
        CushyStorage.init(this)

        // OR: Initialize with custom security settings if needed
        /*
        val customConfig = CushyConfig(
            tagSizeBits = 128,
            keySize = 256
        )
        CushyStorage.init(this, customConfig)
        */
    }
}
