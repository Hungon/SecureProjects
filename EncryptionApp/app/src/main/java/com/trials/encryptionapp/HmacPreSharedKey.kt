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
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
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
        } catch (e: InvalidKeyException) {
        } finally {
        }
        return hmac
    }

    fun verify(hmac: ByteArray, plain: ByteArray, keyData: ByteArray): Boolean {
        val hmacForPlain = calculate(plain, keyData)
        return (hmacForPlain != null && Arrays.equals(hmac, hmacForPlain))
    }

    companion object {
        private val TAG = HmacPreSharedKey::class.java.simpleName
        // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
        // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
        // Mac クラスの getInstance に渡すパラメータ (認証モード)
        private const val TRANSFORMATION = "HmacSHA256"
        // 暗号アルゴリズム
        private const val KEY_ALGORITHM = "HmacSHA256"
        // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する
        // 鍵長チェック
        private const val MIN_KEY_LENGTH_BYTES = 16

        private fun generateKey(keyData: ByteArray): SecretKey? {
            return try {
                if (keyData.size >= MIN_KEY_LENGTH_BYTES) {
                    // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
                    SecretKeySpec(keyData, KEY_ALGORITHM)
                } else {
                    null
                }
            } catch (e:IllegalArgumentException) {
                null
            }
        }
    }
}
