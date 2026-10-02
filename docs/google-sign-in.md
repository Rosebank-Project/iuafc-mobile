# Google sign-in (SSO)

The app signs users in with their Google account using Android **Credential Manager**.
Google returns an ID token, and **Supabase Auth** checks it and creates the user (provider `google`).
The same Google login can be used on the website once it uses Supabase (#23).

## How it works

1. The user taps **Continue with Google** on the login screen.
2. Android shows the Google accounts on the phone.
3. Google returns an ID token to the app (`ui/screens/auth/GoogleCredentialProvider.kt`).
4. The app sends the token to Supabase (`SupabaseSessionRepository.signInWithGoogle`).
5. Supabase verifies it with Google, creates or finds the user, and returns a session.

A random nonce is hashed and sent to Google, and the raw value is sent to Supabase, so a stolen
token cannot be reused.

## Setup (already done for the group project)

- **Google Cloud** (project `iuafc`): OAuth consent screen + two OAuth clients
  - *Web application*: its Client ID and secret are in Supabase → Authentication → Providers → Google
  - *Android*: package `com.app.iuafc` + the SHA-1 of each developer's debug key
- **Supabase**: Google provider enabled
- **App**: `GOOGLE_WEB_CLIENT_ID` (the **Web** client ID) in `local.properties`
- **GitHub Actions**: `GOOGLE_WEB_CLIENT_ID` repository secret

## Every team member: register your SHA-1

Google only accepts apps signed with a registered key, and every computer has its own debug key.
Until your SHA-1 is registered, **Continue with Google** shows an error on your builds (email login
still works).

1. Get your SHA-1:

   - **macOS / Linux**
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
     ```
   - **Windows (PowerShell)**
     ```powershell
     keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | Select-String SHA1
     ```

   If `keytool` is not found, use the one inside Android Studio:
   `Android Studio/jbr/bin/keytool` (Windows: `C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`).

2. Send the SHA-1 to Miguel. He creates an extra **Android** OAuth client in Google Cloud with
   package `com.app.iuafc` and your SHA-1.

3. Add `GOOGLE_WEB_CLIENT_ID` to your `local.properties` (ask Miguel for the value) and sync Gradle.

> The APK built by GitHub Actions uses a different key, so Google sign-in does not work on it.
> For the demo video, install the app on the phone from Android Studio on a computer whose SHA-1
> is registered.

## Testing on the emulator

The emulator must use a **Google Play** system image and have a Google account added in
**Settings → Passwords & accounts**. Without an account the app shows
"No Google account found".
