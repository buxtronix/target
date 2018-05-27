package net.cactii.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ListAdapter;

public class SavedGameList extends ListActivity {
	private SavedGameListAdapter mAdapter;
	public boolean mCurrentSaved;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mAdapter = new SavedGameListAdapter(this);
		this.getListView().setBackgroundColor(0xFFFFFFFF);
		setListAdapter(this.mAdapter);
	}
	
	public void DeleteGame(final String filename) {
		LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.deletegame, null); 
		new AlertDialog.Builder(SavedGameList.this)
        .setTitle("Delete Saved Game?")
        .setView(view)
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	//
                }
        })
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
            		new File(filename).delete();
            		SavedGameList.this.mAdapter.refreshFiles();
            		SavedGameList.this.mAdapter.notifyDataSetChanged();
                }
        })
        .show();
	}
	
	public void LoadGame(String filename) {
        Intent i = new Intent().putExtra("filename", filename);
        setResult(Activity.RESULT_OK, i);
        finish();
	}
	
    void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
	
	public void saveCurrent() {
		this.mCurrentSaved = true;
		int fileIndex;
		for (fileIndex = 0 ; ; fileIndex++)
			if (! new File(SavedGameListAdapter.SAVEDGAME_DIR + "/savedgame_" + fileIndex).exists())
				break;
		String filename = SavedGameListAdapter.SAVEDGAME_DIR + "/savedgame_" + fileIndex;
		try {
			this.copy(new File(SavedGameListAdapter.SAVEDGAME_DIR + "/savedgame"), new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mAdapter.refreshFiles();
		this.mAdapter.notifyDataSetChanged();
	}
}
