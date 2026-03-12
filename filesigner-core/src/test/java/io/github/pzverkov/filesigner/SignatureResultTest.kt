package io.github.pzverkov.filesigner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SignatureResultTest {

    @Test
    fun `Success equality based on content`() {
        val sig1 = SignatureResult.Success(
            signatureBytes = byteArrayOf(1, 2, 3),
            algorithm = "SHA256withECDSA",
            timestamp = 1000L,
            securityLevel = SecurityLevel.STRONG_BOX,
        )
        val sig2 = SignatureResult.Success(
            signatureBytes = byteArrayOf(1, 2, 3),
            algorithm = "SHA256withECDSA",
            timestamp = 1000L,
            securityLevel = SecurityLevel.STRONG_BOX,
        )

        assertEquals(sig1, sig2)
        assertEquals(sig1.hashCode(), sig2.hashCode())
    }

    @Test
    fun `Success inequality on different bytes`() {
        val sig1 = SignatureResult.Success(
            signatureBytes = byteArrayOf(1, 2, 3),
            algorithm = "SHA256withECDSA",
            timestamp = 1000L,
            securityLevel = SecurityLevel.TEE,
        )
        val sig2 = SignatureResult.Success(
            signatureBytes = byteArrayOf(4, 5, 6),
            algorithm = "SHA256withECDSA",
            timestamp = 1000L,
            securityLevel = SecurityLevel.TEE,
        )

        assertNotEquals(sig1, sig2)
    }

    @Test
    fun `FileTooLarge carries size info`() {
        val error = SignatureResult.Error.FileTooLarge(
            sizeBytes = 600 * 1024 * 1024L,
            maxBytes = 500 * 1024 * 1024L,
        )
        assertEquals(600 * 1024 * 1024L, error.sizeBytes)
        assertEquals(500 * 1024 * 1024L, error.maxBytes)
    }

    @Test
    fun `error types are distinct`() {
        val errors: List<SignatureResult> = listOf(
            SignatureResult.Error.FileNotFound,
            SignatureResult.Error.FileReadError,
            SignatureResult.Error.KeyGenerationFailed,
            SignatureResult.Error.SigningFailed("test"),
            SignatureResult.Error.FileTooLarge(100, 50),
        )
        assertEquals(5, errors.distinct().size)
    }
}
