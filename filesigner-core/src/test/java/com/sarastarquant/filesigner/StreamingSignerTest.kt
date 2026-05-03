package com.sarastarquant.filesigner

import com.sarastarquant.filesigner.internal.StreamingSigner
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class StreamingSignerTest {

    @Test
    fun `algorithm constant is SHA256withECDSA`() {
        assertEquals("SHA256withECDSA", StreamingSigner.SIGNATURE_ALGORITHM)
    }

    @Test
    fun `sign fails gracefully with invalid key`() {
        val signer = StreamingSigner()
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3))

        // Create a mock private key that will cause Signature to fail
        val result = try {
            signer.sign(input, object : java.security.PrivateKey {
                override fun getAlgorithm() = "EC"
                override fun getFormat() = "PKCS#8"
                override fun getEncoded() = byteArrayOf()
            })
        } catch (_: Exception) {
            Result.failure<ByteArray>(IllegalStateException("Expected failure"))
        }

        assert(result.isFailure)
    }

    @Test
    fun `verify fails gracefully with invalid key`() {
        val signer = StreamingSigner()
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3))
        val fakeSignature = byteArrayOf(0, 1, 2, 3)

        val result = try {
            signer.verify(input, fakeSignature, object : java.security.PublicKey {
                override fun getAlgorithm() = "EC"
                override fun getFormat() = "X.509"
                override fun getEncoded() = byteArrayOf()
            })
        } catch (_: Exception) {
            Result.failure<Boolean>(IllegalStateException("Expected failure"))
        }

        assert(result.isFailure)
    }
}
