package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class CryptoManager {

    private val keyStore: KeyStore? = try {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun getSecretKey(): SecretKey? {
        if (keyStore == null) return null
        return try {
            val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
            existingKey?.secretKey ?: createSecretKey()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                keyStore.deleteEntry("secret")
                createSecretKey()
            } catch (e2: Exception) {
                e2.printStackTrace()
                null
            }
        }
    }

    private fun createSecretKey(): SecretKey? {
        if (keyStore == null) return null
        return try {
            KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
                init(
                    KeyGenParameterSpec.Builder(
                        "secret",
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
            }.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encrypt(bytes: ByteArray): String {
        val secretKey = getSecretKey() ?: return Base64.encodeToString(bytes, Base64.DEFAULT)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(bytes)
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    fun decrypt(data: String): ByteArray {
        val secretKey = getSecretKey()
        val combined = Base64.decode(data, Base64.DEFAULT)
        if (secretKey == null) return combined
        return try {
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            combined // Return raw if decryption fails
        }
    }

    fun encryptString(text: String): String {
        if (text.isEmpty()) return ""
        return encrypt(text.toByteArray(Charsets.UTF_8))
    }

    fun decryptString(data: String): String {
        if (data.isEmpty()) return ""
        return try {
            String(decrypt(data), Charsets.UTF_8)
        } catch (e: Exception) {
            data // Return raw if decryption fails (e.g. unencrypted legacy note)
        }
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
