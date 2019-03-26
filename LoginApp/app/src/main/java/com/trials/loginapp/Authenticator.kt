package com.trials.loginapp

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.content.Context
import android.os.Bundle

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {


    override fun getAuthTokenLabel(authTokenType: String?): String {

    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
    }

    companion object {
        val JSSEC_ACCOUNT_TYPE = "org.jssec.android.accountmanager"
        val JSSEC_AUTHTOKEN_TYPE = "webservice"
        val JSSEC_AUTHTOKEN_LABEL = "JSSEC Web Service"
        val RE_AUTH_NAME = "reauth_name"
    }
}