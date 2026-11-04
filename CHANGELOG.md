# Changelog

## 3.0.17

- Sleep timer in the player and as a notification action
- Playback speed 0.5–2x, per video or remembered (pitch stays locked)
- Watch-later: add any history entry to the queue
- Data saver mode (480p everywhere, no preloads)
- Watch stats tab (videos, hours, active days, top channels)
- Full settings backup/restore to JSON with restart
- Downloads: pause-all/resume-all, notification action, scheduled night downloads
- Comments: like button and replies when signed in
- Enriched notification (elapsed time, all actions) and search filters (All/Videos/Channels/Playlists)
- Homescreen player widget

## 3.0.16

- Hotfix for instant crash on open: WorkManager no longer auto-inits at startup (was killing the app before Application.onCreate, with no report and no dialog)
- Crash reports are now ALWAYS shown (not hidden behind the welcome dialog) and a copy is auto-saved to Downloads/TestTube/
- Welcome dialog can no longer brick the app into a crash loop; search filter chips fail safe

## 3.0.15

- Sleep timer (More options, 15/30/60/90 min, pauses when done)
- Playback speed 0.5–2x in More options (pitch stays locked, remembered per app)
- Queue save/load as JSON files (Library → queue tab)
- Night downloads: resume paused queue daily at 02:00 on unmetered networks
- Chapters already exist via the segments button (tap to jump, long-press for preview); no change needed
- Stats-for-nerds overlay toggle (resolution, codec, bitrate, buffer, dropped frames)
- Richer notification: elapsed-time chronometer, all actions in compact view
- Search filters: All / Videos / Channels / Playlists chips
- Comments: like button + reply composer for signed-in accounts (best-effort InnerTube endpoints, errors shown)
- Homescreen widget: title, play/pause, next (uses notification media-button intents)

## 3.0.14

- Upstream breakage radar: scheduled extractor canary (every 6h) with auto-issue on failure
- `version.json` broadcasts: `notice`/`noticeTag` info dialogs + `minVersionCode` update-required prompts
- Playback errors on HDR streams fall straight back to SDR muxed; repeated extraction failures suggest checking for updates
- New strings translated (update_open_github, upstream notices)

## 3.0.12

- Shorts now use a single player + cache prefetch instead of two players (less RAM, battery and mobile data)
- New: Allow HDR toggle (Settings → Playback, off by default — fixes blue skin on Samsung)
- New: SponsorBlock categories picker (Settings → Playback)
- More options Download now shows progress and a shortcut to Downloads when already downloading
- Crash dialog can Share the report (Copy still there)
- Full translations for all new settings (es/fr/ja/ko/ru/tr/zh)
- F-Droid metadata: changelogs 30003–30012, phone screenshots
- Removed the fake baseline profile stub + profileinstaller (no Macrobenchmark profile yet)
- CI: new check workflow (debug build + unit tests + lint) runs on every push to main

## 3.0.11

- New: Allow HDR toggle, SponsorBlock categories picker, download progress dialog
- Full translations for the new settings

## 3.0.10

- Forced H264 fallback + Prefer H264 toggle (fixes remaining blue videos on Samsung firmware)

## 3.0.9

- Forced SDR fallback: HDR-only videos play H264 muxed instead of blue HDR

## 3.0.8

- Prefer SDR over HDR (VP9.2 / AV1 10-bit deprioritized); dropped missing media3 API

## 3.0.7

- Fixed blue skin on Samsung: disabled video tunneling (GPU tone mapping)

## 3.0.6

- Fixed MediaButtonReceiver crash: restored PlaybackService MEDIA_BUTTON handler

## 3.0.5

- New privacy controls: Personalize Home, Skip sponsors, Preload on mobile
- Encrypted login flag, R8 obfuscation, shared OkHttp pool for live streams
- Stability: queue diff off main thread, Call.cancel on pause/cancel, FloatingWindow/MiniBar/Controller release
- More options Download shows progress if already downloading

## 3.0.4

- Sharper thumbnails (hqdefault + 854x480)
- Download current video from the player More options menu

## 3.0.3

- Security/privacy hardening: login WebView allowlist, PoToken lockdown, FileProvider narrowed, https-only links, mandatory SHA-256 + same-signer updates
- Stability: no leaked ExoPlayer on font change, DownloadService stops itself, capped history, throttled progress
- Independent dwicao releases with AOSP test-key signing + version.json auto-updates

## 3.0.0

- New look: one consistent style everywhere. Buttons, icons and labels share one colour; the long-press menu is a single list of actions with icons and names; fewer background shades
- New: three themes to choose from in Settings → Appearance → Theme: Dark, Light and NiiX (black, dark glass with a pink edge, pink accents and JetBrains Mono, like niixa.org). System default follows the phone between Light and Dark
- The mini player and bottom bar are frosted glass with the lists scrolling under them (Android 12+), dialogs and sheets blur what is behind them
- Library: the selected tab's edge follows its rounded corners
- Swiping a video right now says "Add"
- Download dialogs: Cancel is a plain button, the option buttons stay readable in every theme
- Fixed: a comment without a name showed "null"

## 2.5.0

- New: Search-only Home (Settings → Content). Home shows just the logo, the name and a search bar, no suggested videos and no top bar; nothing is loaded for it. Tap the logo for the settings.

## 2.4.2

