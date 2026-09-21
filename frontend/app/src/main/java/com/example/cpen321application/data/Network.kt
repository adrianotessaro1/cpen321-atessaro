package com.example.cpen321application.data

import com.example.cpen321application.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * This function makes a request to the given URL and returns the response body.
 * 
 * @param client The OkHttpClient to use for the request.
 * @param url The URL to make the request to.
 * @return The response body.
 */
internal suspend fun requestBody(client: OkHttpClient, url: String): String {

    // withContext means: change to the IO pool ,execute this block, wait here until finished and then
    // return to the thread I was in before
    return withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val responseData = response.body?.string()

            if (responseData == null) {
                throw Exception("No response data")
            }

            responseData
        }
    }
}

/**
 * This function makes a request to the given URL and returns the response body as a JSONObject.
 * 
 * @param client The OkHttpClient to use for the request.
 * @param url The URL to make the request to.
 * @return The response body as a JSONObject.
 */
internal suspend fun requestData(client: OkHttpClient, url: String): JSONObject =
    JSONObject(requestBody(client, url))