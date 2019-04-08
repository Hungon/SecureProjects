package com.trials.secureimageloader

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.ImageView


class MainActivity : AppCompatActivity() {

    private lateinit var queryBox: EditText
    private lateinit var searchButtonWithHttp: Button
    private lateinit var searchButtonWithHttps: Button
    private lateinit var msgBox: TextView
    private lateinit var imgBox: ImageView
    private var asyncTask: AsyncTask<String, Void, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queryBox = findViewById(R.id.edit_query)
        searchButtonWithHttp = findViewById(R.id.button_search_with_http)
        searchButtonWithHttps = findViewById(R.id.button_search_with_https)
        msgBox = findViewById(R.id.text_message)
        imgBox = findViewById(R.id.image_result)

        searchButtonWithHttp.setOnClickListener {
            onHttpSearchClick()
        }

        searchButtonWithHttps.setOnClickListener {
            onHttpsSearchClick()
        }
    }

    override fun onPause() {
        asyncTask?.cancel(true)
        super.onPause()
    }

    private fun onHttpSearchClick() {
        val query = queryBox.text.toString()
        msgBox.text = String.format("HTTP:%s", query)
        imgBox.setImageBitmap(null)
        asyncTask?.cancel(true)
        asyncTask = object : HttpImageSearchTask() {

            override fun onPostExecute(result: Any?) {
                when (result) {
                    null -> {
                        msgBox.append("\nException happened\n")
                    }
                    is Exception -> {
                        msgBox.append("\nException happened\n  $result")
                    }
                    else -> {
                        val data = result as ByteArray
                        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                        imgBox.setImageBitmap(bmp)
                    }

                }
            }
        }.execute(query)
    }

    private fun onHttpsSearchClick() {
        val query = queryBox.text.toString()
        msgBox.text = String.format("HTTPS:%s", query)
        imgBox.setImageBitmap(null)
        asyncTask?.cancel(true)
        asyncTask = object : HttpsImageSearchTask() {
            override fun onPostExecute(result: Any?) {
                if (result != null) {
                    if (result is Exception) {
                        msgBox.append("\nException happened\n$result")
                    } else {
                        val data = result as ByteArray
                        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                        imgBox.setImageBitmap(bmp)
                    }
                }
            }
        }.execute(query)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
