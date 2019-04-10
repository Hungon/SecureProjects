package com.trials.encryptionapp

import android.util.Log
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec


class AesCryptoPBEKey {


    var iv: ByteArray? = null
    var salt: ByteArray? = null

    constructor(_iv: ByteArray, _salt: ByteArray) {
        iv = _iv
        salt = _salt
    }

    constructor() {
        iv = null
        initSalt()
    }

    private fun initSalt() {
        salt = ByteArray(SALT_LENGTH_BYTES)
        val sr = SecureRandom()
        sr.nextBytes(salt)
    }

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray? {
        var encrypted: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(password, salt)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            iv = cipher.iv
            encrypted = cipher.doFinal(plain)
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

    fun decrypt(encrypted: ByteArray?, password: CharArray): ByteArray? {
        var plain: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(password, salt)
            val ivParameterSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            plain = cipher.doFinal(encrypted)
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

        private val TAG = AesCryptoPBEKey::class.java.simpleName

        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val KEY_GENERATOR_MODE = "PBEWITHSHA256AND128BITAES-CBC-BC"
        const val SALT_LENGTH_BYTES = 20
        private const val KEY_GEN_ITERATION_COUNT = 1024
        private const val KEY_LENGTH_BITS = 128

        private fun generateKey(password: CharArray, salt: ByteArray?): SecretKey? {
            if (salt == null) {
                Log.e(TAG, "generateKey() salt is null")
                return null
            }
            var secretKey: SecretKey? = null
            var keySpec: PBEKeySpec? = null
            try {
                val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_MODE)
                keySpec = PBEKeySpec(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH_BITS)
                // reset password
                Arrays.fill(password, '?')
                secretKey = secretKeyFactory.generateSecret(keySpec)
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG,"generateKey() ${e.message}")
            } catch (e: InvalidKeySpecException) {
                Log.e(TAG,"generateKey() ${e.message}")
            } finally {
                keySpec?.clearPassword()
            }
            return secretKey
        }

    }
}