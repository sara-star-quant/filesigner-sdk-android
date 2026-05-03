package com.sarastarquant.filesigner

/**
 * Indicates the hardware security level used for key storage and cryptographic operations.
 */
enum class SecurityLevel {
    /** Key is backed by a dedicated StrongBox secure element (highest security). */
    STRONG_BOX,

    /** Key is backed by the Trusted Execution Environment. */
    TEE,

    /** Security level could not be determined. Key is still in Android KeyStore. */
    UNKNOWN,
}
