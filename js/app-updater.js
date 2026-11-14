/* User-triggered PWA update: does not clear localStorage, IndexedDB or user data. */
(function(){
'use strict';
const CURRENT='30';
const PATH='./version.json';
let checking=false;
function status(message){const el=document.getElementById('appUpdateStatus');if(el){el.textContent=message;el.setAttribute('role','status')}}
function addVersion(url,version){const u=new URL(url,location.href);u.searchParams.set('appVersion',version);return u.href}
async function check(){
 if(checking)return;
 const btn=document.getElementById('appUpdateButton');checking=true;if(btn)btn.disabled=true;
 try{
  if(!navigator.onLine){status('Đang ngoại tuyến. Dữ liệu học tập vẫn được giữ nguyên.');return}
  status('Đang kiểm tra phiên bản...');
  const response=await fetch(PATH+'?t='+Date.now(),{cache:'no-store'});
  if(!response.ok)throw Error('HTTP '+response.status);
  const data=await response.json();
  if(typeof data.version!=='string'||!/^[a-zA-Z0-9._-]{1,50}$/.test(data.version))throw Error('Phiên bản không hợp lệ');
  if(data.version===CURRENT){status('Bạn đang sử dụng phiên bản mới nhất (v'+CURRENT+').');return}
  status('Đã có phiên bản '+data.version+'. Đang cập nhật...');
  if('serviceWorker' in navigator){
   const registration=await navigator.serviceWorker.getRegistration();
   if(registration){
    await registration.update();
    if(registration.waiting){registration.waiting.postMessage({type:'SKIP_WAITING'});await new Promise(resolve=>setTimeout(resolve,350))}
   }
  }
  // A new URL forces the app shell to refresh; storage is untouched.
  location.replace(addVersion(location.href,data.version));
 }catch(e){status('Chưa kiểm tra được cập nhật. Vui lòng thử lại khi có mạng.')}
 finally{checking=false;if(btn)btn.disabled=false}
}
document.addEventListener('DOMContentLoaded',()=>{const btn=document.getElementById('appUpdateButton');if(btn)btn.addEventListener('click',check)});
window.GITAppUpdater=Object.freeze({check});
})();
