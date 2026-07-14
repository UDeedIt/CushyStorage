package pro.udeedit.devtools.cushystorage

/**
 * Configuration options for CushyStorage.
 *
 * @property aesMode The encryption mode to use. Default is "AES/GCM/NoPadding".
 * @property ivSizeBytes The size of the Initialization Vector in bytes. Default is 12.
 * @property tagSizeBits The size of the authentication tag in bits. Default is 128.
 * @property keySize The size of the AES key in bits. Default is 256.
 */
data class CushyConfig(
    val aesMode: String = "AES/GCM/NoPadding",
    val ivSizeBytes: Int = 12,
    val tagSizeBits: Int = 128,
    val keySize: Int = 256
)
