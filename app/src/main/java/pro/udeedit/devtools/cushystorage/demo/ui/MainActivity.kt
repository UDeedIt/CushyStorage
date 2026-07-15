package pro.udeedit.devtools.cushystorage.demo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pro.udeedit.devtools.cushystorage.CushyStorage
import pro.udeedit.devtools.cushystorage.demo.ui.theme.CushyStorageTheme
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_COUNTER
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_DARK_THEME
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_SECURE_ALIAS
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_SECURE_TOKEN
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_USERNAME
import pro.udeedit.devtools.cushystorage.demo.utils.CushyKeys.KEY_USER_AGE

/**
 * Main Activity for the CushyStorage Demo.
 *
 * This class demonstrates a Developer-to-Developer (D2D) implementation of
 * a unified storage API. It showcases three layers of persistence:
 * 1. Simple (SharedPreferences)
 * 2. Reactive (DataStore Flows)
 * 3. Secure (AES-GCM + KeyStore)
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CushyStorageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CushyDemoScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CushyDemoScreen() {

    // Accessing the current Android context for UI elements like Toasts
    val context = LocalContext.current

    // Providing a Coroutine Scope for asynchronous disk I/O (DataStore calls)
    val scope = rememberCoroutineScope()

    // Managing scroll state for smaller screens or long content
    val scrollState = rememberScrollState()

    // --- DIALOG STATE ---
    // Explicitly reading the value into a local constant for the check
    // Using explicit state object to resolve IDE "unused" warnings
    val showResetDialogState = remember { mutableStateOf(false) }

    // Tracks if the Reset Confirmation Dialog should be visible
//    var showResetDialog by remember { mutableStateOf(false) }

    // --- REACTIVE STORAGE STATES ---

    /**
     * ObserveString provides a Flow from the SimpleReactiveEngine (DataStore).
     * Using collectAsStateWithLifecycle ensures we don't leak resources when the app is in the background.
     */
    val reactiveCounter by CushyStorage.observeString("counter", "0")
        .collectAsStateWithLifecycle(initialValue = "0")

    /**
     * ObserveStringEncrypted demonstrates the "Power Layer":
     * It automatically decrypts the incoming DataStore Flow using the AES-GCM engine.
     */
    val secureReactiveAlias by CushyStorage.observeStringEncrypted(KEY_SECURE_ALIAS)
        .collectAsStateWithLifecycle(initialValue = "No Alias Set")


    // --- UI STATES INITIALIZED FROM STORAGE ---

    // Initial load from Standard SharedPreferences (Simple layer)
    var simpleInput by remember { mutableStateOf(CushyStorage.getString(KEY_USERNAME, "")) }

    // State for the manual secret entry
    var secureInput by remember { mutableStateOf("") }

    // Holds the decrypted result after a manual "Load" action
    var decryptedSecret by remember { mutableStateOf("Click Load") }

    // Holds the raw Base64 [IV + Ciphertext] to show what is actually stored on disk
    var rawEncryptedValue by remember { mutableStateOf("No data saved yet") }


    // --- OTHER SIMPLE TYPES ---

    // Demonstrating Boolean support (e.g., for settings)
    var isDarkTheme by remember { mutableStateOf(CushyStorage.getBoolean(KEY_DARK_THEME, false)) }

    // Demonstrating Int support (e.g., for user metrics)
    var userAge by remember { mutableIntStateOf(CushyStorage.getInt(KEY_USER_AGE, 25)) }


    // --- UTILITY STATES ---

    // Key used for demonstrating hasValue and remove functions
    var checkKey by remember { mutableStateOf("") }

    // Feedback message for the utility section
    var keyExistsStatus by remember { mutableStateOf("Enter a key to check") }


    // --- RESET CONFIRMATION DIALOG ---

    /**
     * Logic for the data reset confirmation.
     * This ensures destructive actions are intentional.
     */
    if (showResetDialogState.value) {
        AlertDialog(
            onDismissRequest = { showResetDialogState.value = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will clear Simple, Reactive, and Secure storage. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        // Clearing keys across all library layers
                        CushyStorage.remove(KEY_USERNAME)
                        CushyStorage.remove(KEY_DARK_THEME)
                        CushyStorage.remove(KEY_USER_AGE)
                        CushyStorage.saveStringReactive(KEY_COUNTER, "0")
                        CushyStorage.remove(KEY_SECURE_TOKEN)
                        CushyStorage.remove(KEY_SECURE_ALIAS)

                        // Closing dialog and providing feedback
                        showResetDialogState.value = false
                        Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialogState.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    // --- MAIN UI LAYOUT ---

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CushyStorage Demo") },
                actions = {
                    /**
                     * The 🛋️ icon acts as our global Reset button.
                     * It triggers the confirmation dialog.
                     */
                    IconButton(onClick = { showResetDialogState.value = true }) {
                        Text(text = "🛋️", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            Text(
                text = "Developer-to-Developer Storage Library",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))


            // --- SECTION: Standard Storage (Simple) ---

            SectionTitle("Standard Storage (Simple)")

            OutlinedTextField(
                value = simpleInput,
                onValueChange = {
                    simpleInput = it
                    // Simple synchronous save to SharedPreferences
                    CushyStorage.saveString(KEY_USERNAME, it)
                },
                label = { Text("Username (Auto-saves to SharedPreferences)") },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(32.dp))


            // --- SECTION: Reactive Storage (Simple) ---

            SectionTitle("Reactive Storage (Simple)")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "Live Counter: $reactiveCounter",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Button(
                            onClick = {
                                scope.launch {
                                    val next = (reactiveCounter.toIntOrNull() ?: 0) + 1
                                    // Async save to SimpleReactiveEngine (DataStore)
                                    CushyStorage.saveStringReactive(KEY_COUNTER, next.toString())
                                }
                            }
                        ) {
                            Text("Increment")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            onClick = {
                                scope.launch {
                                    /**
                                     * Demonstrates one-shot retrieval from DataStore
                                     * instead of observing a continuous Flow.
                                     */
                                    val onceValue = CushyStorage.getStringReactiveOnce(KEY_COUNTER)
                                    Toast.makeText(context, "Fetched Once: $onceValue", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Fetch Once")
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            // --- SECTION: Reactive + Secure ---

            SectionTitle("Reactive & Secure Storage")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    /**
                     * This UI element reacts to changes in the encrypted storage automatically.
                     * This is the library's most advanced data flow.
                     */
                    Text(
                        text = "Secret Alias (Live): ${secureReactiveAlias ?: "None"}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Updates automatically via Encrypted Flow.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            var aliasInput by remember { mutableStateOf("") }

            OutlinedTextField(
                value = aliasInput,
                onValueChange = { aliasInput = it },
                label = { Text("Update Secret Alias") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = {
                    scope.launch {
                        // Encrypts and syncs via DataStore Flow
                        CushyStorage.saveStringEncrypted(KEY_SECURE_ALIAS, aliasInput)
                    }
                }
            ) {
                Text("Sync Encrypted Alias")
            }


            Spacer(modifier = Modifier.height(32.dp))


            // --- SECTION: Secure Storage (One-Shot) ---

            SectionTitle("Secure Storage (One-Shot)")

            OutlinedTextField(
                value = secureInput,
                onValueChange = { secureInput = it },
                label = { Text("Manual Secret Token") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.padding(top = 8.dp)) {

                Button(onClick = {
                    scope.launch {
                        /**
                         * Encrypts plain text using AES-GCM and stores it securely.
                         * The KeyStore ensures the encryption key never leaves the device hardware.
                         */
                        CushyStorage.saveStringEncrypted(KEY_SECURE_TOKEN, secureInput)

                        // Updates the raw display so the developer can see the 'scrambled' result
                        rawEncryptedValue = CushyStorage.getRawStringEncrypted(KEY_SECURE_TOKEN) ?: ""
                    }
                }) {
                    Text("Save Secure")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    onClick = {
                        scope.launch {
                            /**
                             * Decrypts the Base64 string back into readable text.
                             * This demonstrates the 'getOnce' capability of the SecureEngine.
                             */
                            decryptedSecret = CushyStorage.getStringEncrypted(KEY_SECURE_TOKEN) ?: "Not found"
                        }
                    }
                ) {
                    Text("Decrypt & Load")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Raw Encrypted Base64 (IV + Ciphertext):", style = MaterialTheme.typography.labelSmall)

            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                /**
                 * Visually proves the data is encrypted. Note how the IV
                 * changes the output even if the input text is the same.
                 */
                Text(
                    text = rawEncryptedValue,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Using the state here resolves the 'unused variable' warning
            Card(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Decrypted Result: $decryptedSecret",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            Spacer(modifier = Modifier.height(32.dp))


            // --- SECTION: Other Simple Types ---

            SectionTitle("Other Simple Types")

            Row(verticalAlignment = Alignment.CenterVertically) {

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        isDarkTheme = it
                        // Persisting boolean type directly
                        CushyStorage.saveBoolean(KEY_DARK_THEME, it)
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Boolean Toggle (Simple)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("User Age (Int): $userAge")

            Slider(
                value = userAge.toFloat(),
                onValueChange = {
                    userAge = it.toInt()
                    // Persisting integer type directly
                    CushyStorage.saveInt(KEY_USER_AGE, it.toInt())
                },
                valueRange = 0f..100f
            )


            Spacer(modifier = Modifier.height(32.dp))


            // --- SECTION: Utilities ---

            SectionTitle("Library Utilities")

            OutlinedTextField(
                value = checkKey,
                onValueChange = { checkKey = it },
                label = { Text("Key to check/remove") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.padding(top = 8.dp)) {

                Button(onClick = {
                    /**
                     * demonstrating the hasValue utility.
                     * It checks if the key exists in the standard SharedPreferences layer.
                     */
                    val exists = CushyStorage.hasValue(checkKey)
                    keyExistsStatus = if (exists) "✅ '$checkKey' exists" else "❌ Not found"
                }) {
                    Text("Check hasValue")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        scope.launch {
                            /**
                             * demonstrating the unified remove function.
                             * It clears the key from Simple, Reactive, and Secure layers.
                             */
                            CushyStorage.remove(checkKey)
                            keyExistsStatus = "🗑️ Removed '$checkKey' from all layers"
                        }
                    }
                ) {
                    Text("Remove Key")
                }
            }

            Text(
                text = keyExistsStatus,
                modifier = Modifier.padding(top = 8.dp)
            )


            // Final bottom spacer to ensure content isn't cramped at the bottom edge
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Reusable Title component for Demo sections.
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}


/**
 * Preview for Android Studio.
 */
@Preview(showBackground = true, apiLevel = 35)
@Composable
fun DemoPreview() {
    // Ensure CushyStorage is initialized for the Preview.
    // In a real app, this happens in Application.onCreate().
    val context = LocalContext.current
    if (!CushyStorage.isInitialized) {
        CushyStorage.init(context)
    }

    CushyStorageTheme {
        CushyDemoScreen()
    }
}
