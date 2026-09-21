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
import com.example.cpen321application.data.requestData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
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

    // Get the current context (used to access the Android API) 
    val context = LocalContext.current

    // Create a coroutine scope to handle asynchronous operations and background tasks (separate from the main thread)
    val coroutineScope = rememberCoroutineScope()

    // Status of the authentication process and user information
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
        // Launch a coroutine to handle the asynchronous authentication process so the UI thread is not blocked
        coroutineScope.launch {
            try {
                // Create a credential manager to handle the authentication process
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    // do not filter by authorized accounts that have previously used the app
                    .setFilterByAuthorizedAccounts(false)
                    // Web Client ID on Oauth in the Google Cloud Project
                    .setServerClientId(serverClientId = BuildConfig.GOOGLE_CLIENT_ID)
                    // No need for nonce in this case since we are not performing any server side validation
                    .setNonce(nonce = null)
                    .build()

                // Create a request to get the Google ID token
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()


                // Get the Google ID token from the credential manager
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                handleSignInResult(result) { status ->
                    authStatus = status
                    Log.d("GoogleSignIn", status)
                }

                // Extract the Google account information from the result
                val googleAccountInfo = extractGoogleUser(result)

                if (googleAccountInfo != null) {
                    googleName = "${googleAccountInfo.givenName} ${googleAccountInfo.familyName}"

                    // Get the current time and format it 
                    val now = OffsetDateTime.now()
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'xxx", Locale.CANADA)
                    clientTime = now.format(formatter)

                    try {
                        // Get the backend name from the backend
                        val backendNameRaw = requestData(httpClient, BuildConfig.API_BASE_URL + BACKEND_NAME_ROUTE)
                        backendName = backendNameRaw.getString("firstName") + " " + backendNameRaw.getString("lastName")

                        // Get the server IP from the backend
                        val serverIpRaw = requestData(httpClient, BuildConfig.API_BASE_URL + SERVER_IP_ROUTE)
                        serverIp = serverIpRaw.getString("ip")

                        // Get the client IP from the backend
                        val clientIpRaw = requestData(httpClient, BuildConfig.API_BASE_URL + CLIENT_IP_ROUTE)
                        clientIp = clientIpRaw.getString("ip")

                        // Get the server time from the backend
                        val serverTimeRaw = requestData(httpClient, BuildConfig.API_BASE_URL + SERVER_TIME_ROUTE)
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
            InfoRow(label = "Name (backend)", value = backendName)
            InfoRow(label = "Name (Google account)", value = googleName)
        }
    }
}

/**
 * One labelled field. Lable on the left and value on the right.
 * 
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
        // Label on the left
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Value on the right
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Google Sign In Button with Google logo
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

/**
 * This function handles the result of the Google sign in process and updates the status of the authentication process
 * 
 * @param result The result of the Google sign in process
 * @param onStatusUpdate A function to update the status of the authentication process
 */
private fun handleSignInResult(result: GetCredentialResponse, onStatusUpdate: (String) -> Unit) {
    // Check the type of the credential
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

