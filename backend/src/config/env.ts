import 'dotenv/config';

const rawPort = process.env.PORT;
const port =
  rawPort === undefined || rawPort === ''
    ? 3000
    : Number.parseInt(rawPort, 10);

if (Number.isNaN(port) || port < 1 || port > 65535) {
  throw new Error(`Invalid PORT: ${rawPort}`);
}

const rawServerPublicIp = process.env.SERVER_PUBLIC_IP;
const serverPublicIp =
  rawServerPublicIp === undefined || rawServerPublicIp === ''
    ? '127.0.0.1'
    : rawServerPublicIp;

export const env = {
  port,
  serverPublicIp,
} as const;
