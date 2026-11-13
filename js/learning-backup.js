/* Optional standalone learning history backup helper. No automatic upload, deletion, or migration.
 * Integrate only after a reviewed UI and device test. */
(function (global) {
  'use strict';
  const KEY = 'gct-history';
  const FORMAT = 'git-chinese-trainer-history';
  const VERSION = 1;
  function exportHistory(storage) {
    const store = storage || global.localStorage;
    const raw = store.getItem(KEY);
    if (raw === null) throw new Error('Không có lịch sử học để sao lưu.');
    const history = JSON.parse(raw);
    if (!history || typeof history !== 'object' || Array.isArray(history)) throw new Error('Dữ liệu lịch sử không hợp lệ.');
    return JSON.stringify({ format: FORMAT, version: VERSION, exportedAt: new Date().toISOString(), history }, null, 2);
  }
  function validateBackup(text) {
    const obj = JSON.parse(text);
    if (!obj || obj.format !== FORMAT || obj.version !== VERSION || !obj.history ||
        typeof obj.history !== 'object' || Array.isArray(obj.history)) throw new Error('Bản sao lưu không đúng định dạng.');
    return obj.history;
  }
  function restoreHistory(text, options) {
    const opts = options || {};
    if (opts.confirm !== true) throw new Error('Cần xác nhận rõ trước khi khôi phục.');
    const store = opts.storage || global.localStorage;
    const history = validateBackup(text);
    const previous = store.getItem(KEY);
    // Caller must retain previous backup before invoking this operation.
    if (previous !== null && opts.previousBackupSaved !== true) throw new Error('Hãy xuất và lưu bản sao dữ liệu hiện tại trước khi ghi đè.');
    store.setItem(KEY, JSON.stringify(history));
    return { restored: true, previousExisted: previous !== null };
  }
  global.GITLearningBackup = Object.freeze({ exportHistory, validateBackup, restoreHistory });
})(typeof window !== 'undefined' ? window : globalThis);
