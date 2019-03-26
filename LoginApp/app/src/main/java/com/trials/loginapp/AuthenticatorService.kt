package com.trials.loginapp

import android.app.Service
import android.content.Intent
import android.os.IBinder


class AuthenticationService : Service() {
    private var mAuthenticator: Authenticator? = null
    fun onCreate() {
        mAuthenticator = Authenticator(this)
    }

    fun onBind(intent: Intent): IBinder {
        return mAuthenticator!!.getIBinder()
    }
}