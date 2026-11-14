const test=require('node:test');
const assert=require('node:assert/strict');
const auth=require('../js/supabase-auth.js');
const config={url:'https://example.supabase.co',publishableKey:'sb_publishable_test'};
test('no auth request without credentials',async()=>{
 let count=0;await assert.rejects(auth.signInWithPassword({config,email:'',password:'',fetch:()=>{count++}}),/email/);assert.equal(count,0);
});
test('auth rejects failed server response without returning credentials',async()=>{
 await assert.rejects(auth.signInWithPassword({config,email:'test@example.com',password:'dummy',fetch:async()=>({ok:false,status:401})}),/401/);
});
test('auth returns access token in memory on success',async()=>{
 let request;
 const r=await auth.signInWithPassword({config,email:'test@example.com',password:'dummy',fetch:async(url,opts)=>{request={url,opts};return{ok:true,json:async()=>({access_token:'token',user:{id:'11111111-1111-4111-8111-111111111111'}})};}});
 assert.equal(r.accessToken,'token');assert.match(request.url,/\/auth\/v1\/token/);assert.equal(JSON.parse(request.opts.body).email,'test@example.com');
});
