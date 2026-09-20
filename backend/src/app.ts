import express, { type Express } from 'express';
import { env } from './config/env';

export function createApp(): Express {
  const app = express();

  // Tell Express that connection is coming from a proxy and not directly from the client
  // so we neeed to trust this proxy and read the "X-Forwarded-For" header
  app.set('trust proxy', 1);

  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  app.get('/api/name', (_req, res) => {
    res.json({ name: 'Adriano, Tessaro' });
  });

  app.get('/api/server-ip', (_req, res) => {
    res.json({ ip: env.serverPublicIp });
  });

  app.get('/api/client-ip', (_req, res) => {
    // Get the socket's remote address 
    const clientIp = _req.ip;
    if (clientIp === undefined) {
      return res.status(400).json({ error: 'Client IP not found' });
    }
    res.json({ ip: clientIp });
  });

  app.get('/api/server-time', (_req, res) => {
    const time = new Date();
    // Remove the (UTC) from the time string
    const formattedTime = time.toTimeString().replace(/^(\d{2}:\d{2}:\d{2}) GMT([+-]\d{2})(\d{2}).*$/, '$1 GMT$2:$3');
    res.json({ time: formattedTime });
  });

  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });

  return app;
}
