# Release build and Google Play

How to build the signed version of the app that is uploaded to the Google Play Store.

## What the release build does

- **R8** shrinks and obfuscates the code and removes unused resources (`isMinifyEnabled`,
  `isShrinkResources`). The APK goes from about 27 MB (debug) to about 3.5 MB.
- **Debug and verbose logs are removed** (`Log.d` / `Log.v`, see `app/proguard-rules.pro`);
  warnings and errors stay.
- It is **signed with the release key**, which is different from each developer's debug key.

## 1. Create the release key (once, by the tech lead)

The key proves future updates come from us. **Keep the file and its passwords safe and never
commit them** (`*.jks` is in `.gitignore`). If the key is lost, the app can't be updated on Play.

```bash
keytool -genkeypair -v -keystore ~/keys/iuafc-release.jks -alias iuafc \
  -keyalg RSA -keysize 2048 -validity 10000
```

`keytool` asks for the passwords and your name; type them yourself, don't put them in the
command. If `keytool` is not found, use the one inside Android Studio
(`/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool`).

## 2. Tell Gradle where the key is

Add to `local.properties` (never committed):

```properties
RELEASE_STORE_FILE=/Users/<you>/keys/iuafc-release.jks
RELEASE_STORE_PASSWORD=<store password>
RELEASE_KEY_ALIAS=iuafc
RELEASE_KEY_PASSWORD=<key password>
```

Without these lines the release build still compiles, but it is not signed.

## 3. Build

```bash
./gradlew bundleRelease     # app/build/outputs/bundle/release/app-release.aab  (upload to Play)
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk     (install on a phone)
```

## 4. Google sign-in with the release key

Google only accepts apps signed with a registered key (see [google-sign-in.md](google-sign-in.md)).
Register the release key's SHA-1 as another **Android** OAuth client in Google Cloud
(package `com.app.iuafc`):

```bash
keytool -list -v -keystore ~/keys/iuafc-release.jks -alias iuafc | grep SHA1
```

When the app is uploaded to Google Play, Play re-signs it with its own **app signing key**.
Copy that SHA-1 from Play Console → *Test and release → App integrity → App signing* and register
it too, or Google sign-in will fail for users who install from the Play Store.

## 5. Upload to Google Play

1. Create the app in [Play Console](https://play.google.com/console) (*Create app*, name
   "IUAFC Church Management", free).
2. *Test and release → Internal testing → Create new release* and upload `app-release.aab`.
3. Fill in the store listing (short and full description, app icon 512×512, feature graphic
   1024×500, at least two phone screenshots from `docs/images`).
4. *App content*: privacy policy URL `https://iuafc.vercel.app/privacy`, account deletion URL
   `https://iuafc.vercel.app/delete-account`, data safety form, content rating questionnaire,
   target audience.

Reference: Android Developers. 2026. Sign your app. [Online]. Available at:
https://developer.android.com/studio/publish/app-signing [Accessed 6 November 2026].
