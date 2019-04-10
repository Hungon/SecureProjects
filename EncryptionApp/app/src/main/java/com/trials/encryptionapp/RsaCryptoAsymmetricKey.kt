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

    fun encrypt(plain: ByteArray, keyData: ByteArray): ByteArray? {
        var encrypted: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val publicKey = generatePubKey(keyData)
            if (publicKey != null) {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                encrypted = cipher.doFinal(plain)
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "encrypt() ${e.message}")
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG, "encrypt() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "encrypt() ${e.message}")
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, "encrypt() ${e.message}")
        } catch (e: BadPaddingException) {
            Log.e(TAG, "encrypt() ${e.message}")
        } finally {
        }
        return encrypted
    }

    // Decryption must be implemented on server side, but for sample it decrypts on client side
    fun decrypt(encrypted: ByteArray?, keyData: ByteArray): ByteArray? {
        var plain: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val privateKey = generatePriKey(keyData)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            plain = cipher.doFinal(encrypted)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "decrypt() ${e.message}")
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG, "decrypt() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "decrypt() ${e.message}")
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, "decrypt() ${e.message}")
        } catch (e: BadPaddingException) {
            Log.e(TAG, "decrypt() ${e.message}")
        } finally {
        }
        return plain
    }


    companion object {

        private val TAG = RsaCryptoAsymmetricKey::class.java.simpleName
        private const val TRANSFORMATION = "RSA/NONE/OAEPPADDING"
        private const val KEY_ALGORITHM = "RSA"
        private const val MIN_KEY_LENGTH = 2000

        private fun generatePubKey(keyData: ByteArray): PublicKey? {
            var publicKey: PublicKey? = null
            var keyFactory: KeyFactory? = null
            try {
                keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
                publicKey = keyFactory?.generatePublic(X509EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "generatePubKey() ${e.message}")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG, "generatePubKey() ${e.message}")
            } catch (e: InvalidKeySpecException) {
                Log.e(TAG, "generatePubKey() ${e.message}")
            } finally {
                if (keyFactory == null) Log.e(TAG, "generatePubKey() keyFactory is null")
                if (publicKey == null) {
                    Log.e(TAG, "generatePubKey() publicKey is null")
                    return null
                }
            }
            if (publicKey is RSAPublicKey) {
                val len = publicKey.modulus.bitLength()
                if (len < MIN_KEY_LENGTH) {
                    Log.e(TAG, "$len Key length is insufficient. key length must be more than $MIN_KEY_LENGTH")
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
                Log.e(TAG, "generatePriKey() ${e.message}")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG, "generatePriKey() ${e.message}")
            } catch (e: InvalidKeySpecException) {
                Log.e(TAG, "generatePriKey() ${e.message}")
            } finally {
                if (keyFactory == null) Log.e(TAG, "generatePriKey() keyFactory is null")
                if (privateKey == null) {
                    Log.e(TAG, "generatePriKey() privateKey is null")
                    return null
                }
            }
            return privateKey
        }
    }
}