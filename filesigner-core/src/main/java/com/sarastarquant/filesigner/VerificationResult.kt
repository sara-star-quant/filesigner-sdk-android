package com.sarastarquant.filesigner

/**
 * Result of a signature verification operation.
 */
sealed class VerificationResult {
    /** The signature is valid - the file has not been modified since signing. */
    data object Valid : VerificationResult()

    /** The signature is invalid - the file content does not match the signature. */
    data object Invalid : VerificationResult()

    /** Verification could not be completed. */
    data class Error(val message: String) : VerificationResult()
}
