# GIT Chinese Trainer — Safe Hybrid AI upgrade

## Current verified baseline
- GitHub Pages PWA and Android WebView share a web frontend.
- Learning history is stored locally as `gct-history` in localStorage.
- The assistant has a local vocabulary/rule fallback and an optional user-configured backend endpoint.
- No cloud sync, user identity, restore test, or hosted AI backend is present in the inspected files.
- Current APK workflow packages a WebView pointing to the live GitHub Pages URL; APK build success does not validate web behavior.

## Safety gates before deployment
1. Keep main unchanged until review and Android tests.
2. Export and verify a backup of `gct-history` before any migration. Do not silently overwrite history.
3. Define versioned records and immutable operation IDs for retry-safe sync.
4. Implement per-user authentication, row-level authorization, HTTPS, and encrypted backups in the selected backend.
5. Keep AI provider keys server-side; no keys in JS, GitHub Pages, or APK.
6. Only mark a record as synced after server acknowledgment. Expose pending/error states.
7. Test offline writes, reconnection, duplicates, conflict resolution, app upgrades, restores, and Android microphone/TTS.
8. Validate actual HSK datasets against the intended official syllabus and version before claiming HSK 1–6 completeness.

## Proposed components
- Offline: existing HSK dataset and deterministic grading; versioned IndexedDB learning store.
- Router: use local answers when available, online AI only when requested/appropriate; timeouts and fallback.
- Backend: authenticated Node.js API, model proxy, rate limits, content checks, audit and privacy controls.
- Sync: outbox, idempotent event uploads, server revision and conflict handling; separate managed database and scheduled backups.
- Android: native TTS and permissions; test WebView support for speech recognition independently.

## Release policy
No automatic cloud upload of personal learning data without an opt-in, account configuration, privacy notice, and verified backend. No API tokens committed to GitHub. GitHub stores source and CI artifacts, not private learning histories.
