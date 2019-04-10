package com.trials.encryptionapp

import android.util.Log
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesCryptoPreSharedKey(private var iv: ByteArray) {

    fun encrypt(keyData: ByteArray, plain: ByteArray): ByteArray? {
        var encrypted: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(keyData)
            if (secretKey != null) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                iv = cipher.iv
                encrypted = cipher.doFinal(plain)
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG,"encrypt() ${e.message}")
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG,"encrypt() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG,"encrypt() ${e.message}")
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG,"encrypt() ${e.message}")
        } catch (e: BadPaddingException) {
            Log.e(TAG,"encrypt() ${e.message}")
        } finally {
        }
        return encrypted
    }

    fun decrypt(keyData: ByteArray?, encrypted: ByteArray?): ByteArray? {
        var plain: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(keyData)
            if (secretKey != null) {
                val ivParameterSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
                plain = cipher.doFinal(encrypted)
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } catch (e: InvalidAlgorithmParameterException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } catch (e: BadPaddingException) {
            Log.e(TAG,"decrypt() ${e.message}")
        } finally {
        }
        return plain
    }

    companion object {

        private val TAG = AesCryptoPreSharedKey::class.java.simpleName
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val KEY_ALGORITHM = "AES"
        const val IV_LENGTH_BYTES = 16
        private const val MIN_KEY_LENGTH_BYTES = 16

        private fun generateKey(keyData: ByteArray?): SecretKey? {
            if (keyData ==null) {
                Log.e(TAG,"generateKey() keyData is null")
                return null
            }
            var secretKey: SecretKey? = null
            try {
                if (keyData.size >= MIN_KEY_LENGTH_BYTES) {
                    secretKey = SecretKeySpec(keyData, KEY_ALGORITHM)
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "generateKey() ${e.message}")
            }
            return secretKey
        }
    }
}