package com.sarastarquant.filesigner.internal

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import com.sarastarquant.filesigner.SecurityLevel
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Manages ECDSA P-256 keys in Android KeyStore with StrongBox/TEE hardware backing.
 */
internal class KeystoreManager(
    private val keyAlias: String,
    private val preferStrongBox: Boolean,
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    var securityLevel: SecurityLevel = SecurityLevel.UNKNOWN
        private set

    fun hasKey(): Boolean {
        return try {
            keyStore.containsAlias(keyAlias)
        } catch (_: Exception) {
            false
        }
    }

    fun generateKeyPair(): Result<KeyPair> {
        return try {
            val keyPair = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && preferStrongBox) {
                try {
                    val kp = generateKeyPairWithSpec(strongBox = true)
                    securityLevel = SecurityLevel.STRONG_BOX
                    kp
                } catch (_: StrongBoxUnavailableException) {
                    val kp = generateKeyPairWithSpec(strongBox = false)
                    securityLevel = SecurityLevel.TEE
                    kp
                }
            } else {
                val kp = generateKeyPairWithSpec(strongBox = false)
                securityLevel = SecurityLevel.TEE
                kp
            }
            Result.success(keyPair)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateKeyPairWithSpec(strongBox: Boolean): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE,
        )

        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        ).apply {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            setUserAuthenticationRequired(false)
            setInvalidatedByBiometricEnrollment(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && strongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_DEVICE_CREDENTIAL)
        }

        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

    fun getPrivateKey(): PrivateKey? {
        return try {
            keyStore.getKey(keyAlias, null) as? PrivateKey
        } catch (_: Exception) {
            null
        }
    }

    fun getPublicKey(): PublicKey? {
        return try {
            keyStore.getCertificate(keyAlias)?.publicKey
        } catch (_: Exception) {
            null
        }
    }
}
