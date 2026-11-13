-- GIT Chinese Trainer: immutable, per-user encrypted-in-transit learning snapshots.
-- Run in Supabase SQL Editor on a project you control.
create table if not exists public.learning_backups (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  device_id uuid not null,
  revision bigint not null check (revision > 0),
  schema_version integer not null default 1 check (schema_version = 1),
  payload jsonb not null check (jsonb_typeof(payload) = 'object'),
  created_at timestamptz not null default now(),
  unique(user_id, device_id, revision)
);
create index if not exists learning_backups_latest_idx
  on public.learning_backups (user_id, created_at desc);
alter table public.learning_backups enable row level security;
revoke all on public.learning_backups from anon;
revoke all on public.learning_backups from authenticated;
grant select, insert on public.learning_backups to authenticated;
drop policy if exists "read_own_learning_backups" on public.learning_backups;
create policy "read_own_learning_backups" on public.learning_backups
for select to authenticated using (auth.uid() = user_id);
drop policy if exists "insert_own_learning_backups" on public.learning_backups;
create policy "insert_own_learning_backups" on public.learning_backups
for insert to authenticated with check (auth.uid() = user_id);
-- No update/delete policies: backups remain immutable from the client.
-- Retention, export and account deletion must be handled by trusted admin procedures.
