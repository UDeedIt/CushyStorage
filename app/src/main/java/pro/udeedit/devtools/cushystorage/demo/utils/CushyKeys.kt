package pro.udeedit.devtools.cushystorage.demo.utils

/**
 * Centralized keys for the CushyStorage Demo.
 *
 * Extracting keys to a single object prevents "Magic Strings"
 * and reduces the risk of typos throughout the application.
 */
object CushyKeys {
    // Simple Storage Keys
    const val KEY_USERNAME = "username"
    const val KEY_DARK_THEME = "dark_theme"
    const val KEY_USER_AGE = "user_age"

    // Reactive Storage Keys
    const val KEY_COUNTER = "counter"

    // Secure Storage Keys
    const val KEY_SECURE_TOKEN = "token"
    const val KEY_SECURE_ALIAS = "secret_alias"
}
