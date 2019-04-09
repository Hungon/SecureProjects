package com.trials.encryptionapp

import android.util.Log
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class RsaCryptoAsymmetricKey {

    // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
    // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
    // Cipher クラスの getInstance に渡すパラメータ (/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
    // サンプルでは、暗号アルゴリズム=RSA、ブロック暗号モード=NONE、パディングルール=OAEPPADDING
    // ★ポイント 3 ★ 十分安全な長さを持つ鍵を利用する
    // 鍵長チェック

    fun encrypt(plain: ByteArray, keyData: ByteArray): ByteArray? {
        var encrypted: ByteArray? = null
        try {
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val publicKey = generatePubKey(keyData)
            if (publicKey != null) {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
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

    fun decrypt(encrypted: ByteArray?, keyData: ByteArray): ByteArray? {
        // 本来、復号処理はサーバー側で実装すべきものであるが、
        // 本サンプルでは動作確認用に、アプリ内でも復号処理を実装した。
        // 実際にサンプルコードを利用する場合は、アプリ内に秘密鍵を保持しないようにすること。
        var plain: ByteArray? = null
        try {
            // ★ポイント 1 ★ 明示的に暗号モードとパディングを設定する
            // ★ポイント 2 ★ 脆弱でない (基準を満たす) 暗号技術(アルゴリズム・モード・パディング等)を使用する
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val privateKey = generatePriKey(keyData)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            plain = cipher.doFinal(encrypted)
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: NoSuchPaddingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: IllegalBlockSizeException) {
        } catch (e: BadPaddingException) {
        } finally {
        }
        return plain
    }


    companion object {

        private val TAG = RsaCryptoAsymmetricKey::class.java.simpleName
        private const val TRANSFORMATION = "RSA/NONE/OAEPPADDING" // 暗号アルゴリズム
        private const val KEY_ALGORITHM = "RSA"
        private const val MIN_KEY_LENGTH = 2000

        private fun generatePubKey(keyData: ByteArray): PublicKey? {
            var publicKey: PublicKey? = null
            var keyFactory: KeyFactory? = null
            try {
                keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
                publicKey = keyFactory?.generatePublic(X509EncodedKeySpec(keyData))
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
                privateKey = keyFactory!!.generatePrivate(PKCS8EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {
            } catch (e: NoSuchAlgorithmException) {
            } catch (e: InvalidKeySpecException) {
            } finally {
            }
            return privateKey
        }
    }
}