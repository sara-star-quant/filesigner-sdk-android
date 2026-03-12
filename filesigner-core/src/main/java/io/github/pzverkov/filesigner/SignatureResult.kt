package io.github.pzverkov.filesigner

/**
 * Result of a signing operation.
 */
sealed class SignatureResult {

    /**
     * Signing completed successfully.
     *
     * @property signatureBytes The DER-encoded ECDSA signature bytes.
     * @property algorithm The signature algorithm used (e.g. "SHA256withECDSA").
     * @property timestamp The Unix timestamp (milliseconds) when the signature was produced.
     * @property securityLevel The hardware security level of the key that produced the signature.
     */
    data class Success(
        val signatureBytes: ByteArray,
        val algorithm: String,
        val timestamp: Long,
        val securityLevel: SecurityLevel,
    ) : SignatureResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return signatureBytes.contentEquals(other.signatureBytes) &&
                algorithm == other.algorithm &&
                timestamp == other.timestamp &&
                securityLevel == other.securityLevel
        }

        override fun hashCode(): Int {
            var result = signatureBytes.contentHashCode()
            result = 31 * result + algorithm.hashCode()
            result = 31 * result + timestamp.hashCode()
            result = 31 * result + securityLevel.hashCode()
            return result
        }
    }

    /**
     * Signing failed.
     */
    sealed class Error : SignatureResult() {
        /** The file URI could not be resolved or the file does not exist. */
        data object FileNotFound : Error()

        /** The file could not be read (I/O error or permission issue). */
        data object FileReadError : Error()

        /** The file exceeds [SignerConfig.maxFileSize]. */
        data class FileTooLarge(val sizeBytes: Long, val maxBytes: Long) : Error()

        /** The signing key could not be generated in Android KeyStore. */
        data object KeyGenerationFailed : Error()

        /** The signing operation failed. */
        data class SigningFailed(val message: String) : Error()
    }
}
