package com.example.sendymapdemo

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL

class URLConnector(url: String) : Thread() {
    private var result: String? = null
    private var URL1: String=url
    override fun run() {
        val output = request(URL1)
        result = output
    }

    fun getResult(): String? {
        return result
    }

    private fun request(urlStr: String): String {
        val output = StringBuilder()
        try {
            val url = URL(urlStr)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

            conn.connectTimeout = 10000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.doOutput = true

            val resCode = conn.responseCode
            if (resCode == HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                var line: String?
                while (true) {
                    line = reader.readLine()
                    if (line == null) {
                        break
                    }
                    output.append(line + "\n")
                }

                reader.close()
                conn.disconnect()
            }
        } catch (ex: Exception) {
            Log.e("HTTPConnection", "Exception in processing response.", ex)
            ex.printStackTrace()
        }

        return output.toString()
    }
}