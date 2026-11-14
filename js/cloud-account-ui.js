(function(){
'use strict';
let session=null;
const $=id=>document.getElementById(id);
const setStatus=(message)=>{if($('cloudStatus'))$('cloudStatus').textContent=message;};
const config=()=>({url:$('cloudUrl').value.trim(),publishableKey:$('cloudKey').value.trim()});
function open(){const d=$('cloudDialog');d.hidden=false;d.style.display='flex';$('appDrawer')?.setAttribute('inert','');$('cloudEmail').focus();}
function close(){const d=$('cloudDialog');d.hidden=true;d.style.display='none';$('cloudPassword').value='';}
function busy(value){for(const id of ['cloudLogin','cloudBackup','cloudList'])$(id).disabled=value;}
async function signIn(){
 busy(true);
 try{
  const result=await GITSupabaseAuth.signInWithPassword({config:config(),email:$('cloudEmail').value.trim(),password:$('cloudPassword').value});
  session={...result,config:config()};
  $('cloudPassword').value='';
  $('cloudBackup').disabled=false;$('cloudList').disabled=false;
  setStatus('Đã đăng nhập. Chỉ sao lưu khi bạn nhấn Sao lưu ngay.');
 }catch(e){setStatus(e.message);}finally{busy(false);$('cloudBackup').disabled=!session;$('cloudList').disabled=!session;}
}
async function backup(){
 if(!session){setStatus('Vui lòng đăng nhập trước.');return;}
 if(!confirm('Tạo một bản sao lưu mới trên Supabase? Dữ liệu trên thiết bị sẽ không bị thay đổi.'))return;
 busy(true);
 try{
  const result=await GITSupabaseSync.uploadSnapshot({config:session.config,accessToken:session.accessToken,userId:session.userId,consent:true});
  setStatus('Đã gửi bản sao lưu phiên bản '+result.revision+'. Dữ liệu gốc được giữ nguyên.');
 }catch(e){setStatus(e.message);}finally{busy(false);}
}
async function list(){
 if(!session)return;
 busy(true);
 try{
  const rows=await GITSupabaseSync.listSnapshots({config:session.config,accessToken:session.accessToken,userId:session.userId});
  setStatus('Tìm thấy '+rows.length+' bản sao lưu gần nhất. '+(rows[0]?'Mới nhất: phiên bản '+rows[0].revision+'.':''));
 }catch(e){setStatus(e.message);}finally{busy(false);}
}
document.addEventListener('DOMContentLoaded',()=>{
 $('cloudOpen').addEventListener('click',open);
 $('cloudClose').addEventListener('click',close);
 $('cloudLogin').addEventListener('click',signIn);
 $('cloudBackup').addEventListener('click',backup);
 $('cloudList').addEventListener('click',list);
 $('cloudLogout').addEventListener('click',()=>{session=null;$('cloudPassword').value='';$('cloudBackup').disabled=true;$('cloudList').disabled=true;setStatus('Đã đăng xuất. Dữ liệu học tập trên thiết bị vẫn nguyên vẹn.');});
 $('cloudDialog').addEventListener('click',e=>{if(e.target.id==='cloudDialog')close();});
 document.addEventListener('keydown',e=>{if(e.key==='Escape'&&!$('cloudDialog').hidden)close();});
});
})();
