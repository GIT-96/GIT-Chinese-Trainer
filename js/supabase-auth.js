/* Isolated Supabase Auth REST client. No automatic login or data upload.
   Access token is held in memory, not localStorage. */
(function(root,factory){
 const api=factory();
 if(typeof module!=='undefined'&&module.exports)module.exports=api;
 else root.GITSupabaseAuth=api;
})(typeof window!=='undefined'?window:globalThis,function(){
 'use strict';
 const sync=typeof require==='function'?require('./supabase-sync.js'):null;
 function origin(config){
  if(sync)return sync.validateConfig(config);
  if(!config||typeof config.url!=='string'||typeof config.publishableKey!=='string'||!config.publishableKey.trim())throw Error('Thiếu cấu hình Supabase.');
  const u=new URL(config.url);
  if(u.protocol!=='https:'||!/^[a-z0-9-]+\.supabase\.co$/i.test(u.hostname)||u.pathname!=='/'||u.search||u.hash)throw Error('Supabase URL không hợp lệ.');
  return u.origin;
 }
 async function signInWithPassword(opts){
  const base=origin(opts.config);
  if(!opts.email||!opts.password)throw Error('Cần email và mật khẩu.');
  const request=opts.fetch||(typeof fetch!=='undefined'?fetch:null);
  if(!request)throw Error('Không có kết nối mạng.');
  const response=await request(base+'/auth/v1/token?grant_type=password',{
   method:'POST',headers:{apikey:opts.config.publishableKey,'Content-Type':'application/json'},
   body:JSON.stringify({email:opts.email,password:opts.password})
  });
  if(!response.ok)throw Error('Đăng nhập thất bại (HTTP '+response.status+').');
  const result=await response.json();
  if(!result.access_token||!result.user||!result.user.id)throw Error('Phiên đăng nhập không hợp lệ.');
  return {accessToken:result.access_token,userId:result.user.id,expiresIn:result.expires_in||0};
 }
 return Object.freeze({signInWithPassword});
});
