package com.sarastarquant.filesigner

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.sarastarquant.filesigner.internal.KeystoreManager
import com.sarastarquant.filesigner.internal.StreamingSigner
import java.io.FileNotFoundException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

/**
 * Hardware-backed ECDSA P-256 file signing SDK for Android.
 *
 * Signs files using keys stored in Android KeyStore (StrongBox or TEE).
 * Private keys never leave the hardware secure enclave.
 *
 * Usage:
 * ```kotlin
 * val signer = FileSigner.Builder(context)
 *     .keyAlias("my_app_key_v1")
 *     .preferStrongBox(true)
 *     .maxFileSize(500 * 1024 * 1024L)
 *     .build()
 *
 * val result = signer.sign(fileUri)
 * val verification = signer.verify(fileUri, signatureBytes)
 * ```
 */
class FileSigner private constructor(
    private val contentResolver: ContentResolver,
    private val config: SignerConfig,
) {
    private val keystoreManager = KeystoreManager(config.keyAlias, config.preferStrongBox)
    private val streamingSigner = StreamingSigner()

    /**
     * Signs a file identified by its content URI.
     *
     * If no signing key exists, one is generated automatically in Android KeyStore
     * with StrongBox preference (falling back to TEE).
     *
     * @param fileUri Content URI of the file to sign.
     * @return [SignatureResult.Success] with DER-encoded signature bytes, or [SignatureResult.Error].
     */
    fun sign(fileUri: Uri): SignatureResult {
        // Ensure key exists
        if (!keystoreManager.hasKey()) {
            val keyResult = keystoreManager.generateKeyPair()
            if (keyResult.isFailure) {
                return SignatureResult.Error.KeyGenerationFailed
            }
        }

        // Validate file size
        val fileSize = getFileSize(fileUri)
        if (fileSize == null) {
            return SignatureResult.Error.FileNotFound
        }
        if (fileSize > config.maxFileSize) {
            return SignatureResult.Error.FileTooLarge(fileSize, config.maxFileSize)
        }

        // Get private key
        val privateKey = keystoreManager.getPrivateKey()
            ?: return SignatureResult.Error.KeyGenerationFailed

        // Open stream and sign
        val inputStream = try {
            contentResolver.openInputStream(fileUri)
                ?: return SignatureResult.Error.FileNotFound
        } catch (_: FileNotFoundException) {
            return SignatureResult.Error.FileNotFound
        } catch (_: SecurityException) {
            return SignatureResult.Error.FileReadError
        }

        val signResult = inputStream.use { stream ->
            streamingSigner.sign(stream, privateKey)
        }

        return signResult.fold(
            onSuccess = { signatureBytes ->
                SignatureResult.Success(
                    signatureBytes = signatureBytes,
                    algorithm = StreamingSigner.SIGNATURE_ALGORITHM,
                    timestamp = System.currentTimeMillis(),
                    securityLevel = keystoreManager.securityLevel,
                )
            },
            onFailure = { e ->
                SignatureResult.Error.SigningFailed(e.message ?: "Unknown error")
            },
        )
    }

    /**
     * Verifies a file against a previously produced signature.
     *
     * @param fileUri Content URI of the original file.
     * @param signatureBytes The DER-encoded ECDSA signature bytes to verify against.
     * @return [VerificationResult.Valid], [VerificationResult.Invalid], or [VerificationResult.Error].
     */
    fun verify(fileUri: Uri, signatureBytes: ByteArray): VerificationResult {
        val publicKey = keystoreManager.getPublicKey()
            ?: return VerificationResult.Error("No verification key available")

        val inputStream = try {
            contentResolver.openInputStream(fileUri)
                ?: return VerificationResult.Error("File not found")
        } catch (_: FileNotFoundException) {
            return VerificationResult.Error("File not found")
        } catch (_: SecurityException) {
            return VerificationResult.Error("File access denied")
        }

        val verifyResult = inputStream.use { stream ->
            streamingSigner.verify(stream, signatureBytes, publicKey)
        }

        return verifyResult.fold(
            onSuccess = { isValid ->
                if (isValid) VerificationResult.Valid else VerificationResult.Invalid
            },
            onFailure = { e ->
                VerificationResult.Error(e.message ?: "Verification failed")
            },
        )
    }

    /**
     * Verifies a file against a signature using an externally supplied public key.
     *
     * Unlike [verify], this does not use the device KeyStore and does not require a
     * local signing key. Use it to verify a signature produced on another device:
     * the signer exports its key via [getPublicKeyEncoded], the relying party passes
     * those bytes here.
     *
     * @param fileUri Content URI of the original file.
     * @param signatureBytes The DER-encoded ECDSA signature bytes to verify against.
     * @param publicKeyBytes The X.509 (SubjectPublicKeyInfo) encoded EC public key,
     *                       as returned by [getPublicKeyEncoded].
     * @return [VerificationResult.Valid], [VerificationResult.Invalid], or [VerificationResult.Error].
     */
    fun verify(fileUri: Uri, signatureBytes: ByteArray, publicKeyBytes: ByteArray): VerificationResult {
        val publicKey = publicKeyFromEncoded(publicKeyBytes)
            ?: return VerificationResult.Error("Invalid public key")

        val inputStream = try {
            contentResolver.openInputStream(fileUri)
                ?: return VerificationResult.Error("File not found")
        } catch (_: FileNotFoundException) {
            return VerificationResult.Error("File not found")
        } catch (_: SecurityException) {
            return VerificationResult.Error("File access denied")
        }

        val verifyResult = inputStream.use { stream ->
            streamingSigner.verify(stream, signatureBytes, publicKey)
        }

        return verifyResult.fold(
            onSuccess = { isValid ->
                if (isValid) VerificationResult.Valid else VerificationResult.Invalid
            },
            onFailure = { e ->
                VerificationResult.Error(e.message ?: "Verification failed")
            },
        )
    }

    /**
     * Returns whether a signing key already exists in Android KeyStore.
     */
    fun hasSigningKey(): Boolean = keystoreManager.hasKey()

    /**
     * Returns the encoded public key bytes (X.509 SubjectPublicKeyInfo), or null if no key exists.
     */
    fun getPublicKeyEncoded(): ByteArray? = keystoreManager.getPublicKey()?.encoded

    /**
     * Returns the current configuration.
     */
    fun getConfig(): SignerConfig = config

    private fun publicKeyFromEncoded(encoded: ByteArray): PublicKey? {
        return try {
            KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(encoded))
        } catch (_: Exception) {
            null
        }
    }

    private fun getFileSize(uri: Uri): Long? {
        return try {
            contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        cursor.getLong(sizeIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Builder for creating [FileSigner] instances.
     *
     * @param context Android context, used to obtain a [ContentResolver]. The application context
     *                is extracted automatically to avoid leaking activity references.
     */
    class Builder(context: Context) {
        private val contentResolver: ContentResolver = context.applicationContext.contentResolver
        private var keyAlias: String = SignerConfig.DEFAULT_KEY_ALIAS
        private var preferStrongBox: Boolean = true
        private var maxFileSize: Long = SignerConfig.DEFAULT_MAX_FILE_SIZE

        /**
         * Sets the Android KeyStore alias for the signing key.
         * Default: `"filesigner_ecdsa_p256_v1"`
         */
        fun keyAlias(alias: String) = apply { this.keyAlias = alias }

        /**
         * Whether to prefer StrongBox hardware backing over TEE on Android 9+.
         * Falls back to TEE automatically if StrongBox is unavailable.
         * Default: `true`
         */
        fun preferStrongBox(prefer: Boolean) = apply { this.preferStrongBox = prefer }

        /**
         * Maximum file size in bytes. Files exceeding this limit will return
         * [SignatureResult.Error.FileTooLarge].
         * Default: 500 MB
         */
        fun maxFileSize(bytes: Long) = apply {
            require(bytes > 0) { "maxFileSize must be positive" }
            this.maxFileSize = bytes
        }

        /**
         * Creates a [FileSigner] instance with the configured settings.
         */
        fun build(): FileSigner {
            val config = SignerConfig(
                keyAlias = keyAlias,
                preferStrongBox = preferStrongBox,
                maxFileSize = maxFileSize,
            )
            return FileSigner(contentResolver, config)
        }
    }
}
