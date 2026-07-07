# FastTrack — a private, local-only intermittent fasting app

A minimal Android app (Kotlin + Jetpack Compose + Room + Glance widget) built for personal
use. **No internet permission is requested anywhere in the manifest** — all fasting,
weight, and water data lives only in a local SQLite database on your phone
(`/data/data/com.local.fasttrack/databases/fasttrack.db`). There is no analytics, no
cloud sync, no account.

## Features
- Start/end a fast with 16:8, 18:6, or 20:4 presets
- Circular progress timer (styled after the screenshot you shared) showing the
  remaining time and current metabolic "stage" (Digesting → Fat burning → Ketosis →
  Autophagy) — the same simplified staging popular fasting apps like Zero use
- History tab: streak, total fasts, average length, longest fast, and a deletable list
  of past fasts
- Daily water log (+250 ml chip) and weight log, shown as "Daily tasks" cards
- A home-screen widget showing live remaining time + current stage, matching the
  screenshot's "Fat burning / 08:35:21" look. It updates instantly when you start/end
  a fast or log water, and refreshes every 15 minutes in the background otherwise
  (Android does not allow second-by-second widget redraws — that's a platform limit,
  not a shortcut I took)

## How to build it (you'll need Android Studio — I can't compile an APK in this sandbox)
1. Install **Android Studio** (Hedgehog or newer) if you don't have it.
2. Download the `FastTrack` folder I've attached and unzip it anywhere.
3. Open Android Studio → **Open** → select the unzipped `FastTrack` folder.
4. Android Studio will detect there's no Gradle wrapper and offer to create one —
   accept that (or Studio may auto-generate it on first sync). If it asks which
   Gradle version, use **8.7** or newer.
5. Let Gradle sync (needs internet the first time, to download the Android/Kotlin/Compose
   libraries themselves — after that, the *app* itself never touches the network).
6. Plug in your phone (with USB debugging on) or start an emulator, then click **Run ▶**.
7. Long-press your home screen → **Widgets** → find "FastTrack" → drag the "Fasting
   Timer" widget onto your home screen.

## Project layout
```
app/src/main/java/com/local/fasttrack/
  MainActivity.kt              – hosts the two tabs (Fast / Progress)
  ui/                          – Compose screens, theme, the ViewModel
  data/                        – Room entities, DAOs, database, repository, fasting-stage logic
  widget/                      – Glance widget + its receiver + background refresh worker
app/src/main/res/              – strings, colors, launcher icon, widget metadata
```

## Extending it later
- Add a custom-hours option: change `PRESETS` in `HomeScreen.kt` and allow a text/slider input.
- Add charts for weight trend: the `weights` StateFlow in `FastingViewModel` already
  exposes full history — a simple Compose Canvas line chart or the `Vico`/`MPAndroidChart`
  library could plot it.
- Add notifications when a fast completes: WorkManager is already wired up in
  `WidgetRefreshWorker` — you could add a notification check in `doWork()`.

## Building this entirely from your phone (no PC), via GitHub Actions

I've added `.github/workflows/android-build.yml` — it tells GitHub's own servers to
compile the APK for you. You just need to get this folder into a GitHub repo and
trigger the workflow, all from your phone.

1. **Install Termux** (get it from F-Droid, not the outdated Play Store version) and
   in it run: `pkg update && pkg install git unzip -y`
2. **Get the project onto Termux's storage**: run `termux-setup-storage` once (grants
   file access), then copy the zip you downloaded, e.g.
   `cp /sdcard/Download/FastTrack.zip ~/ && cd ~ && unzip FastTrack.zip && cd FastTrack`
3. **Create an empty GitHub repo** (e.g. `fasttrack-app`) via the GitHub app or
   github.com in your browser — don't initialize it with a README.
4. **Create a Personal Access Token**: GitHub app/site → Settings → Developer settings
   → Personal access tokens → generate one with `repo` scope. Copy it somewhere safe —
   you'll use it as your password when pushing.
5. **Push from Termux**, inside the `FastTrack` folder:
   ```
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/fasttrack-app.git
   git push -u origin main
   ```
   When it asks for a username/password, enter your GitHub username and paste the
   token as the password.
6. **Watch it build**: open the repo's **Actions** tab (GitHub app or browser) — the
   workflow runs automatically on push. If it doesn't, tap "Run workflow" manually.
7. **Download the APK**: once the run finishes (~3–5 min), open it and download the
   `FastTrack-debug-apk` artifact — it's a zip containing `app-debug.apk`.
8. **Install it**: unzip, tap `app-debug.apk` in your file manager. Android will ask
   you to allow installs from that app the first time — allow it, then install.

You'll repeat steps 5–7 (just the push) any time you want to rebuild after editing code.

## A note on the "fasting stages" shown
The Digesting/Fat burning/Ketosis/Autophagy labels and their hour thresholds
(`FastingStage.kt`) are the simplified, commonly-cited milestones used by mainstream
fasting trackers for motivation — they are general reference points, not medical advice.
