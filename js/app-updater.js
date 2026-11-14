/* Explicit opt-in web/PWA refresh and Android APK release check. */
(function(){'use strict';
const CURRENT='31', RELEASE='https://api.github.com/repos/GIT-96/GIT-Chinese-Trainer/releases/latest';
let checking=false;
const status=m=>{const e=document.getElementById('appUpdateStatus');if(e){e.textContent=m;e.setAttribute('role','status')}};
async function checkAndroid(){
 const native=window.GITNativeUpdate;
 const installed=Number(native.getVersionCode());
 const response=await fetch(RELEASE,{cache:'no-store',headers:{Accept:'application/vnd.github+json'}});
 if(response.status===404){status('Chưa có APK phát hành trên GitHub Releases.');return}
 if(!response.ok)throw Error('HTTP '+response.status);
 const release=await response.json();
 const asset=(release.assets||[]).find(a=>/\\.apk$/i.test(a.name)&&a.browser_download_url);
 const version=Number(String(release.tag_name||'').match(/(?:^|[^0-9])v?(\\d+)$/i)?.[1]);
 if(!Number.isSafeInteger(version)||version<=0||!asset){status('Bản phát hành chưa có APK hoặc số phiên bản hợp lệ.');return}
 if(version<=installed){status('APK đang là bản mới nhất (mã '+installed+').');return}
 const details='Có APK v'+version+' mới hơn bản '+installed+'.\\n'+String(release.body||'').slice(0,500)+'\\n\\nTải và cài đặt? Android sẽ yêu cầu xác nhận.';
 status('Đã có APK v'+version+' — chờ bạn xác nhận.');
 if(!confirm(details))return;
 native.installApk(asset.browser_download_url);
 status('Đang tải APK v'+version+'. Xem thông báo tải xuống trên Android.');
}
async function checkWeb(){
 if(!navigator.onLine){status('Đang ngoại tuyến; dữ liệu học vẫn được giữ nguyên.');return}
 const response=await fetch('./version.json?t='+Date.now(),{cache:'no-store'});
 if(!response.ok)throw Error('HTTP '+response.status);
 const data=await response.json();
 if(!/^[a-zA-Z0-9._-]{1,50}$/.test(data.version||''))throw Error('Invalid version');
 if(data.version===CURRENT){status('Website đang là phiên bản mới nhất (v'+CURRENT+').');return}
 status('Có bản web v'+data.version+'. Đang làm mới giao diện...');
 if('serviceWorker' in navigator){const reg=await navigator.serviceWorker.getRegistration();if(reg){await reg.update();if(reg.waiting)reg.waiting.postMessage({type:'SKIP_WAITING'});}}
 const url=new URL(location.href);url.searchParams.set('appVersion',data.version);location.replace(url.href);
}
async function check(){if(checking)return;checking=true;const btn=document.getElementById('appUpdateButton');if(btn)btn.disabled=true;try{status('Đang kiểm tra phiên bản...');if(window.GITNativeUpdate&&typeof window.GITNativeUpdate.getVersionCode==='function')await checkAndroid();else await checkWeb()}catch(e){status('Không kiểm tra được bản cập nhật. Hãy kiểm tra kết nối mạng.')}finally{checking=false;if(btn)btn.disabled=false}}
document.addEventListener('DOMContentLoaded',()=>document.getElementById('appUpdateButton')?.addEventListener('click',check));
window.GITAppUpdater=Object.freeze({check});
})();
