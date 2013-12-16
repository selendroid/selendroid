package io.selendroid.androiddriver;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		WebView webview = (WebView) findViewById(R.id.webview);
		webview.loadData(
				"<html><body>" +
			    "<h1 id='AndroidDriver'>Android Driver</h1>" +
				"</body></html>", 
				"text/html", "UTF-8");
	}
}
