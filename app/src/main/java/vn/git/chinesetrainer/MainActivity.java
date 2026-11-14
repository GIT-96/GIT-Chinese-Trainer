package vn.git.chinesetrainer;
import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import java.util.Locale;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.webkit.GeolocationPermissions;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
public class MainActivity extends Activity {
 private WebView web; private TextToSpeech tts; private boolean ttsReady=false; private PermissionRequest pendingMic; private GeolocationPermissions.Callback pendingGeo; private String pendingGeoOrigin;
 private static final int MIC=101, GEO=102;
 private long updateDownloadId=-1;
 private final BroadcastReceiver updateReceiver=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){if(!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))return;long id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);if(id!=updateDownloadId)return;DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);DownloadManager.Query query=new DownloadManager.Query().setFilterById(id);try(android.database.Cursor c=dm.query(query)){if(c!=null&&c.moveToFirst()){int status=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));if(status==DownloadManager.STATUS_SUCCESSFUL){Uri uri=dm.getUriForDownloadedFile(id);if(uri!=null){Intent install=new Intent(Intent.ACTION_VIEW);install.setDataAndType(uri,"application/vnd.android.package-archive");install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);try{startActivity(install);}catch(Exception ex){Toast.makeText(MainActivity.this,"Mở tệp APK đã tải trong Downloads để cài đặt.",Toast.LENGTH_LONG).show();}}}else Toast.makeText(MainActivity.this,"Tải bản cập nhật thất bại.",Toast.LENGTH_LONG).show();}}catch(Exception ex){Toast.makeText(MainActivity.this,"Không mở được APK vừa tải.",Toast.LENGTH_LONG).show();}}};
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff0874d9);getWindow().setNavigationBarColor(0xfff4f8ff);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 if(Build.VERSION.SDK_INT>=33)registerReceiver(updateReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(updateReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
 tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){int result=tts.setLanguage(Locale.SIMPLIFIED_CHINESE);ttsReady=(result!=TextToSpeech.LANG_MISSING_DATA && result!=TextToSpeech.LANG_NOT_SUPPORTED);}});
 web=new WebView(this);setContentView(web);
 web.addJavascriptInterface(new Object(){@JavascriptInterface public int getVersionCode(){return 26;} @JavascriptInterface public void installApk(String url){if(url==null)return;runOnUiThread(()->{try{Uri uri=Uri.parse(url);String host=uri.getHost();String path=uri.getPath();if(!"https".equals(uri.getScheme())||!("github.com".equals(host)||"objects.githubusercontent.com".equals(host))||path==null||!path.endsWith(".apk")||(!"github.com".equals(host)?false:!path.startsWith("/GIT-96/GIT-Chinese-Trainer/releases/download/")))throw new SecurityException("Invalid release URL");DownloadManager.Request req=new DownloadManager.Request(uri);req.setTitle("GIT Chinese Trainer - cập nhật");req.setDescription("Đang tải APK từ GitHub Releases");req.setMimeType("application/vnd.android.package-archive");req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);req.setDestinationInExternalFilesDir(MainActivity.this,android.os.Environment.DIRECTORY_DOWNLOADS,"git-chinese-trainer-update.apk");updateDownloadId=((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(req);Toast.makeText(MainActivity.this,"Đang tải bản cập nhật. Vui lòng chờ...",Toast.LENGTH_LONG).show();}catch(Exception ex){Toast.makeText(MainActivity.this,"Không thể tải APK từ liên kết này.",Toast.LENGTH_LONG).show();}});}},"GITNativeUpdate");web.addJavascriptInterface(new Object(){@JavascriptInterface public void speak(String text,double rate){if(text==null||text.length()>500)return;runOnUiThread(()->{if(web.getUrl()==null||!web.getUrl().startsWith("https://git-96.github.io/GIT-Chinese-Trainer/"))return;if(ttsReady){tts.setSpeechRate((float)Math.max(0.5,Math.min(1.3,rate)));tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"mandarin");}});}},"GITNativeTTS");WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setGeolocationEnabled(true);settings.setMediaPlaybackRequiresUserGesture(true);
 web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri url=request.getUrl();return !("https".equals(url.getScheme())&&"git-96.github.io".equals(url.getHost()));}});
 web.setWebChromeClient(new WebChromeClient(){
 @Override public void onPermissionRequest(PermissionRequest request){runOnUiThread(()->{if(!request.getOrigin().getHost().equals("git-96.github.io")){request.deny();return;}for(String resource:request.getResources()){if(PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});}else{pendingMic=request;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC);}return;}}request.deny();});}
 @Override public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback){if(!origin.startsWith("https://git-96.github.io")){callback.invoke(origin,false,false);return;}if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){callback.invoke(origin,true,false);}else{pendingGeo=callback;pendingGeoOrigin=origin;requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},GEO);}}
 });
 web.loadUrl("https://git-96.github.io/GIT-Chinese-Trainer/?v=31");
 }
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);boolean ok=results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED;if(code==MIC&&pendingMic!=null){if(ok)pendingMic.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});else pendingMic.deny();pendingMic=null;}if(code==GEO&&pendingGeo!=null){boolean allowed=false;for(int x:results)if(x==PackageManager.PERMISSION_GRANTED)allowed=true;pendingGeo.invoke(pendingGeoOrigin,allowed,false);pendingGeo=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}try{unregisterReceiver(updateReceiver);}catch(Exception ignored){}web.destroy();super.onDestroy();}
}