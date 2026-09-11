package com.example.maiplan.utils.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class TokenCipher {
    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "maiplan_session_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val STORED_VALUE_VERSION = "v1"
        const val GCM_TAG_LENGTH_BITS = 128
        val KEY_LOCK = Any()
    }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val initializationVector = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encryptedValue = Base64.encodeToString(
            cipher.doFinal(value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

        return "$STORED_VALUE_VERSION:$initializationVector:$encryptedValue"
    }

    fun decrypt(storedValue: String): String {
        val components = storedValue.split(':', limit = 3)

        require(components.size == 3 && components[0] == STORED_VALUE_VERSION) {
            "Unsupported encrypted token format"
        }

        val initializationVector = Base64.decode(components[1], Base64.NO_WRAP)
        val encryptedValue = Base64.decode(components[2], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, initializationVector)
        )

        return cipher.doFinal(encryptedValue).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey = synchronized(KEY_LOCK) {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }

        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)
            ?.secretKey
            ?: KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            ).run {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )

                generateKey()
            }
    }
}
