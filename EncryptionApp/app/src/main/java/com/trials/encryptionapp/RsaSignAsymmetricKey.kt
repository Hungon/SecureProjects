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


    // This process must be implemented on server side, but for sample it decrypts on client side
    fun sign(plain: ByteArray, keyData: ByteArray): ByteArray? {
        var sign: ByteArray? = null
        try {
            val signature = Signature.getInstance(TRANSFORMATION)
            val privateKey = generatePriKey(keyData)
            signature.initSign(privateKey)
            signature.update(plain)
            sign = signature.sign()
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG,"sign() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG,"sign() ${e.message}")
        } catch (e: SignatureException) {
            Log.e(TAG,"sign() ${e.message}")
        } finally {
        }
        return sign
    }

    fun verify(sign: ByteArray?, plain: ByteArray, keyData: ByteArray): Boolean {
        var ret = false
        try {
            val signature = Signature.getInstance(TRANSFORMATION)
            val publicKey = generatePubKey(keyData)
            signature.initVerify(publicKey)
            signature.update(plain)
            ret = signature.verify(sign)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG,"verify() ${e.message}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG,"verify() ${e.message}")
        } catch (e: SignatureException) {
            Log.e(TAG,"verify() ${e.message}")
        } finally {
        }
        return ret
    }

    companion object {
        private val TAG = RsaSignAsymmetricKey::class.java.simpleName
        private const val TRANSFORMATION = "SHA256withRSA"
        private const val KEY_ALGORITHM = "RSA"
        private const val MIN_KEY_LENGTH = 2000

        private fun generatePubKey(keyData: ByteArray): PublicKey? {
            var publicKey: PublicKey? = null
            var keyFactory: KeyFactory? = null
            try {
                keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
                publicKey = keyFactory!!.generatePublic(X509EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {
                Log.e(TAG,"generatePubKey() ${e.message}")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG,"generatePubKey() ${e.message}")
            } catch (e: InvalidKeySpecException) {
                Log.e(TAG,"generatePubKey() ${e.message}")
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
                privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyData))
            } catch (e: IllegalArgumentException) {
                Log.e(TAG,"generatePriKey() ${e.message}")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG,"generatePriKey() ${e.message}")
            } catch (e: InvalidKeySpecException) {
                Log.e(TAG,"generatePriKey() ${e.message}")
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