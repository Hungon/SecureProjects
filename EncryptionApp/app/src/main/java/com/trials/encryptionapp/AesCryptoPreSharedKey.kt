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
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(keyData)
            if (secretKey != null) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                iv = cipher.iv
                encrypted = cipher.doFinal(plain)
            }
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: NoSuchPaddingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: IllegalBlockSizeException) {
        } catch (e: BadPaddingException) {
        } finally {
        }
        return encrypted
    }

    fun decrypt(keyData: ByteArray, encrypted: ByteArray): ByteArray? {
        var plain: ByteArray? = null
        try {
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateKey(keyData)
            if (secretKey != null) {
                val ivParameterSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
                plain = cipher.doFinal(encrypted)
            }
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: NoSuchPaddingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: InvalidAlgorithmParameterException) {
        } catch (e: IllegalBlockSizeException) {
        } catch (e: BadPaddingException) {
        } finally {
        }
        return plain
    }

    companion object {

        private val TAG = AesCryptoPreSharedKey::class.java.simpleName
        // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
        // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
        // Cipher クラスの getInstance に渡すパラメータ (/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
        // サンプルでは、暗号アルゴリズム=AES、ブロック暗号モード=CBC、パディングルール=PKCS7Padding
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        // 暗号アルゴリズム
        private const val KEY_ALGORITHM = "AES"
        // IV のバイト長
        const val IV_LENGTH_BYTES = 16
        // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する
        // 鍵長チェック
        private const val MIN_KEY_LENGTH_BYTES = 16

        private fun generateKey(keyData: ByteArray): SecretKey? {
            var secretKey: SecretKey? = null
            try {
                // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する
                if (keyData.size >= MIN_KEY_LENGTH_BYTES) {
                    // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
                    secretKey = SecretKeySpec(keyData, KEY_ALGORITHM)
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "generateKey() ${e.message}")
            }
            return secretKey
        }
    }
}