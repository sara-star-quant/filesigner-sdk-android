package com.sarastarquant.filesigner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignerConfigTest {

    @Test
    fun `default config has expected values`() {
        val config = SignerConfig()
        assertEquals("filesigner_ecdsa_p256_v1", config.keyAlias)
        assertTrue(config.preferStrongBox)
        assertEquals(500L * 1024 * 1024, config.maxFileSize)
    }

    @Test
    fun `custom config preserves values`() {
        val config = SignerConfig(
            keyAlias = "custom_key",
            preferStrongBox = false,
            maxFileSize = 100L * 1024 * 1024,
        )
        assertEquals("custom_key", config.keyAlias)
        assertEquals(false, config.preferStrongBox)
        assertEquals(100L * 1024 * 1024, config.maxFileSize)
    }
}
