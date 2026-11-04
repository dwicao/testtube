<p align="center">
<img src="testtube.png" alt="" width="200">
</p>

in a world full of excess we give you 5 mb of simplicity 

TestTube is an ad-free YouTube client for Android. No account needed: Home, search, subscriptions, queue and history all work without logging in. Signing in is optional and only used if you want your own YouTube subscriptions.

Requires Android 8.0 (API 26) or later.

## Features
* [x] **Light, Dark and NiiX theme** 
* [x] **Works with or without login** 
* [x] **Your YouTube subscriptions (if logged in)** 
* [x] **Home for discovering**
* [x] **Ad-free playback**
* [x] **Swipe a video right to add it to the queue**
* [x] **Sponsor-block**
* [x] **AI video block**
* [x] **Local queue support**
* [x] **Local history**
* [x] **Background & Picture-in-Picture support**
* [x] **Built-in video and queue downloader**
* [x] **Watched videos greyed out**
* [x] **Light on data**
## How it works

Almost everything is native: the player, Home, Subscriptions, search, channel and playlist pages (every channel tab), History, the queue, the screen under the video (Description/Comments tabs, suggestions) and the downloader are drawn by the app itself and get their data directly from YouTube through the NewPipe extractor. This is faster, smoother and fully under our control.

Only two small web views are left: the Google sign-in page, and the live chat of live streams. There is no YouTube website layer, no injected scripts and no page bridge. A third hidden offline BotGuard page (network blocked, no cookies sent) mints PO tokens for playback.

Subscriptions come from one of two places. By default the app shows the latest videos of the channels you follow, read from YouTube's public channel feeds, so no account is involved. "Use my YouTube account" switches to your account's own subscriptions.

## Privacy

* No analytics, crash reporting or ads in the app. History, queue and followed channels stay on the phone, and backups are off. Crash logs stay in the app's files (cleared after one restart) and are never uploaded.
* YouTube and Google still see your IP address and the videos you watch, as with any YouTube client. Sign-in, live chat and extractor requests share one visitor (cookies), like a single browser tab.
* Home discovery (signed out) is built from your recent local history: up to 4 video IDs and 2 titles are sent as YouTube search/next lookups. No toggle yet — clearing history stops it.
* SponsorBlock: the app sends the first 4 characters of a hash of the video ID + your enabled categories + IP to sponsor.ajay.app. Cached in memory for 1h, never in the shared HTTP cache. There are no dislike-count lookups.
* Hide AI videos (off by default): when switched on, the app downloads the community AiSList from raw.githubusercontent.com once a day (If-None-Match). Only IP + User-Agent + ETag are visible, no IDs or cookies.
* Update checks: the app first asks raw.githubusercontent.com/dwicao/testtube/main/version.json (no API limits), then falls back to api.github.com/repos/dwicao/testtube/releases. Check runs 5 seconds after start, including test versions if “Pre-releases” is on. Updates are downloaded from github.com/dwicao/testtube with mandatory SHA-256 + same-signer + versionCode checks. Only IP + `User-Agent: TestTube` are visible. Switch off by tapping the TestTube logo on Home → Updates → “Check for updates automatically”.

## Releases (fork: dwicao/testtube)

* Tag: `git tag v3.0.1 && git push origin v3.0.1` — `.github/workflows/release.yml` builds `assembleRelease`, signs with the public AOSP test key, creates the GitHub Release at `github.com/dwicao/testtube/releases`, uploads `testtube3.0.1.apk`, and updates `version.json` on `main` so the app auto-updates.
* Local: `./BUILD.sh` (own key in `~/.testtube/signing`). CI key is the well-known AOSP `testkey` (password `android`) — fine for testing, not for private accounts: anyone can forge updates signed with it, so the app still enforces same-signer installs.

## Screenshots
**NiiX Theme**
<p align="center">
<img src="screens/1.jpeg" alt="" width="200">
<img src="screens/2.jpeg" alt="" width="200">
<img src="screens/3.jpeg" alt="" width="200">
<img src="screens/4.jpeg" alt="" width="200">
</p>


## License

GPL-3.0. NewPipe Extractor is GPL-3.0-or-later.




Peace and Love

by

techniix / cuteLiLi / QuacK / decoi
