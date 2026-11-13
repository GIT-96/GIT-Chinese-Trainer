/* Opt-in Supabase backup transport. Never activated by merely loading this file.
   Keep the anon/publishable key client-side ONLY; never expose service_role keys. */
(function (root, factory) {
  const api = factory();
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  else root.GITSupabaseSync = api;
})(typeof window !== 'undefined' ? window : globalThis, function () {
  'use strict';
  const STORAGE_KEY = 'gct-history';
  const ID_KEY = 'gct-sync-device-id-v1';
  const REV_KEY = 'gct-sync-revision-v1';
  function validateConfig(config) {
    if (!config || typeof config.url !== 'string' || typeof config.publishableKey !== 'string')
      throw new Error('Thiếu cấu hình Supabase.');
    const u = new URL(config.url);
    if (u.protocol !== 'https:' || !/^[a-z0-9-]+\.supabase\.co$/i.test(u.hostname) || u.pathname !== '/')
      throw new Error('Supabase URL không hợp lệ.');
    if (!config.publishableKey.trim()) throw new Error('Thiếu publishable key.');
    return u.origin;
  }
  function validateHistory(history) {
    if (!history || typeof history !== 'object' || Array.isArray(history))
      throw new Error('Lịch sử học không hợp lệ.');
    return history;
  }
  function readHistory(storage) {
    const raw = storage.getItem(STORAGE_KEY);
    if (!raw) throw new Error('Chưa có dữ liệu học tập để sao lưu.');
    return validateHistory(JSON.parse(raw));
  }
  function deviceId(storage, uuid) {
    let id = storage.getItem(ID_KEY);
    if (!id) {
      if (!uuid || !/^[0-9a-f-]{36}$/i.test(uuid)) throw new Error('Cần UUID thiết bị hợp lệ.');
      id = uuid;
      storage.setItem(ID_KEY, id);
    }
    return id;
  }
  async function uploadSnapshot(opts) {
    if (!opts || opts.consent !== true) throw new Error('Cần bật đồng bộ và xác nhận trước khi gửi dữ liệu.');
    const origin = validateConfig(opts.config);
    const token = opts.accessToken;
    const userId = opts.userId;
    if (!token || !userId || !/^[0-9a-f-]{36}$/i.test(userId)) throw new Error('Cần đăng nhập tài khoản hợp lệ.');
    const storage = opts.storage || localStorage;
    const history = readHistory(storage);
    const uuid = opts.uuid || (typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : null);
    const id = deviceId(storage, uuid);
    const previous = Number(storage.getItem(REV_KEY) || 0);
    if (!Number.isSafeInteger(previous) || previous < 0) throw new Error('Phiên bản đồng bộ không hợp lệ.');
    const revision = previous + 1;
    const payload = { history, exportedAt: new Date().toISOString(), format: 'git-chinese-trainer-history' };
    const fetcher = opts.fetch || fetch;
    const response = await fetcher(origin + '/rest/v1/learning_backups?on_conflict=user_id,device_id,revision', {
      method: 'POST',
      headers: { apikey: opts.config.publishableKey, Authorization: 'Bearer ' + token,
        'Content-Type': 'application/json', Prefer: 'resolution=ignore-duplicates,return=minimal' },
      body: JSON.stringify({ user_id: userId, device_id: id, revision, schema_version: 1, payload })
    });
    if (!response.ok) throw new Error('Đồng bộ thất bại: HTTP ' + response.status);
    storage.setItem(REV_KEY, String(revision));
    return { synced: true, revision };
  }
  async function listSnapshots(opts) {
    const origin = validateConfig(opts.config);
    if (!opts.accessToken || !opts.userId) throw new Error('Cần đăng nhập.');
    const fetcher = opts.fetch || fetch;
    const url = origin + '/rest/v1/learning_backups?select=id,device_id,revision,created_at&user_id=eq.' +
      encodeURIComponent(opts.userId) + '&order=created_at.desc&limit=30';
    const response = await fetcher(url, { headers: {
      apikey: opts.config.publishableKey, Authorization: 'Bearer ' + opts.accessToken
    }});
    if (!response.ok) throw new Error('Không thể lấy danh sách bản sao lưu: HTTP ' + response.status);
    return response.json();
  }
  return Object.freeze({ validateConfig, validateHistory, readHistory, uploadSnapshot, listSnapshots });
});
