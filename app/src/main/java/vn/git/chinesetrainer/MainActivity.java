package vn.git.chinesetrainer;
import android.Manifest;
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
import androidx.annotation.NonNull;
public class MainActivity extends Activity {
 private WebView web; private PermissionRequest pendingMic; private GeolocationPermissions.Callback pendingGeo; private String pendingGeoOrigin;
 private static final int MIC=101, GEO=102;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff0874d9);getWindow().setNavigationBarColor(0xfff4f8ff);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 web=new WebView(this);setContentView(web);WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setGeolocationEnabled(true);settings.setMediaPlaybackRequiresUserGesture(true);
 web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri url=request.getUrl();return !("https".equals(url.getScheme())&&"git-96.github.io".equals(url.getHost()));}});
 web.setWebChromeClient(new WebChromeClient(){
 @Override public void onPermissionRequest(PermissionRequest request){runOnUiThread(()->{if(!request.getOrigin().getHost().equals("git-96.github.io")){request.deny();return;}for(String resource:request.getResources()){if(PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});}else{pendingMic=request;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC);}return;}}request.deny();});}
 @Override public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback){if(!origin.startsWith("https://git-96.github.io")){callback.invoke(origin,false,false);return;}if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){callback.invoke(origin,true,false);}else{pendingGeo=callback;pendingGeoOrigin=origin;requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},GEO);}}
 });
 web.loadUrl("https://git-96.github.io/GIT-Chinese-Trainer/?v=25");
 }
 @Override public void onRequestPermissionsResult(int code,@NonNull String[] permissions,@NonNull int[] results){super.onRequestPermissionsResult(code,permissions,results);boolean ok=results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED;if(code==MIC&&pendingMic!=null){if(ok)pendingMic.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});else pendingMic.deny();pendingMic=null;}if(code==GEO&&pendingGeo!=null){boolean allowed=false;for(int x:results)if(x==PackageManager.PERMISSION_GRANTED)allowed=true;pendingGeo.invoke(pendingGeoOrigin,allowed,false);pendingGeo=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}