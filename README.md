# CPEN 321 — M1 App Skeleton

_Keep this README up to date with the steps required to build and run the frontend and backend (including any scripts, config files, and environment variables). TAs will follow these instructions._

## Requirements

Install the following before the frontend or backend setup steps:

- [git](https://git-scm.com/install/)


--- 

## Frontend Setup

### Requirements

- [Android Studio](https://developer.android.com/studio) (latest version)
- [Java 17](https://adoptium.net/temurin/releases/?version=17)
- [Android SDK](https://developer.android.com/studio#command-tools) with API level 36+ (Android 16)

### Setup

1. **Open project**: Open the `frontend/` directory in Android Studio
2. **Sync Gradle**: Android Studio will automatically prompt you to sync the project. Click "Sync Now". You can also manually run `cd frontend && ./gradlew build` to trigger the sync and download the necessary dependencies.
3. **Configure Android SDK**: Ensure you have Android SDK 36 installed.
4. **Set up emulator/device**:
   - Create a new AVD (Android Virtual Device) by selecting Pixel 9 as the device and Android Baklava (API level 36) as the system image.
   - Alternatively, connect a physical Android device running Android 16 (API level 36).
5. **Setup app config**: Copy the example file, then fill in local values:
   ```bash
   cp frontend/local.properties.example frontend/local.properties
   ```
   Set at least:
   - `sdk.dir`: path to your Android SDK. Android Studio usually writes this the first time you open `frontend/`. On Mac it is often `sdk.dir=/Users/<username>/Library/Android/sdk`.
   - `API_BASE_URL`: backend URL baked into the APK. Leave it as the deployed server:
     `https://34-182-50-132.sslip.io`. That server is already running and stays up until
     grades are posted, so **you do not need to run the backend yourself** to exercise the app.
     Only if you want to run it locally, use `http://10.0.2.2:3000` for the emulator
     (`10.0.2.2` is the host machine as seen from the emulator).
   - `GOOGLE_CLIENT_ID`: required — Google sign-in fails without it. Use the **Web application**
     OAuth client ID: `507043675190-ghcqcn9gapkg8jvk076c1kuanda5h31c.apps.googleusercontent.com`


### Build and Run

- **Debug build**: Click the green play button in the toolbar, to compile the code, package a debug APK, and install it on the connected device or running emulator. Alternatively, from the project root, run `./scripts/run-frontend.sh`.
- **Release build**: from the project root, run `cd frontend && ./gradlew assembleRelease`. The
  APK is written to `frontend/app/build/outputs/apk/release/app-release.apk` and is signed
  automatically with the local debug keystore (`~/.android/debug.keystore`).
  **Do not generate a new signing key.** The Android OAuth client is registered against one
  certificate SHA-1, so an APK signed with any other key compiles and installs but fails
  Google sign-in with `DEVELOPER_ERROR`. For the same reason, an APK you rebuild here will not
  sign in — only the submitted APK carries the registered key.


### Backend Configuration

The backend is already deployed at `https://34-182-50-132.sslip.io` (HTTPS via Caddy) and stays
up until grades are posted, so no backend setup is needed just to run the app. The sections below
are only for building and running it yourself.

---
## Backend Setup

You can run the backend in one of two ways:
* Locally via Node.js 
* Via Docker Compose

Both ways use the same `backend/.env` file (see below).

### Environment configuration

From the project root:

```bash
cp backend/.env.example backend/.env
```

Set at least:
- `JWT_SECRET`: a long random string used to sign auth tokens.
- `MONGODB_URI`: only needed for local development (default in `.env.example` assumes MongoDB on `localhost:27017`). Ignored when running via Docker Compose.
- `PORT` (optional): defaults to `3000` if unset.


### Option 1: Run locally

**Requirements:** 
- [Node.js](https://nodejs.org/en/download/) 22+
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) 10+

**Setup:** 
1. Install dependencies:

   ```bash
   cd backend
   npm install
   ```

2. **Development** (TypeScript with auto-reload):

   ```bash
   npm run dev
   ```

3. **Production build** (optional):

   ```bash
   npm run build
   npm start
   ```

### Option 2: Run with Docker Compose

**Requirements:** 
- [Docker](https://docs.docker.com/desktop/setup/install) and [Docker Compose](https://docs.docker.com/desktop/setup/install) v2.24+
- [curl](https://curl.se/download.html)

**Setup**
1. **Start** (from the project root):

   ```bash
   ./scripts/run-backend.sh
   ```

   Or run Compose directly:

   ```bash
   docker compose up --build -d
   ```

2. **Stop**:

   ```bash
   docker compose down
   ```

## Additional Setup

### Google sign-in (needed for Button 1)

The OAuth consent screen is in **Testing** mode, so only accounts on the test-user list can sign
in. Add the provided test account to the emulator before tapping Button 1:

**Settings → Passwords & accounts → Add account → Google**

The account and its password are in `M1_Doc.pdf`, submitted with this milestone — they are
deliberately not stored in this repository.

### Emulator networking

If sign-in reports "No credentials available" and Button 3 reports "Unable to resolve host", the
emulator's DNS resolver has gone stale — this happens after the host machine changes network or a
VPN toggles. Relaunch the emulator with an explicit resolver; no code change is involved:

```bash
emulator -avd <your-avd> -dns-server 8.8.8.8 -no-snapshot-load
```