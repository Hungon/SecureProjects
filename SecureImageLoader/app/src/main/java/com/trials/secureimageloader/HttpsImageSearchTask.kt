package com.trials.secureimageloader

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.SSLException


abstract class HttpsImageSearchTask : AsyncTask<String, Void, Any?>() {

    @Throws(IOException::class)
    private fun checkResponse(response: HttpURLConnection) {
        val statusCode = response.responseCode
        if (HttpURLConnection.HTTP_OK != statusCode) {
            throw IOException("HttpStatus: $statusCode")
        }
    }

    private fun getByteArray(strUrl: String): ByteArray? {
        val buff = ByteArray(1024)
        var result: ByteArray? = null
        val response: HttpURLConnection
        var inputStream: BufferedInputStream? = null
        var responseArray: ByteArrayOutputStream? = null
        var length = 0
        try {
            val url = URL(strUrl)
            response = url.openConnection() as HttpURLConnection
            response.requestMethod = "GET"
            response.connect()
            checkResponse(response)
            inputStream = BufferedInputStream(response.inputStream)
            responseArray = ByteArrayOutputStream()
            while (length != -1) {
                length = inputStream.read(buff)
                if (length > 0) responseArray.write(buff, 0, length)
            }
            result = responseArray.toByteArray()
        } catch(e: SSLException) {
            Log.e(TAG, e.message)
        } catch (e: IOException) {
            Log.e(TAG, e.message)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                try {
                    responseArray?.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.message)
                }
            }
        }
        return result
    }

    public override fun doInBackground(vararg params: String?): Any? {
        var responseArray: ByteArray?
        // Search image
        // not include sensitive info
        val s = StringBuilder()
        for (param in params) {
            s.append(param)
            s.append('+')
        }
        s.deleteCharAt(s.length - 1)
        val search_url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + s.toString()
        responseArray = getByteArray(search_url)
        if (responseArray == null) {
            return null
        }
        // Ensure received data is right data
        var image_url = ""
        try {
            val json = String(responseArray)
            image_url = JSONObject(json).getJSONObject("responseData").getJSONArray("results").getJSONObject(0)
                .getString("url")
        } catch (e: JSONException) {
            return e
        }
        // Get image with https
        // it can contain sensitive info
        if (image_url != null) {
            responseArray = getByteArray(image_url)
            if (responseArray == null) {
                return null
            }
        }
        return responseArray
    }

    companion object {
        private val TAG = HttpsImageSearchTask::class.java.simpleName
    }

}
