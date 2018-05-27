package net.cactii.target;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

public class GoogleDefineActivity extends Activity {
  
  private TextView headerView;
  private WebView googleWebView;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // For now, a specific layout (fullscreen portrait)
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
    setContentView(R.layout.googledefine);
    
    this.headerView = (TextView)findViewById(R.id.googleDefineHeader);
    this.googleWebView = (WebView)findViewById(R.id.googleView);
    Bundle extras = getIntent().getExtras();
    String wordToDefine = extras.getString("net.cactii.target.wordToDefine");
    this.headerView.setText("Google definition for: " + wordToDefine);
    this.googleWebView.loadUrl("http://www.google.com/search?q=define:" + wordToDefine);
  }
}
