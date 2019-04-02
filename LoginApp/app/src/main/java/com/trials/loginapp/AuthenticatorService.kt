package com.trials.loginapp

import android.app.Service
import android.content.Intent
import android.os.IBinder


class AuthenticationService : Service() {
    private var mAuthenticator: Authenticator? = null

    override fun onCreate() {
        mAuthenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return mAuthenticator!!.iBinder
    }
}