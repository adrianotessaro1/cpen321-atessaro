package com.example.cpen321application.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cpen321application.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.WebSocketListener
import org.json.JSONObject

private const val GRID_SIZE = 16
private const val WS_ROUTE = "/ws"

@Composable
fun PictureScreen(modifier: Modifier = Modifier) {

    // The grid of colors
    val grid = remember {
        mutableStateListOf<Color>().apply {
            repeat(GRID_SIZE * GRID_SIZE) { add(Color.White) }
        }
    }

    var connectionStatus by remember { mutableStateOf("Not connected") }

    val httpClient = remember { OkHttpClient() }

    // DisposableEffect is used to clean up the websocket connection when the composable is disposed
    DisposableEffect (Unit) {
        // Where the websocket will connect to
        val request = okhttp3.Request.Builder().url(BuildConfig.API_BASE_URL + WS_ROUTE).build()

        // What the websocket will do
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                connectionStatus = "Connected"
            }

            // Messages will come in the following format:
            // {"x":14,"y":10,"color":"#FFFFFF"}
            // {"x":6,"y":9,"color":"#3a2a1a"}
            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {

                try {
                    val json = JSONObject(text)
                    val x = json.getInt("x")
                    val y = json.getInt("y")
                    val color = json.getString("color")
                    val parsedColor = Color(android.graphics.Color.parseColor(color))

                    grid[y * GRID_SIZE + x] = parsedColor
                } catch (err: Exception) {
                    println("Error parsing message: $err")
                }

            }

            override fun onFailure(
                webSocket: okhttp3.WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                connectionStatus = "Connection failed"
            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                connectionStatus = "Connection closed"
            }
        }

        val websocket = httpClient.newWebSocket(request, listener)

        onDispose {
            websocket.close(1000, null)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pixel Art",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            PixelGrid(
                cells = grid,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = connectionStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Paints the grid as one Canvas of 256 rectangles.
 */
@Composable
private fun PixelGrid(cells: List<Color>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = size.width / GRID_SIZE

        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                drawRect(
                    color = cells[y * GRID_SIZE + x],
                    topLeft = Offset(x * cell, y * cell),
                    size = Size(cell, cell)
                )
            }
        }
    }
}
