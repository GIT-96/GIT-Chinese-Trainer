# Supabase backup — safe integration checklist

Branch: `upgrade/hybrid-ai-safe-v1`. No production data migration or automatic uploads.

1. In Supabase Authentication, enable email sign-in and create a **test user**. Verify email if required.
2. In Project Settings → API Keys, obtain the **publishable** (`sb_publishable_...`) key and Project URL. Never share database password, `service_role`, or secret key.
3. Run the existing migration `supabase/migrations/202611130001_learning_backups.sql` in the correct Supabase project (only once; it is idempotent).
4. Run `node --test tests/*.test.cjs` and `node --check js/supabase-auth.js` locally or in CI.
5. Before first real upload, export `gct-history` locally using `GITLearningBackup.exportHistory()` and retain the file off-device.
6. Sign in via `GITSupabaseAuth.signInWithPassword({config,email,password})`; the returned accessToken/userId are held in memory only.
7. Upload only after explicit consent via `GITSupabaseSync.uploadSnapshot({config,accessToken,userId,consent:true})`.
8. Check the returned revision, then list with `GITSupabaseSync.listSnapshots`. Verify a second test user cannot read another user's rows (RLS).
9. Test offline and HTTP 503: local `gct-history` and revision must remain unchanged. Do **not** enable auto-sync or restore until live tests pass.

**Important:** This is a prepared integration and unit-test suite, not proof of a successful upload to the live project. The app UI is intentionally unchanged. Supabase URL/key have not yet been configured.
