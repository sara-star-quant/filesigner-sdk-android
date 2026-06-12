package com.sarastarquant.filesigner

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.sarastarquant.filesigner.internal.StreamingSigner
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec

/**
 * Covers verification against an externally supplied X.509 public key, the path that
 * lets a relying party verify a signature produced on another device. Uses plain JCA
 * keys so the crypto round-trip runs on the JVM without Android KeyStore.
 */
class ExternalKeyVerifyTest {

    private fun generateKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
    }

    private fun sign(data: ByteArray, keyPair: KeyPair): ByteArray {
        return StreamingSigner().sign(ByteArrayInputStream(data), keyPair.private).getOrThrow()
    }

    @Test
    fun `reconstructed public key verifies a genuine signature`() {
        val data = "hardware-backed signing".toByteArray()
        val keyPair = generateKeyPair()
        val signature = sign(data, keyPair)

        val reconstructed = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(keyPair.public.encoded))

        val result = StreamingSigner().verify(ByteArrayInputStream(data), signature, reconstructed)

        assertTrue(result.getOrThrow())
    }

    @Test
    fun `reconstructed public key rejects a tampered file`() {
        val keyPair = generateKeyPair()
        val signature = sign("original".toByteArray(), keyPair)

        val reconstructed = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(keyPair.public.encoded))

        val result = StreamingSigner()
            .verify(ByteArrayInputStream("tampered".toByteArray()), signature, reconstructed)

        assertEquals(false, result.getOrThrow())
    }

    @Test
    fun `invalid public key bytes do not reconstruct`() {
        val reconstructed = try {
            KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(byteArrayOf(1, 2, 3)))
        } catch (_: Exception) {
            null
        }
        assertNull(reconstructed)
    }

    private fun buildSigner(resolver: ContentResolver): FileSigner {
        val context = mockk<Context>()
        every { context.applicationContext } returns context
        every { context.contentResolver } returns resolver
        return FileSigner.Builder(context).build()
    }

    @Test
    fun `verify overload returns Valid for a genuine signature`() {
        val data = "cross-device verification".toByteArray()
        val keyPair = generateKeyPair()
        val signature = sign(data, keyPair)

        val uri = mockk<Uri>()
        val resolver = mockk<ContentResolver>()
        every { resolver.openInputStream(uri) } answers { ByteArrayInputStream(data) }

        val result = buildSigner(resolver).verify(uri, signature, keyPair.public.encoded)

        assertEquals(VerificationResult.Valid, result)
    }

    @Test
    fun `verify overload returns Error for invalid public key bytes`() {
        val uri = mockk<Uri>()
        val resolver = mockk<ContentResolver>()

        val result = buildSigner(resolver).verify(uri, byteArrayOf(0), byteArrayOf(1, 2, 3))

        assertTrue(result is VerificationResult.Error)
    }
}
