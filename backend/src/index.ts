import { createApp } from './app';
import { env } from './config/env';
import { WebSocket, RawData, WebSocketServer } from 'ws';

const WEBSOCKET_PATH = '/ws';
const WEBSOCKET_UPSTREAM_URL = 'wss://8.229.22.124';

const app = createApp();

// Create the server, register Express to handle the HTTP requests and return the server instance
const server = app.listen(env.port, () => {
  console.log(`Server listening on port ${env.port}`);
});

const webSocketServer = new WebSocketServer({ server, path: WEBSOCKET_PATH });

function connectToWebSocketUpstream() {
  const ws = new WebSocket(WEBSOCKET_UPSTREAM_URL);

  ws.on('open', () => {
    console.log('WebSocket connection established');
  });

  ws.on('message', (data: RawData, isBinary: boolean): void => {
    // Iterate over all clients
    for (const client of webSocketServer.clients) {
      // Check if the client is open
      if (client.readyState === WebSocket.OPEN) {
        // Send the message to the client 
        client.send(data, { binary: isBinary });
      }
    }
  });

  ws.on('error', (err) => {
    console.error(`WebSocket error: ${err}`);
  });

  ws.on('close', () => {
    console.log('WebSocket connection closed, trying to reconnect...');
    setTimeout(() => {
      connectToWebSocketUpstream();
    }, 1000);
  });
}

connectToWebSocketUpstream();

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    server.close(() => {
      process.exit(0);
    });
  });
}
