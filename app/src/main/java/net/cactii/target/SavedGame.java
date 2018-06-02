package net.cactii.target;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class SavedGame {
  
  private MainActivity activity;
  
  public SavedGame(MainActivity activity) {
    this.activity = activity;
  }
  
  public SavedGame() {
	  
  }

  // Save the current game state
  // Simple text file..
  // First line is nine letter word
  // Second line is shuffled version
  // Other lines are valid words, until @@PLAYERWORDS@@
  // Then the rest are the player's words
  public void Save() {
    BufferedWriter writer = null;
    if (DictionaryThread.currentInstance.validWords == null ||
        DictionaryThread.currentInstance.validWords.size() < 1) {
      Log.d("Target", "No game to save");
      return;
    }
    try {
      writer = new BufferedWriter(new FileWriter(this.activity.getFilesDir().getPath() + MainActivity.saveFilename));
      if (this.activity.targetGrid.gameActive)
        writer.write("active\n");
      else
        writer.write("inactive\n");
      writer.write(this.activity.countDown.initialTime + "\n");
      writer.write(this.activity.countDown.remainingTime + "\n");
      writer.write(DictionaryThread.currentInstance.currentNineLetter.word + "\n");
      writer.write(DictionaryThread.currentInstance.currentNineLetter.shuffled + "\n");
      for (String word : DictionaryThread.currentInstance.validWords)
        writer.write(word + "\n");
      writer.write("@@PLAYERWORDS@@\n");
      for (PlayerWord word : this.activity.playerWords) {
        if (word.result != PlayerWord.RESULT_HEADER &&
            word.result != PlayerWord.RESULT_MISSED)
          writer.write(word.word + "\n");
      }
    } catch (IOException e) {
      Log.d("Target", "Error saving game: "+e.getMessage());
    }
    finally {
      try {
        if (writer != null)
          writer.close();
      } catch (IOException e) {
        //pass
      }
    }
    Log.d("Target", "Successfully saved game.");
  }

  // restore from the above saved file
  // Returns whether a game was successfully restored
  public boolean Restore(String filename) {
    String line = null;
    BufferedReader br = null;
    InputStream ins = null;
    
    String currentNineLetter = "";
    String currentShuffled = "";
    ArrayList<String> validWords = new ArrayList<String>();
    ArrayList<PlayerWord> playerWords = new ArrayList<PlayerWord>();
    String activeState = "";
    String initialTime = "0";
    String remainingTime = "0";

    try {
      ins = new FileInputStream(new File(filename));
      br = new BufferedReader(new InputStreamReader(ins), 8192);
      activeState = br.readLine();
      initialTime = br.readLine();
      remainingTime = br.readLine();
      currentNineLetter = br.readLine();
      currentShuffled = br.readLine();

      boolean fetchingPlayerWords = false;
      while((line = br.readLine())!=null) {
        String word = line.trim();
        if (fetchingPlayerWords) {
          PlayerWord playerWord = new PlayerWord(word);
          playerWords.add(playerWord);
        } else {
          if (word.equals("@@PLAYERWORDS@@")) {
            fetchingPlayerWords = true;
            continue;
          }
          validWords.add(word);
        }
      }
    } catch (FileNotFoundException e) {
      Log.d("Target", "FNF Error restoring game: " + e.getMessage());
    } catch (IOException e) {
      Log.d("Target", "IO Error restoring game: " + e.getMessage());
    }
    finally {
      try {
        ins.close();
        br.close();
      } catch (Exception e) {
        // Nothing.
      }
    }

    // Validate data read from the savedgame file
    if (currentNineLetter.length() != 9 ||
        currentShuffled.length() != 9 ||
        validWords.size() < 1 ||
        !activeState.contains("active")) {
      Log.d("Target", "Error restoring game");
      return false;
    }

    // Populate the game with the words, letter, etc.
    DictionaryThread.currentInstance.currentNineLetter = new NineLetterWord(currentNineLetter);
    DictionaryThread.currentInstance.currentNineLetter.setShuffledWord(currentShuffled);
    this.activity.targetGrid.setLetters(currentShuffled);
    DictionaryThread.currentInstance.validWords = validWords;
    this.activity.InitPlayerWords();
    for (PlayerWord word : playerWords)
      this.activity.playerWords.add(word);
    
    // Restore the UI state
    this.activity.playerWordsAdapter.notifyDataSetChanged();
    this.activity.setGameState(true);
    if (this.activity.preferences.getBoolean("livescoring", true)) {
      this.activity.showWordCounts(this.activity.countCorrectWords());
    } else
      this.activity.showWordCounts(this.activity.CountPlayerWords());
    if (activeState.equals("inactive")) {
      this.activity.setGameState(false);
      this.activity.scoreAllWords();
      this.activity.enteredWordBox.setText("TIME'S UP!");
    } else
      this.activity.setGameState(true);
    
    // Reconfigures the countdown timer to the last saved time
    this.activity.countDown.end();
    this.activity.timeRemaining.setText("");
    this.activity.enteredWordBox.setText("");
    if (Integer.parseInt(remainingTime) > 0) {
      this.activity.countDown.enabled = true;
      this.activity.countDown.begin(Integer.parseInt(initialTime),
                                    Integer.parseInt(remainingTime));
    }
    Log.d("Target", "Restored game successfully.");
    return true;
  }
  
  public SavedGameState RestoreGrid(String filename) {
	    String line = null;
	    BufferedReader br = null;
	    InputStream ins = null;
        ArrayList<PlayerWord> playerWords;
        ArrayList<String> validWords;
	    
	    String currentNineLetter = "";
	    String currentShuffled = "";
	    playerWords = new ArrayList<PlayerWord>();
	    validWords = new ArrayList<String>();
	    try {
	        ins = new FileInputStream(new File(filename));
	        br = new BufferedReader(new InputStreamReader(ins), 8192);
	        br.readLine();
	        br.readLine();
	        br.readLine();
	        currentNineLetter = br.readLine();
	        currentShuffled = br.readLine();
	        boolean fetchingPlayerWords = false;
	        while((line = br.readLine())!=null) {
	          String word = line.trim();
	          if (fetchingPlayerWords) {
	            PlayerWord playerWord = new PlayerWord(word);
				playerWords.add(playerWord);
	          } else {
	            if (word.equals("@@PLAYERWORDS@@")) {
	              fetchingPlayerWords = true;
	              continue;
	            }
	            validWords.add(word);
	          }
	        }
	    } catch (FileNotFoundException e) {
	        Log.d("Target", "FNF Error restoring game: " + e.getMessage());
	      } catch (IOException e) {
	        Log.d("Target", "IO Error restoring game: " + e.getMessage());
	      }
	      finally {
	        try {
	          ins.close();
	          br.close();
	        } catch (Exception e) {
	          // Nothing.
	        }
	      }
	      // Validate data read from the savedgame file
	      if (currentNineLetter.length() != 9 ||
	          currentShuffled.length() != 9) {
	        Log.d("Target", "Error restoring game");
	        return null;
	      }
	    SavedGameState sgs = new SavedGameState();
	    sgs.validWords = validWords.size() + 1; // Add 1 for nine-letter
	    sgs.playerWords = playerWords.size();
	    sgs.currentShuffled = currentShuffled;
	    return sgs;
  }
  
  public class SavedGameState {
	  int validWords;
	  int playerWords;
	  String currentShuffled;
  }
}
