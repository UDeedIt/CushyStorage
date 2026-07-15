# CushyStorage 🛋️

A unified, ultra-comfortable wrapper for Android Preferences.

CushyStorage provides a single, intuitive API that replaces the fragmented use of standard SharedPreferences, Jetpack DataStore, and the deprecated EncryptedSharedPreferences.

## 🚀 Why CushyStorage?
Android storage is fragmented. Developers often have to choose between old blocking APIs and complex reactive ones. CushyStorage bridges this gap by offering three distinct layers under one "Cushy" interface:

1. Standard Storage (Simple): Fast, synchronous SharedPreferences for non-sensitive UI settings.
2. Reactive Storage (Simple): Flow-based DataStore for real-time UI updates (perfect for Jetpack Compose).
3. Secure Storage (Encrypted): Hardware-backed AES-GCM encryption via Android KeyStore, providing a modern replacement for the legacy EncryptedSharedPreferences.

## ✨ Key Features
- Flat API: Access everything via a single CushyStorage object.
- Zero Boilerplate: No manual Cipher or KeyStore setup required.
- Reactive by Design: Built-in support for Kotlin Flow to update UI automatically.
- Developer Focused (D2D): Comprehensive KDoc documentation and clean internal architecture.
- Customizable: Optional CushyConfig to adjust encryption parameters (Tag size, IV size, etc.).

## 🛠 Quick Start

### 1. Initialize
Initialize once in your Application class:
kotlin 
class MyApp : Application() { 
    override fun onCreate() {
        super.onCreate() 
        // Minimal setup 
        CushyStorage.init(this) 
    } 
} 

### 2. Basic Usage
kotlin
// Simple Save (Synchronous)
CushyStorage.saveString("username", "Alex") 

// Simple Get 
val name = CushyStorage.getString("username")


### 3. Reactive & Secure Usage
kotlin 
// Observe data changes in Compose 
val counter by CushyStorage.observeString("count", "0").collectAsStateWithLifecycle() 

// Secure Encryption (Asynchronous) 
lifecycleScope.launch { 
     CushyStorage.saveStringEncrypted("token", "secret_api_key")
     val decrypted = CushyStorage.getStringEncrypted("token") 
}

## 🔐 Security Architecture
CushyStorage implements AES-GCM (Galois/Counter Mode), which provides both data confidentiality and authenticity.
- Key Management: Cryptographic keys are generated and stored in the Android KeyStore, ensuring they never leave the device's secure hardware.
- Initialization Vector (IV): A unique, random IV is generated for every write operation and bundled with the ciphertext to prevent pattern analysis.

## 📄 License
This project is licensed under the MIT License.

---
Developed with ❤️ for the Android Developer Community.