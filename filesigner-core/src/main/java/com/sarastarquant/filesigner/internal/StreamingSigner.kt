package com.sarastarquant.filesigner.internal

import java.io.InputStream
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

/**
 * Performs streaming ECDSA signature operations in chunks, keeping memory usage constant
 * regardless of file size.
 */
internal class StreamingSigner {

    companion object {
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val BUFFER_SIZE = 8192
    }

    fun sign(input: InputStream, privateKey: PrivateKey): Result<ByteArray> {
        return try {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(privateKey)
            }

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            try {
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    signature.update(buffer, 0, bytesRead)
                }
            } finally {
                buffer.fill(0)
            }

            Result.success(signature.sign())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun verify(input: InputStream, signatureBytes: ByteArray, publicKey: PublicKey): Result<Boolean> {
        return try {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(publicKey)
            }

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            try {
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    signature.update(buffer, 0, bytesRead)
                }
            } finally {
                buffer.fill(0)
            }

            Result.success(signature.verify(signatureBytes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
