package com.trials.loginpageapp

import android.accounts.*
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.io.IOException


class UserActivity : AppCompatActivity() {

    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        logView = findViewById(R.id.text_log_view)
    }

    fun addAccount(view: View) {
        logLine()
        logLine("Add new account")
        if (!checkAuthenticator()) return
        val am = AccountManager.get(this)
        am.addAccount(
            TRIALS_ACCOUNT_TYPE, TRIALS_TOKEN_TYPE, null, null, this,
            { future ->
                try {
                    val result = future.result
                    val type = result.getString(AccountManager.KEY_ACCOUNT_TYPE)
                    val name = result.getString(AccountManager.KEY_ACCOUNT_NAME)
                    if (type != null && name != null) {
                        logLine("Added following account")
                        logLine("Account type: %s", type)
                        logLine("Account name: %s", name)
                    } else {
                        val code = result.getString(AccountManager.KEY_ERROR_CODE)
                        val msg = result.getString(AccountManager.KEY_ERROR_MESSAGE)
                        logLine("Could not add the account")
                        logLine("Error code゙ %s: %s", code, msg)
                    }
                } catch (e: OperationCanceledException) {
                } catch (e: AuthenticatorException) {
                } catch (e: IOException) {
                }
            }, null
        )
    }

    fun getAuthToken() {
        logLine()
        logLine("Retrieve token")
        if (!checkAuthenticator()) return
        val am = AccountManager.get(this)

        val accounts = am.getAccountsByType(TRIALS_ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            val account = accounts[0]
            am.getAuthToken(
                account, TRIALS_TOKEN_TYPE, null, this,
                { future ->

                    try {
                        val result = future.result
                        val name = result.getString(AccountManager.KEY_ACCOUNT_NAME)
                        val authtoken = result.getString(AccountManager.KEY_AUTHTOKEN)
                        logLine(" %s's token:", name)
                        if (authtoken != null) {
                            logLine(" %s", authtoken); } else {
                            logLine("Could not get authentication"); }
                    } catch (e: OperationCanceledException) {
                        logLine("Exception: %s", e.javaClass.name)
                    } catch (e: AuthenticatorException) {
                        logLine("Exception: %s", e.javaClass.name)
                    } catch (e: IOException) {
                        logLine("Exception: %s", e.javaClass.name)
                    }
                }, null
            )
        } else {
            logLine("The account has not been registered")
        }
    }

    private fun checkAuthenticator(): Boolean {
        val am = AccountManager.get(this)
        var pkgname: String? = null
        for (ad in am.authenticatorTypes) {
            if (TRIALS_ACCOUNT_TYPE == ad.type) {
                pkgname = ad.packageName
                break
            }
        }
        if (pkgname == null) {
            logLine("Could not find authenticator")
            return false
        }
        logLine(" Account type: %s", TRIALS_ACCOUNT_TYPE)
        logLine(" Authenticator's Package name: $pkgname")
        if (!PackageCertification.test(this, pkgname, getTrustedCertificateHash())) {
            logLine("Invalid certification")
            return false
        }
        logLine("Valid certification")
        return true
    }

    private fun getTrustedCertificateHash(): String {
        return if (BuildConfig.DEBUG) {
            // hash code of android.debug as sha256
            "fb57f3c56192d21dc44bbeca2aae33038e630ce3967c0cf9da6d3d2e40c1908d"
        } else {
            // hash code of the project as sha256
            "1210c7e8195664bf3d49ceeecf9f53d1256f4c5cafd43fb27166fd4c44e1c417"
        }
    }

    private fun log(str: String) {
        logView.append(str)
    }

    private fun logLine(line: String = "\n") {
        log(line + "\n")
    }

    private fun logLine(fmt: String, vararg args: Any) {
        logLine(String.format(fmt, *args))
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName
        const val TRIALS_ACCOUNT_TYPE = "com.trials.loginapp.accountmanager"
        const val TRIALS_TOKEN_TYPE = "webservice"
    }
}
