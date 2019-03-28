package com.trials.loginapp

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {


    private fun accountExist(account: Account): Boolean {
        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType(TRIALS_ACCOUNT_TYPE)
        for (ac in accounts) {
            if (ac == account) {
                return true
            }
        }
        return false
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return TRIALS_AUTHTOKEN_LABEL
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val bundle = Bundle();
        if (accountExist(account)) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(RE_AUTH_NAME, account?.name)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        } else {
            bundle.putString(AccountManager.KEY_ERROR_CODE, String.valueOf(-2))
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, context.getString(R.string.error_account_not_exists));
        }
        return bundle
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return result
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType(TRIALS_ACCOUNT_TYPE);
        val bundle = Bundle()
        if (accounts.isNotEmpty()) {
            context.getString(R.string.error_account_exists))
        } else {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        }
        return bundle
    }

    companion object {
        val TRIALS_ACCOUNT_TYPE = "com.trials.loginapp.accountmanager"
        val TRIALS_AUTHTOKEN_TYPE = "webservice"
        val TRIALS_AUTHTOKEN_LABEL = "Trials Web Service"
        val RE_AUTH_NAME = "reauth_name"
    }
}