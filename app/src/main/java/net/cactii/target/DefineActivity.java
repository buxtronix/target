package net.cactii.target;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DefineActivity extends Activity {
  
  private TextView headerView;
  private TextView defineView;
  private LinearLayout scroller;
  // Thread handling definition lookups
  public Thread dictcThread;
  
  public String wordToDefine;
  
  public static DefineActivity currentInstance;
    
  private static void setCurrent(DefineActivity current){
    DefineActivity.currentInstance = current;
  }
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // For now, a specific layout (fullscreen portrait)
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
    setContentView(R.layout.define);
    DefineActivity.setCurrent(this);
    this.headerView = (TextView)findViewById(R.id.defineHeader);
    this.defineView = (TextView)findViewById(R.id.defineText);
    this.scroller = (LinearLayout)findViewById(R.id.defineScroll);
    // Handles searching for definitions
    Bundle extras = getIntent().getExtras();
    this.wordToDefine = extras.getString("net.cactii.target.wordToDefine");

    if (this.wordToDefine == null)
      finish();
    else {
      DictClient dictc = new DictClient();
      dictc.wordToDefine = this.wordToDefine;
      this.dictcThread = new Thread(dictc);
      this.dictcThread.start();
      this.headerView.setText("Definition search for: " + dictc.wordToDefine);
    }
  }
  
  public Handler handler = new Handler() {
    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
      ArrayList<DictClient.Definition> definitions;
      if (msg.arg1 == DictClient.RESULT_FOUND) {
        definitions = (ArrayList<DictClient.Definition>)msg.obj;
        scroller.removeView(defineView);
        for (DictClient.Definition def : definitions) {
          TextView tv = new TextView(DefineActivity.this);
          tv.setText("From: " + def.source);
          tv.setBackgroundColor(0x30000000);
          tv.setTextColor(0xFF000000);
          tv.setTypeface(Typeface.DEFAULT_BOLD);
          tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                             LayoutParams.WRAP_CONTENT));
          scroller.addView(tv);
          
          TextView tv1 = new TextView(DefineActivity.this);
          tv1.setText("Word: " + def.word);
          tv1.setBackgroundColor(0x15000000);
          tv1.setTextColor(0xFF000000);
          tv1.setTypeface(Typeface.DEFAULT_BOLD);
          tv1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                             LayoutParams.WRAP_CONTENT));
          scroller.addView(tv1);
          
          TextView tv2 = new TextView(DefineActivity.this);
          tv2.setText(def.definition);
          tv2.setBackgroundColor(0x00000000);
          tv2.setTextColor(0xFF000000);
          tv2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                             LayoutParams.WRAP_CONTENT));
          scroller.addView(tv2);
        }

      }
      else if (msg.arg1 == DictClient.RESULT_NOTFOUND)
        defineView.setText("No definition found.");
      else
        defineView.setText("Error fetching definition");
      
      final Button gb = new Button(DefineActivity.this);
      gb.setText("Definition via Google");
      gb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                         LayoutParams.WRAP_CONTENT));
      gb.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          Intent myIntent = null;
          myIntent = new Intent(DefineActivity.this, GoogleDefineActivity.class);
          myIntent.putExtra("net.cactii.target.wordToDefine", wordToDefine.toLowerCase());
          startActivity(myIntent);
        }
        
      });
      scroller.addView(gb);
    }
  };
}