- Fixed: the app closed when the next video started while playing in the background (Android 12+ doesn't let a background app start its player service again, and 2.4.1 let go of it between videos). Only a real pause lets go of it now.
- Same kind of crash prevented for downloads: the next part of a playlist download no longer closes the app when it starts in the background, and Android 15's 6-hour limit for downloads is handled
- The update installer and its permission screen can't close the app on phones that lack them

## 2.4.1

- Light theme stays light on phones with Android 16's "expanded dark theme" (it was being inverted into an odd dark look)
- Fixed a crash right after switching the theme
- New: Clear cache on exit (Settings → Playback, also in the welcome dialog, which shows once more for it)
- Paused videos no longer keep the app running in the background; the notification can be swiped away then
- If the app ever closes unexpectedly, the next start shows the error to copy into a bug report

## 2.4.0

- Videos start playing before likes, suggestions and chapters are in; those fill in a moment later
- Shorts: next 3 extracted right away, the next one already loading in a second player, so a swipe starts it at once. No likes/suggestions request for Shorts. Quality capped at 720p.
- BotGuard and the connections to YouTube get set up shortly after start, not on the first tap
- Home: first page shown right away, more loaded right after instead of before
- Baseline profile covers Glide and OkHttp too
- Requests waiting for a free slot start the moment one frees up instead of polling; the AI-list scan no longer holds a slot
- Video details aren't copied through JSON twice more on every start
- Offline copy of the lists is written in the background, not on the main thread
- Download sizes in your language and units; removed unused code and duplicate helpers

## 2.3.1

- Pictures load much faster: Glide instead of Picasso, thumbnails as WebP (about a third smaller), the next rows' pictures load ahead, and pictures stay cached at their real size
- The notification picture comes from the picture cache instead of being downloaded again
- Missing channel pictures are looked up in the background instead of holding up the list
- Subscriptions: old videos jumped to the top after loading more (videos without a readable date were treated as brand new)
- Pull to refresh now really reloads every channel, and channel feeds are never served from cache
- Slow channels are waited for (up to 9 s) instead of cut off after 2.5 s
- Subscriptions with a YouTube account: sorting no longer gives up when one video has no date; live streams on top

## 2.3.0

- Subscriptions sorted by upload date, newest first (also while channels are still loading and when scrolling further down). Home unchanged.
- Original audio track by default (AI dubs last); a track picked in the menu only sticks for that video
- Titles, channel names, descriptions and chapters in the creator's original language instead of YouTube's translations
- Channel pages only hide the Playlists tab now; Podcasts, Courses and Releases are back
- Fixed: original audio not detected in some languages

## 2.2.2

### New
- Theme follows the system by default
- Channel pages: compact rows, newest videos first
- Private/removed/age-restricted/blocked videos get skipped in playlists and the queue, with a short note why
- Removed the "Up next" popup

### Fixed
- Light theme got darkened by Android's force dark when the phone was in dark mode
- Close button on the video menu thumbnail was hard to see
- 60 fps videos downloaded in lower quality in playlist downloads ("1080p60" was read as 108060)
- Playlist download errors showed raw English text
- Exporting channels could freeze the app on slow storage (Drive etc.)
- Download dialogs kept working in the background after closing
- "Download" share entry wasn't translated; French and Traditional Chinese "threads"

### Other
- Code cleanup, removed dead code and duplicate helpers
- Dropped the unverifiable YouTube link verification

## 2.1.1

- Light theme (Settings → Appearance)
- Update check reads tags like `v.2.1.1`
- Pre-release switch works without closing the dialog first

## 2.1.0

### Player
- Floating window inside the app, or minimize to the bottom bar (setting)
- Custom gestures: pick what left/right swipes, sideways swipes and double taps do
- Fullscreen: vertical swipes always do brightness and volume
- Video cache setting: Large 150 MB / Medium 100 MB / Small 50 MB
- Clear cache button

### Connection
- Updated NewPipe Extractor
- One consistent browser identity and shared cookies
- Requests are paced; backs off after 429s and bot checks, retries once by itself
- Readable error messages with Try again instead of stack traces
- Subscriptions cache channels for 15 minutes

### Library & look
- Library tab with Queue / History / Downloads tabs, one bin to clear the open tab
- Settings grouped into cards, shorter descriptions
- One colour scheme, dialog animations, blur on Android 12+
- Pre-release updates switch; better version comparison
- Notifications and error messages translated

## 2.0.2

### Settings
- Welcome dialog on first start
- Subscriptions tab can be hidden
- Watched videos: on/off, how dark, and when a video counts as watched
- Separate quality for Wi-Fi and mobile data, switches mid-video
- Write comments when signed in
- Hide AI videos (AiSList) is back
- New app icon

### No account needed
- Sign-in optional; Home built from your history when signed out
- Follow channels locally, import (Takeout, NewPipe, OPML) and export
- Faster Subscriptions, loads as channels answer

### Watching
- YouTube-style drag to minimize
- Floating player when leaving the app, background audio otherwise
- Suggestions, description and comments tabs under the video
- Autoplay that avoids repeats; next video is preloaded
- Shorts tab (optional)
- Ads and Premium upsells filtered out

### Lists
- Channel pages like YouTube's (banner, follow button, tabs)
- Channel pictures next to titles
- Swipe right to queue, left to follow
- Content in your phone's language and country

### Other
- Dislike counts removed
- Build script with live log
- Security hardening (web views, file sharing, file names)
- Rebranded to TestTube
- Cleanup
