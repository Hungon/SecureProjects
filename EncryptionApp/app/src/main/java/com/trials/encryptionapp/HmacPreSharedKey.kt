package com.trials.encryptionapp

import android.util.Log
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class HmacPreSharedKey {


    fun sign(plain: ByteArray, keyData: ByteArray): ByteArray? {
        return calculate(plain, keyData)
    }

    fun calculate(plain: ByteArray, keyData: ByteArray): ByteArray? {
        var hmac: ByteArray? = null
        var count = 0
        try {
            val mac = Mac.getInstance(TRANSFORMATION)
            val secretKey = generateKey(keyData)
            if (secretKey != null) {
                mac.init(secretKey)
                hmac = mac.doFinal(plain)
                val sb = StringBuilder()
                if (hmac == null) {
                    Log.e(TAG, "calculate() hmac is null")
                    return null
                }
                for (i in hmac.indices) {
                    sb.append(Integer.toHexString(hmac[i].toInt() and 0xff))
                    count++
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "calculate() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "calculate() ${e.message}")
        } finally {
        }
        return hmac
    }

    fun verify(hmac: ByteArray?, plain: ByteArray, keyData: ByteArray): Boolean {
        val hmacForPlain = calculate(plain, keyData)
        return (hmacForPlain != null && hmac != null && Arrays.equals(hmac, hmacForPlain))
    }

    companion object {
        private val TAG = HmacPreSharedKey::class.java.simpleName
        private const val TRANSFORMATION = "HmacSHA256"
        private const val KEY_ALGORITHM = "HmacSHA256"
        private const val MIN_KEY_LENGTH_BYTES = 16

        private fun generateKey(keyData: ByteArray): SecretKey? {
            return try {
                if (keyData.size >= MIN_KEY_LENGTH_BYTES) {
                    SecretKeySpec(keyData, KEY_ALGORITHM)
                } else {
                    null
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "generateKey() ${e.message}")
                null
            }
        }
    }
}
