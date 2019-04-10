package com.trials.encryptionapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class MainActivity : AppCompatActivity() {

    private var selectedRadioId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_encrypt.setOnClickListener {
            doEncryption()
        }
    }

    private fun checkForm(text: String, pass: String): Boolean {
        val mes = if (text.isEmpty() && pass.isEmpty()) {
            "plain text and password"
        } else {
            if (text.isEmpty()) {
                "plain text"
            } else if (pass.isEmpty()) {
                "password"
            } else {
                return true
            }
        }
        if (mes.isNotEmpty()) {
            Toast.makeText(
                this,
                String.format(getString(R.string.toast_found_empty, mes)), Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    private fun doEncryption() {
        val plainText = edit_plain_text.text.toString()
        val password = edit_password.text.toString()
        if (checkForm(plainText, password)) {
            when (selectedRadioId) {
                R.id.radio_aes -> {
                    val cryption = AesCryptoPBEKey()
                    val encrypted = cryption.encrypt(plainText.toByteArray(), password.toCharArray())
                    val decrypted = cryption.decrypt(encrypted, password.toCharArray())
                    text_result.text = decrypted.toString()
                }
/*
                R.id.radio_aes_shared -> {
                    val aesCryptoSharedKey = AesCryptoPreSharedKey()
                    val encrypted = aesCryptoSharedKey.encrypt(password.toByteArray(), plainText.toByteArray())
                    val decrypted = aesCryptoSharedKey.decrypt(password.toByteArray(), encrypted)
                    text_result.text = decrypted.toString()
                }
*/
                R.id.radio_hmac -> {
                    val cryption = HmacPBEKey()
                    val encrypted = cryption.sign(plainText.toByteArray(), password.toCharArray())
                    val decrypted = cryption.verify(encrypted, plainText.toByteArray(), password.toCharArray())
                    text_result.text = decrypted.toString()
                }
                R.id.radio_hmac_shared -> {
                    val cryption = HmacPreSharedKey()
                    val encrypted = cryption.sign(plainText.toByteArray(), password.toByteArray())
                    val decrypted = cryption.verify(encrypted, plainText.toByteArray(), password.toByteArray())
                    text_result.text = decrypted.toString()
                }
                R.id.radio_rsa -> {
                    val keyPair = GenerateKeyPair.generate(4048)
                    val cryption = RsaCryptoAsymmetricKey()
                    val encrypted = cryption.encrypt(plainText.toByteArray(), keyPair.public.encoded)
                    val decrypted = cryption.decrypt(encrypted, keyPair.private.encoded)
                    text_result.text = decrypted.toString()
                }
                R.id.radio_rsa_sign -> {
                    val keyPair = GenerateKeyPair.generate(4048)
                    val cryption = RsaSignAsymmetricKey()
                    val encrypted = cryption.sign(plainText.toByteArray(), keyPair.private.encoded)
                    val decrypted = cryption.verify(encrypted, plainText.toByteArray(), keyPair.public.encoded)
                    text_result.text = decrypted.toString()
                }
                else -> {
                    Log.e(TAG, "invalid radio button")
                }
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            // Check which radio button was clicked
            when (view.id) {
                R.id.radio_aes,
//                R.id.radio_aes_shared,
                R.id.radio_hmac,
                R.id.radio_hmac_shared,
                R.id.radio_rsa,
                R.id.radio_rsa_sign ->
                    if (checked) {
                        selectedRadioId = view.id
                    }
                else -> {
                    Log.e(TAG, "invalid radio button")
                }
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
