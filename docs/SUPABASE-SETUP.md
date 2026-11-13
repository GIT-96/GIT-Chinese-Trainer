# Supabase setup — GIT Chinese Trainer (staging only)

1. Create a Supabase project under your account. Enable email sign-in in Authentication.
2. In SQL Editor, apply `supabase/migrations/202611130001_learning_backups.sql`.
3. In project API settings obtain Project URL and **publishable/anon** key. NEVER use service_role or secret keys in client code.
4. Configure authentication and explicitly opt-in from a reviewed UI before calling `GITSupabaseSync.uploadSnapshot`.
5. Use the logged-in user's access token and user UUID from Supabase Auth; never trust a manually typed user ID.
6. Run `node --test tests/supabase-sync.test.cjs` in a local checkout before integration.
7. Verify RLS using two different users: neither can read or insert the other's rows; updates and deletes are blocked.
8. Confirm offline learning and local history remain unchanged; verify remote backup appears after acknowledged upload.
9. Confirm error/timeout keeps history locally and does not claim success.
10. Build and test Android APK only after review. Never merge into main automatically.

Current stage: transport and schema only. No credentials, login UI, automatic upload, download/restore, server-side backups, or production deployment are configured. Uploading snapshots to Supabase is **not** equivalent to a separate disaster-recovery backup. Additional independent backup policy is required.
