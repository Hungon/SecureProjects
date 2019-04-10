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
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
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
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
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

        // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
        // Cipher クラスの getInstance に渡すパラメータ (/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール]) // サンプルでは、暗号アルゴリズム=AES、ブロック暗号モード=CBC、パディングルール=PKCS7Padding
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        // 鍵を生成するクラスのインスタンスを取得するための文字列
        private const val KEY_GENERATOR_MODE = "PBEWITHSHA256AND128BITAES-CBC-BC"
        // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
        // Salt のバイト長
        const val SALT_LENGTH_BYTES = 20
        // ★ポイント 4 ★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
        // PBE で鍵を生成する際の攪拌の繰り返し回数
        private const val KEY_GEN_ITERATION_COUNT = 1024
        // ★ポイント 5 ★ 十分安全な長さを持つ鍵を利用する // 鍵のビット長
        private const val KEY_LENGTH_BITS = 128

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
                // 例では、AES-CBC 128 ビット用の鍵を SHA256 を利用して生成する KeyFactory を使用。
                val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_MODE)
                // ★ポイント 3 ★ パスワードから鍵を生成する場合は、Salt を使用する
                // ★ポイント 4 ★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
                // ★ポイント 5 ★ 十分安全な長さを持つ鍵を利用する
                keySpec = PBEKeySpec(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH_BITS)
                // password のクリア
                Arrays.fill(password, '?')
                // 鍵を生成する
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