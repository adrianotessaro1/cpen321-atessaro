package com.example.cpen321application.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.cpen321application.data.requestBody
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import org.json.JSONArray

private const val GOOD_NEWS_URL =
    "https://www.goodnewsnetwork.org/wp-json/wp/v2/posts?per_page=5&_fields=title,link,date"

// Shown when the Good News Network API cannot be reached. 
private val FALLBACK_HEADLINES = listOf(
    Headline(
        "New AI Model Can Detect Heart Disease Markers in Simple Electrocardiogram Readings\u2013a Breakthrough",
        "https://www.goodnewsnetwork.org/new-ai-model-can-detect-heart-disease-markers-in-simple-electrocardiogram-readings-a-breakthrough/"
    ),
    Headline(
        "Rural French Mayors Rally to Save Their Local Cafe-Bistro, and Help Reverse National Trend",
        "https://www.goodnewsnetwork.org/rural-french-mayors-rally-to-save-their-local-cafe-bistro-and-help-reverse-national-trend/"
    ),
    Headline(
        "Korean Drone Rescue Program Helps Mozambique Achieve Zero Fatalities During Worst Flood in Decades",
        "https://www.goodnewsnetwork.org/korean-drone-rescue-program-helps-mozambique-achieve-zero-fatalities-during-worst-flood-in-decades/"
    ),
    Headline(
        "Historic Philanthropic Gift Will Expand Veterinary Care Access to Millions of Americans and Their Pets",
        "https://www.goodnewsnetwork.org/historic-philanthropic-gift-will-expand-veterinary-care-access-to-millions-of-americans-and-their-pets/"
    ),
    Headline(
        "\u201cEducation is the key to unlock the golden door of freedom.\u201d \u2013 George Washington Carver",
        "https://www.goodnewsnetwork.org/george-washington-carver-quote-about-education/"
    )
)

@Composable
fun SurpriseScreen(modifier: Modifier = Modifier) {

    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }

    var remainingSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // Null when the input is acceptable; a message to show when it is not.
    var inputError by remember { mutableStateOf<String?>(null) }

    // Status of the news section
    var newsStatus by remember { mutableStateOf("") }
    val headlines = remember { mutableStateListOf<Headline>() }

    val httpClient = remember { OkHttpClient() }

    // Needed to hand the article URL to whatever browser the device has.
    val context = LocalContext.current

    // LaunchedEffect is used to run code when the isRunning state changes
    LaunchedEffect(isRunning) {
        // If the timer is not running, return
        if (!isRunning) return@LaunchedEffect

        // While the timer is running, decrement the remaining seconds
        while (remainingSeconds > 0) {
            // Wait for 1 second
            delay(1000)
            remainingSeconds--
        }

        // Once the timer is done, load the news
        newsStatus = "Loading some good news for you..."
        delay(1000)
        headlines.clear()

        try {
            // Get the news from the backend
            val array = JSONArray(requestBody(httpClient, GOOD_NEWS_URL))

            // Loop through the news and add it to the headlines list
            for (i in 0 until array.length()) {
                // Get the news from the array
                val post = array.getJSONObject(i)

                // Add the news to the headlines list
                headlines.add(
                    Headline(
                        title = post.getJSONObject("title").getString("rendered"),
                        link = post.getString("link")
                    )
                )
            }

            newsStatus = ""
        } catch (err: Exception) {
            // The feed is a third-party host; if it is unreachable, fall back to the saved
            // copies rather than leaving the section empty.
            headlines.addAll(FALLBACK_HEADLINES)
            newsStatus = "Could not reach the news service, showing saved headlines. (${err.message})"
        }

        isRunning = false

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
                text = "Set a Timer",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minutesInput,
                    onValueChange = { minutesInput = it },
                    label = { Text("Minutes") },
                    singleLine = true,
                    enabled = !isRunning,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedTextField(
                    value = secondsInput,
                    onValueChange = { secondsInput = it },
                    label = { Text("Seconds") },
                    singleLine = true,
                    enabled = !isRunning,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            inputError?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = DateUtils.formatElapsedTime(remainingSeconds.toLong()),
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Convert the input to minutes and seconds
                        val minutes = if (minutesInput.isBlank()) 0 else minutesInput.toIntOrNull()
                        val seconds = if (secondsInput.isBlank()) 0 else secondsInput.toIntOrNull()
                        val totalSeconds = if (minutes == null || seconds == null) null else minutes * 60 + seconds

                        // Check if the input is valid
                        when {
                            totalSeconds == null -> inputError = "Enter whole numbers please"
                            totalSeconds <= 0 -> inputError = "Enter something greater than zero"
                            else -> {
                                inputError = null
                                remainingSeconds = totalSeconds
                                isRunning = true
                            }
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Start")
                }

                OutlinedButton(
                    onClick = {
                        minutesInput = "";
                        secondsInput = "";
                        remainingSeconds = 0;
                        isRunning = false;
                        newsStatus = "";
                        headlines.clear();
                        inputError = null;
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Reset")
                }
            }

            // If the news status is not empty or the headlines list is not empty, show the news section
            if (newsStatus.isNotEmpty() || headlines.isNotEmpty()) {

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Good News to Boost Your Day !!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (newsStatus.isNotEmpty()) {
                    Text(
                        text = newsStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Iterate through the headlines and show them wiht a smiley face icon and a link to the article
                headlines.forEach { headline ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, headline.link.toUri())
                                    )
                                } catch (err: ActivityNotFoundException) {
                                    newsStatus = "No app on this device can open the article"
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "\uD83D\uDE0A",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = headline.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

private data class Headline(val title: String, val link: String)
