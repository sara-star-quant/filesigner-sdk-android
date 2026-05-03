package com.sarastarquant.filesigner

/**
 * Configuration for a [FileSigner] instance.
 *
 * @property keyAlias The Android KeyStore alias for the signing key.
 * @property preferStrongBox Whether to prefer StrongBox hardware backing over TEE on Android 9+.
 * @property maxFileSize Maximum file size in bytes that will be accepted for signing.
 */
data class SignerConfig(
    val keyAlias: String = DEFAULT_KEY_ALIAS,
    val preferStrongBox: Boolean = true,
    val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
) {
    internal companion object {
        const val DEFAULT_KEY_ALIAS = "filesigner_ecdsa_p256_v1"
        const val DEFAULT_MAX_FILE_SIZE = 500L * 1024 * 1024 // 500 MB
    }
}
