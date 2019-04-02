package com.trials.loginapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.accounts.AccountAuthenticatorActivity
import android.os.Build
import android.text.InputType
import android.view.Window
import android.accounts.AccountManager
import android.support.v4.app.NotificationCompat.getExtras
import android.content.Intent
import android.accounts.Account
import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.view.View


class LoginActivity : AccountAuthenticatorActivity() {

    private var reAuthName: String? = null
    private lateinit var nameEdit: EditText
    private lateinit var passEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        requestWindowFeature(Window.FEATURE_LEFT_ICON)
        setContentView(R.layout.login_activity)
        window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert)
        nameEdit = findViewById (R.id.input_user_name)
        passEdit = findViewById (R.id.input_password)
        reAuthName = intent.getStringExtra(Authenticator.RE_AUTH_NAME)
        if (reAuthName != null) {
            nameEdit.setText(reAuthName)
            nameEdit.inputType = InputType.TYPE_NULL
            nameEdit.isFocusable = false
            nameEdit.isEnabled = false
        }
    }

    fun handleLogin(view: View) {
        val name = nameEdit.text.toString()
        val pass = passEdit.text.toString()
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pass)) {
            setResult(RESULT_CANCELED)
            finish()
        }
        val web = WebService()
        val authToken = web.login(name, pass)
        if (TextUtils.isEmpty(authToken)) {
            setResult(RESULT_CANCELED)
            finish()
        }
        Log.i(TAG, "WebService login succeeded")
        if (reAuthName == null) {
            val am = AccountManager.get(this)
            val account = Account(name, Authenticator.TRIALS_ACCOUNT_TYPE)
            am.addAccountExplicitly(account, null, null)
            am.setAuthToken(account, Authenticator.TRIALS_AUTHTOKEN_TYPE, authToken)
            val intent = Intent()
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, name)
            intent.putExtra(
                AccountManager.KEY_ACCOUNT_TYPE,
                Authenticator.TRIALS_ACCOUNT_TYPE
            )
            setAccountAuthenticatorResult(intent.extras)
            setResult(Activity.RESULT_OK, intent)
        } else {
            val bundle = Bundle()
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, name)
            bundle.putString(
                AccountManager.KEY_ACCOUNT_TYPE,
                Authenticator.TRIALS_ACCOUNT_TYPE
            )
            bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            setAccountAuthenticatorResult(bundle)
            setResult(Activity.RESULT_OK)
        }
        finish()
    }

    companion object {
        private val TAG = AccountAuthenticatorActivity::class.java.simpleName
    }
}
