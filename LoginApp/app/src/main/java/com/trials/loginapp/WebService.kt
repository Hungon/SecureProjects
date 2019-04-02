package com.trials.loginapp

class WebService {

    // use name and password to connect by https
    fun login(username: String, password: String): String {
        return getAuthToken(username, password)
    }

    // for sample this function returns hardcoded value without internet.
    private fun getAuthToken(username: String, password: String): String {
        return "c2f981bda5f34f90c0419e171f60f45c"
    }
}