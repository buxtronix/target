package net.cactii.target;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NewGameActivity extends Activity {
  
  // Area for new game widgets
  //private CheckBox newGameFromSMH;
  //private TextView newGameSMHLabel;
  private RadioGroup newGameWordCount;
  private CheckBox newGameTimed;
  private TextView newGameTimedLabel;
  private Button newGameStart;
  private Button newGameHelp;
  private Button newGameMenu;
  private TextView newGameDictLabel;
  
  public SharedPreferences preferences;
  public SharedPreferences.Editor prefeditor = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // For now, a specific layout (fullscreen portrait)
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.newgame);
    //this.newGameFromSMH = (CheckBox)findViewById(R.id.newGameFromSMH);
    //this.newGameSMHLabel = (TextView)findViewById(R.id.newGameSMHLabel);
    this.newGameWordCount = (RadioGroup)findViewById(R.id.newGameWordCount);
    this.newGameTimed = (CheckBox)findViewById(R.id.newGameTimed);
    this.newGameTimedLabel = (TextView)findViewById(R.id.newGameTimedLabel);
    this.newGameStart = (Button)findViewById(R.id.newGameStart);
    this.newGameHelp = (Button)findViewById(R.id.newGameHelp);
    this.newGameDictLabel = (TextView)findViewById(R.id.newGameDictLabel);
    
    this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
    this.prefeditor = this.preferences.edit();
    
    /*
    // Selecting SMH game deselects word count
    this.newGameFromSMH.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
          newGameWordCount.clearCheck();
      }
    });
    */
    // Selecting word count deselects smh
    this.newGameWordCount.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
    	  /*
        if (checkedId > -1)
          newGameFromSMH.setChecked(false);
          */
      }
    });
    // Clicking SMH label selects the checkbox
    /*
    this.newGameSMHLabel.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        newGameFromSMH.setChecked(!newGameFromSMH.isChecked());
      }
    });
    */
    this.newGameStart.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (newGameWordCount.getCheckedRadioButtonId() == -1) {
          Toast.makeText(NewGameActivity.this, "Please select word count.", Toast.LENGTH_SHORT).show();
          return;
        }
        prefeditor.putBoolean("timed_game", newGameTimed.isChecked());
        prefeditor.commit();
        Intent i = new Intent().
        putExtra("wordcount", newGameWordCount.getCheckedRadioButtonId()).
        putExtra("timed", newGameTimed.isChecked());
        setResult(Activity.RESULT_OK, i);
        finish();
      }
    });
    this.newGameHelp.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        openHelpDialog();
      }
    });

    this.newGameMenu = (Button)findViewById(R.id.newGameMenu);
    this.newGameMenu.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        NewGameActivity.this.openOptionsMenu();
      }
    });


    this.newGameTimed.setChecked(this.preferences.getBoolean("timed_game", true));
    this.newGameTimedLabel.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        newGameTimed.setChecked(!newGameTimed.isChecked());
      }
    });
    Message message = Message.obtain();
    message.what = DictionaryThread.MESSAGE_REREAD_DICTIONARY;
    DictionaryThread.currentInstance.messageHandler.sendMessage(message);
    newVersionCheck();
  }
  
  public void onResume() {
    String dictionary = PreferenceManager.getDefaultSharedPreferences(this).getString("dictpref", "2");
    if (dictionary.equals("0") || dictionary.equals("2")) {
      this.newGameDictLabel.setText("BRITISH words. (Change in options)");
    } else {
      this.newGameDictLabel.setText("AMERICAN words. (Change in options)");
    }
    super.onResume();
  }
  
  // The two methods below are duplicates from MainActivity
  // TODO: Merge back into one file
  private void openHelpDialog() {
    LayoutInflater li = LayoutInflater.from(this);
    View view = li.inflate(R.layout.aboutview, null); 
    new AlertDialog.Builder(NewGameActivity.this)
    .setTitle("Target Help")
    .setIcon(R.drawable.about)
    .setView(view)
    .setNeutralButton("Changes", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
          openChangesDialog();
      }
    })
    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
          //
      }
    })
    .show();  
    // TextView versionLabel = (TextView)findViewById(R.id.aboutVersionCode);
    // versionLabel.setText(getVersionName());
  }
  private void openChangesDialog() {
    LayoutInflater li = LayoutInflater.from(this);
    View view = li.inflate(R.layout.changeview, null); 
    new AlertDialog.Builder(NewGameActivity.this)
    .setTitle("Changelog")
    .setIcon(R.drawable.about)
    .setView(view)
    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
          //
      }
    })
    .show();  
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean supRetVal = super.onCreateOptionsMenu(menu);
    SubMenu menu_options = menu.addSubMenu(0, 0, 0, "Options");
    menu_options.setIcon(R.drawable.menu_options);
    return supRetVal;
  } 
  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    boolean supRetVal = super.onOptionsItemSelected(menuItem);
    switch (menuItem.getItemId()) {
    case 0 :
      startActivityForResult(new Intent(
          NewGameActivity.this, OptionsActivity.class), 0);
      break;
    }
    return supRetVal;
  }
  
  
  public void newVersionCheck() {
 
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor prefeditor = preferences.edit();
    
    int pref_version = preferences.getInt("currentversion", -1);
    int current_version = getVersionNumber();
    if (pref_version == -1 || pref_version != current_version) {
      prefeditor.putInt("currentversion", current_version);
      prefeditor.commit();
      this.openChangesDialog();
      return;
    }
  }
  
  public int getVersionNumber() {
    int version = -1;
      try {
          PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
          version = pi.versionCode;
      } catch (Exception e) {
          Log.e("Target", "Package name not found", e);
      }
      return version;
  }
  public String getVersionName() {
    String version = "";
      try {
          PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
          version = pi.versionName;
      } catch (Exception e) {
          Log.e("Target", "Package name not found", e);
      }
      return version;
  }
}
