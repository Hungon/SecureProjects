package com.trials.encryptionapp

import android.provider.SyncStateContract.Helpers.update
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


class RsaSignAsymmetricKey {


    fun sign(plain: ByteArray, keyData: ByteArray): ByteArray? {
        // 本来、署名処理はサーバー側で実装すべきものであるが、
        // 本サンプルでは動作確認用に、アプリ内でも署名処理を実装した。
        // 実際にサンプルコードを利用する場合は、アプリ内に秘密鍵を保持しないようにすること。
        var sign: ByteArray? = null
        try {
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val signature = Signature.getInstance(TRANSFORMATION)
            val privateKey = generatePriKey(keyData)
            signature.initSign(privateKey)
            signature.update(plain)
            sign = signature.sign()
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: InvalidKeyException) {
        } catch (e: SignatureException) {
        } finally {
        }
        return sign
    }

    fun verify(sign: ByteArray?, plain: ByteArray, keyData: ByteArray): Boolean {
        var ret = false
        try {
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val signature = Signature.getInstance(TRANSFORMATION)
            val publicKey = generatePubKey(keyData)
            signature.initVerify(publicKey)
            signature.update(plain)
            ret = signature.verify(sign)
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: InvalidKeyException) {
        } catch (e: SignatureException) {
        } finally {
        }
        return ret
    }

    companion object {
        private val TAG = RsaSignAsymmetricKey::class.java.simpleName
        // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
        // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
        // Cipher クラスの getInstance に渡すパラメータ (/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
        // サンプルでは、暗号アルゴリズム=RSA、ブロック暗号モード=NONE、パディングルール=OAEPPADDING
        private const val TRANSFORMATION = "SHA256withRSA"
        // 暗号アルゴリズム
        private const val KEY_ALGORITHM = "RSA"
        // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する // 鍵長チェック
        private const val MIN_KEY_LENGTH = 2000

        private fun generatePubKey(keyData: ByteArray): PublicKey? {
            var publicKey: PublicKey? = null
            var keyFactory: KeyFactory? = null
            try {
                keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
                publicKey = keyFactory!!.generatePublic(X509EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {
            } catch (e: NoSuchAlgorithmException) {
            } catch (e: InvalidKeySpecException) {
            } finally {
                if (publicKey == null) {
                    Log.e(TAG, "generatePubKey() publicKey is null")
                    return null
                }
            }
            // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する // 鍵長のチェック
            if (publicKey is RSAPublicKey) {
                val len = publicKey.modulus.bitLength()
                if (len < MIN_KEY_LENGTH) {
                    publicKey = null
                }
            }
            return publicKey
        }

        private fun generatePriKey(keyData: ByteArray): PrivateKey? {
            var privateKey: PrivateKey? = null
            var keyFactory: KeyFactory? = null
            try {
                keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
                privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {

            } catch (e: NoSuchAlgorithmException) {

            } catch (e: InvalidKeySpecException) {

            } finally {
            }
            return privateKey
        }
    }
}