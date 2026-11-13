const test = require('node:test');
const assert = require('node:assert/strict');
const sync = require('../js/supabase-sync.js');
const config = {url:'https://demo-project.supabase.co',publishableKey:'sb_publishable_test'};
function store(initial = {}) {const m = new Map(Object.entries(initial));return {getItem:k=>m.has(k)?m.get(k):null,setItem:(k,v)=>m.set(k,String(v))};}
test('requires opt-in, without network request', async () => {
  let calls=0;
  await assert.rejects(sync.uploadSnapshot({config,consent:false,fetch:()=>{calls++;}}), /xác nhận/);
  assert.equal(calls,0);
});
test('rejects untrusted URLs',()=>assert.throws(()=>sync.validateConfig({url:'https://evil.example',publishableKey:'x'})));
test('does not upload if no history',async()=>{
  let calls=0;
  await assert.rejects(sync.uploadSnapshot({consent:true,config,accessToken:'token',userId:'11111111-1111-4111-8111-111111111111',storage:store(),uuid:'22222222-2222-4222-8222-222222222222',fetch:()=>{calls++;}}),/Chưa có dữ liệu/);
  assert.equal(calls,0);
});
test('acknowledged snapshot increments revision',async()=>{
  const storage=store({'gct-history':JSON.stringify({done:3,correct:2})});
  let posted;
  const opts={consent:true,config,accessToken:'token',userId:'11111111-1111-4111-8111-111111111111',uuid:'22222222-2222-4222-8222-222222222222',storage,fetch:async (url,req)=>{posted=JSON.parse(req.body);return {ok:true,status:201}}};
  const r=await sync.uploadSnapshot(opts);
  assert.equal(r.revision,1);assert.equal(posted.payload.history.done,3);
  assert.equal(storage.getItem('gct-sync-revision-v1'),'1');
});
test('failed upload does not advance revision',async()=>{
  const storage=store({'gct-history':'{"done":1}'});
  await assert.rejects(sync.uploadSnapshot({consent:true,config,accessToken:'token',userId:'11111111-1111-4111-8111-111111111111',uuid:'22222222-2222-4222-8222-222222222222',storage,fetch:async()=>({ok:false,status:503})}),/503/);
  assert.equal(storage.getItem('gct-sync-revision-v1'),null);
});
