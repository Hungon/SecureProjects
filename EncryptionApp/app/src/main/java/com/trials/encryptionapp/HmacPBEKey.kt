package com.trials.encryptionapp

import android.util.Log
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class HmacPBEKey {

    private var salt: ByteArray? = null

    constructor() {
        initSalt()
    }

    constructor(_salt: ByteArray) {
        salt = _salt
    }

    private fun initSalt() {
        salt = ByteArray(SALT_LENGTH_BYTES)
        val sr = SecureRandom()
        sr.nextBytes(salt)
    }

    fun sign(plain: ByteArray, password: CharArray): ByteArray? {
        return calculate(plain, password)
    }

    private fun calculate(plain: ByteArray, password: CharArray): ByteArray? {
        var hmac: ByteArray? = null
        try {
            val mac = Mac.getInstance(TRANSFORMATION)
            val secretKey = generateKey(password, salt)
            mac.init(secretKey)
            hmac = mac.doFinal(plain)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG,"calculate() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG,"calculate() ${e.message}")
        } finally {
        }
        return hmac
    }

    fun verify(hmac: ByteArray?, plain: ByteArray, password: CharArray): Boolean {
        val hmacForPlain = calculate(plain, password)
        return (hmacForPlain != null && hmac != null && Arrays.equals(hmac, hmacForPlain))
    }

    companion object {

        private val TAG = HmacPBEKey::class.java.simpleName
        private const val TRANSFORMATION = "PBEWITHHMACSHA1"
        private const val KEY_GENERATOR_MODE = "PBEWITHHMACSHA1"
        const val SALT_LENGTH_BYTES = 20
        private const val KEY_GEN_ITERATION_COUNT = 1024
        private const val KEY_LENGTH_BITS = 160

        private fun generateKey(password: CharArray, salt: ByteArray?): SecretKey? {
            if (salt == null) {
                Log.e(TAG, "generateKey() salt is null")
                return null
            }
            var secretKey: SecretKey? = null
            var keySpec: PBEKeySpec? = null
            try {
                val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_MODE)
                keySpec = PBEKeySpec(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH_BITS) // password のクリア
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