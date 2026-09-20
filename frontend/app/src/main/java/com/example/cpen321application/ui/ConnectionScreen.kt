package com.example.cpen321application.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.cpen321application.BuildConfig
import com.example.cpen321application.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.time.OffsetDateTime
import java.util.Locale

private const val NOT_LOADED = "—"
private const val FAILED = "Failed"
private const val BACKEND_NAME_ROUTE = "/api/name"
private const val SERVER_IP_ROUTE = "/api/server-ip"
private const val CLIENT_IP_ROUTE = "/api/client-ip"
private const val SERVER_TIME_ROUTE = "/api/server-time"

@Composable
fun ConnectionScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authStatus by remember { mutableStateOf("Not signed in yet") }

    var googleName by remember { mutableStateOf(NOT_LOADED) }
    var backendName by remember { mutableStateOf(NOT_LOADED) }
    var serverIp by remember { mutableStateOf(NOT_LOADED) }
    var clientIp by remember { mutableStateOf(NOT_LOADED) }
    var serverTime by remember { mutableStateOf(NOT_LOADED) }
    var clientTime by remember { mutableStateOf(NOT_LOADED) }

    // Create a singleton OkHttpClient and remember it to avoid creating a new one on each call
    val httpClient = remember { OkHttpClient() }

    val onSignInClick: () -> Unit = {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(serverClientId = BuildConfig.GOOGLE_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setNonce(nonce = null)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                handleSignInResult(result) { status ->
                    authStatus = status
                    Log.d("GoogleSignIn", status)
                }

                val googleAccountInfo = extractGoogleUser(result)

                if (googleAccountInfo != null) {
                    googleName = "${googleAccountInfo.givenName} ${googleAccountInfo.familyName}"

                    val now = OffsetDateTime.now()
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'xxx", Locale.CANADA)
                    clientTime = now.format(formatter)

                    try {
                        val backendNameRaw = requestData(httpClient, BACKEND_NAME_ROUTE)
                        backendName = backendNameRaw.getString("firstName") + " " + backendNameRaw.getString("lastName")

                        // 2. Fetch Server IP
                        val serverIpRaw = requestData(httpClient, SERVER_IP_ROUTE)
                        serverIp = serverIpRaw.getString("ip")

                        // 3. Fetch Client IP
                        val clientIpRaw = requestData(httpClient, CLIENT_IP_ROUTE)
                        clientIp = clientIpRaw.getString("ip")

                        // 4. Fetch Server Time
                        val serverTimeRaw = requestData(httpClient, SERVER_TIME_ROUTE)
                        serverTime = serverTimeRaw.getString("time")
                    } catch (err: Exception) {
                        Log.e("GoogleSignIn", "Error fetching data connection information: , ${err.message}")
                        backendName = FAILED
                        serverIp = FAILED
                        clientIp = FAILED
                        serverTime = FAILED
                    }

                } else {
                    authStatus = "Sign-in failed: No Google ID token"
                    googleName = FAILED
                    backendName = FAILED
                    serverIp = FAILED
                    clientIp = FAILED
                    serverTime = FAILED
                    clientTime = FAILED
                }

            } catch (e: GetCredentialException) {
                authStatus = "Sign-in failed: ${e.message}"
                Log.e("GoogleSignIn", "GetCredentialException", e)
            } catch (e: Exception) {
                authStatus = "Sign-in failed: ${e.message}"
                Log.e("GoogleSignIn", "Exception", e)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Authentication",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoogleSignInButton(onClick = onSignInClick)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = authStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connection Information",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            InfoRow(label = "Server public IP", value = serverIp)
            InfoRow(label = "Client IP", value = clientIp)
            InfoRow(label = "Server local time", value = serverTime)
            InfoRow(label = "Client local time", value = clientTime)
            InfoRow(label = "Name (back end)", value = backendName)
            InfoRow(label = "Name (Google account)", value = googleName)
        }
    }
}

/**
 * One labelled field. Label on the left, value on the right, so the six rows line up
 * into a readable column — M1 deducts marks for a messy screen.
 */
@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Same styling as the first button on MainScreen. If you want to stop maintaining two
 * copies, move this into its own file and have MainScreen call it too.
 */
@Composable
private fun GoogleSignInButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun handleSignInResult(result: GetCredentialResponse, onStatusUpdate: (String) -> Unit) {
    when (val credential = result.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val displayName = googleIdTokenCredential.displayName
                    val email = googleIdTokenCredential.id

                    onStatusUpdate("Signed in as: $displayName ($email)")
                } catch (e: Exception) {
                    onStatusUpdate("Failed to parse Google ID token: ${e.message}")
                    Log.e("GoogleSignIn", "Parsing error", e)
                }
            } else {
                onStatusUpdate("Unexpected credential type: ${credential.type}")
            }
        }
        else -> {
            onStatusUpdate("Unexpected credential type")
        }
    }
}

private fun extractGoogleUser(result: GetCredentialResponse): GoogleIdTokenCredential? {
    val credential = result.credential as? CustomCredential
    if (credential?.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {return null}

    return runCatching {
        GoogleIdTokenCredential.createFrom(credential.data)
    }.getOrNull()
}

private suspend fun requestData(client: OkHttpClient, route: String): JSONObject {

    val fullUrl = BuildConfig.API_BASE_URL + route

    // withContext means: change to the IO pool ,execute this block, wait here until finished and then
    // return to the thread I was in before
    return withContext(Dispatchers.IO) {
        val request = okhttp3.Request.Builder().url(fullUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val responseData = response.body?.string()

            if (responseData == null) {throw Exception("No response data")}

            // No "return" keyword because we are inside a lambda function
            JSONObject(responseData)
        }
    }
}
