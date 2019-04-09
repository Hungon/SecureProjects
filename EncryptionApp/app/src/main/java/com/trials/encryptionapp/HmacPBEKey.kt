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
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val mac = Mac.getInstance(TRANSFORMATION)
            // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
            val secretKey = generateKey(password, salt)
            mac.init(secretKey)
            hmac = mac.doFinal(plain)
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: InvalidKeyException) {
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
        // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
        // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する // Mac クラスの getInstance に渡すパラメータ (認証モード)
        private const val TRANSFORMATION = "PBEWITHHMACSHA1"
        // 鍵を生成するクラスのインスタンスを取得するための文字列
        private const val KEY_GENERATOR_MODE = "PBEWITHHMACSHA1"
        // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
        // Salt のバイト長
        const val SALT_LENGTH_BYTES = 20
        // ★ポイント 4 ★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
        // PBE で鍵を生成する際の攪拌の繰り返し回数
        private const val KEY_GEN_ITERATION_COUNT = 1024
        // ★ポイント 5 ★ 十分安全な長さを持つ鍵を利用する // 鍵のビット長
        private const val KEY_LENGTH_BITS = 160

        private fun generateKey(password: CharArray, salt: ByteArray?): SecretKey? {
            if (salt == null) {
                Log.e(TAG, "generateKey() salt is null")
                return null
            }
            var secretKey: SecretKey? = null
            var keySpec: PBEKeySpec? = null
            try {
                // ★ポイント 2 ★ 脆弱でない (基準を満たす) アルゴリズム・モード・パディングを使用する
                // 鍵を生成するクラスのインスタンスを取得する
                // 例では、AES-CBC 128 ビット用の鍵を SHA1 を利用して生成する KeyFactory を使用。
                val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_MODE)
                // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
                // ★ポイント 4 ★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
                // ★ポイント 5 ★ 十分安全な長さを持つ鍵を利用する
                keySpec = PBEKeySpec(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH_BITS) // password のクリア
                Arrays.fill(password, '?')
                // 鍵を生成する
                secretKey = secretKeyFactory.generateSecret(keySpec)
            } catch (e: NoSuchAlgorithmException) {
            } catch (e: InvalidKeySpecException) {
            } finally {
                keySpec?.clearPassword()
            }
            return secretKey
        }

    }
}