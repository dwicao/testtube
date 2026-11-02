# Before a release

Static checks don't see how Android treats the app in the background. These do, on a real
phone with Android 12 or newer (and on an old one, Android 8–9, when possible).

## Background playback
- [ ] Play a playlist, screen off: let at least 3 videos change over by themselves
- [ ] Same with the app in the background (another app open)
- [ ] Pause from the notification, wait 2 minutes, resume, let the next video start
- [ ] Pause, leave the app: after a while it stops; the notification can be swiped away
- [ ] Swipe the app away from recents while playing

## Downloads
- [ ] Start a playlist download, leave the app: all parts finish
- [ ] Single download while the app is in the background

## Screens
- [ ] Theme Light → Dark → System, from Settings and on first start (clear app data)
- [ ] Rotate during a video; minimize; floating window
- [ ] Shorts: swipe fast through 10+

## After testing
- [ ] Open the app once more: no "closed unexpectedly" report
- [ ] `./gradlew testReleaseUnitTest` passes
