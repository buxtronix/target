package net.cactii.target;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DictionaryThread implements Runnable {

  public static final int MESSAGE_GET_NINELETTER = 0;
  public static final int MESSAGE_HAVE_NINELETTER = 1;
  public static final int MESSAGE_GET_MATCHING_WORDS= 2;
  public static final int MESSAGE_HAVE_MATCHING_WORDS= 3;
  public static final int MESSAGE_DICTIONARY_READY= 4;
  public static final int MESSAGE_REREAD_DICTIONARY = 5;
  public static final int MESSAGE_GET_SMH_NINELETTER = 6;
  public static final int MESSAGE_HAVE_SMH_NINELETTER = 7;
  public static final int MESSAGE_FAIL_SMH_NINELETTER = 8;
  public static final int MESSAGE_FAIL_SMH_NINELETTER_NOTFOUND = 9;
  public static final int MESSAGE_HAVE_DEFINITION = 10;

  private int nineLetterDictionary = 2;
  private int currentDictionary = 2;
  
  // List of all nine letter words
  private ArrayList<NineLetterWord> nineLetterWords;
  // List of valid words for current Nine letter
  public ArrayList<String> validWords;

  public NineLetterWord currentNineLetter;

  public static DictionaryThread currentInstance = null;
  public Handler messageHandler;

  private static void setCurrent(DictionaryThread current){
    DictionaryThread.currentInstance = current;
  }

  public void run() {
    Looper.prepare();
    DictionaryThread.setCurrent(this);
    nineLetterWords = new ArrayList<NineLetterWord>();
//    getDictionary();
//    getNineLetterWords(R.raw.nineletterwords_common);	
//    getNineLetterWords(nineLetterDictionary);
    
    Message message = Message.obtain();
    message.what = MESSAGE_DICTIONARY_READY;
    MainActivity.currentInstance.newWordReadyHandler.sendMessage(message);

    // Handler must be created in the thread (ie inside the run() method)
    messageHandler = new Handler() {
      public void handleMessage(Message msg) {
        switch(msg.what) {
          case MESSAGE_GET_NINELETTER : {
            
            int minSize = msg.arg1;
            int maxSize = msg.arg2;
            int attempts = 0;
            do {
              int random = (int) (Math.random() * nineLetterWords.size()-1);
              currentNineLetter = nineLetterWords.get(random);
              if (attempts++ > 100)
                break;
            } while (NineLetterWord.shuffleWithRange(currentNineLetter, minSize, maxSize) == false);
            // Send message back saying we have the word
            Message message = Message.obtain();
            message.what = MESSAGE_HAVE_NINELETTER;
            message.obj = currentNineLetter.shuffled;
            MainActivity.currentInstance.newWordReadyHandler.sendMessage(message);
            break;
          }
          case MESSAGE_GET_MATCHING_WORDS : {
            // Find words matching current nine letter (shuffled)
            validWords = new ArrayList<String>();
            getAllMatchingWords();
            
            // Send notification back to main thread
            Message message = Message.obtain();
            message.what = MESSAGE_HAVE_MATCHING_WORDS;
            MainActivity.currentInstance.newWordReadyHandler.sendMessage(message);
            break;
          }
          case MESSAGE_REREAD_DICTIONARY : {
            // Dictionary selection has changed, reload
            getDictionary();
            nineLetterWords.clear();
            getNineLetterWords(R.raw.nineletterwords_common); 
            getNineLetterWords(nineLetterDictionary);
          }
        }
      }
    };
  Looper.loop();
  }

  // Fetch all nine letter words from the dictionary, populates this.nineLetterWords.
  //
  // The nineletter dictionary is a binary file. It consists of alternating nine-byte
  // sequences. The first sequence is the 9 letter word (ascii), and the second
  // sequence is an array of bytes, each byte representing the number of word
  // matches with that letter as the 'magic' letter.
  private void getNineLetterWords(int dictionary) {
    InputStream is = MainActivity.currentInstance.getResources().openRawResource(dictionary);
    DataInputStream in = new DataInputStream(is);
    byte[] wordRecord = new byte[9];
    byte[] wordRecordCount = new byte[9];
    try {
      while (in.read(wordRecord) == 9) {
        in.read(wordRecordCount);
        nineLetterWords.add(new NineLetterWord(wordRecord, wordRecordCount));
        wordRecord = new byte[9];
        wordRecordCount = new byte[9];
      }
    } catch (IOException e) {
      // pass
    }
    Log.d("Target", "Read all 9 letter words.");
  }

  // From 'currentNineLetterWord', with an unknown word (ie only the
  // scrambled word), attempt to find the word. Returns boolean,
  // whether or not a word was found.
  private boolean findNineLetterWord() {
    for (NineLetterWord word : this.nineLetterWords) {
      word.word = new String(word.wordArray);
      if (isValidWord(word.word)) {
        currentNineLetter.word = word.word;
        return true;
      }
    }
    return false;
  }
  
  // Dictionary is split into letters for faster searching
  private void getAllMatchingWords() {
    
    // First initialise an array for letters we've already searched.
    char[] searchedLetters = new char[9];
    for (int i = 0 ; i < 9 ; i++)
      searchedLetters[i] = '\0';
    
    // Now go thru nineletters letters and see if we've
    // previously looked for those letters.
    for (int i = 0 ; i < 9 ; i++) {
      char letter = currentNineLetter.word.charAt(i);
      boolean searchedLetterAlready = false;
      for (int j = 0 ; j < 9 ; j++) {

        if (searchedLetters[j] == letter) {
          searchedLetterAlready = true;
          break;
        } else if (searchedLetters[j] == '\0') {
          searchedLetters[j] = letter;
          break;
        }
      }
      if (searchedLetterAlready)
        continue;
      // New letter, find some words. Only split common words out,
      // as that is the most time consuming.
      switch(letter) {
        case 'A':
          getMatchingWords(R.raw.words_common_a);
          break;
        case 'B':
          getMatchingWords(R.raw.words_common_b);
          break;
        case 'C':
          getMatchingWords(R.raw.words_common_c);
          break;
        case 'D':
          getMatchingWords(R.raw.words_common_d);
          break;
        case 'E':
          getMatchingWords(R.raw.words_common_e);
          break;
        case 'F':
          getMatchingWords(R.raw.words_common_f);
          break;
        case 'G':
          getMatchingWords(R.raw.words_common_g);
          break;
        case 'H':
          getMatchingWords(R.raw.words_common_h);
          break;
        case 'I':
          getMatchingWords(R.raw.words_common_i);
          break;
        case 'J':
          getMatchingWords(R.raw.words_common_j);
          break;
        case 'K':
          getMatchingWords(R.raw.words_common_k);
          break;
        case 'L':
          getMatchingWords(R.raw.words_common_l);
          break;
        case 'M':
          getMatchingWords(R.raw.words_common_m);
          break;
        case 'N':
          getMatchingWords(R.raw.words_common_n);
          break;
        case 'O':
          getMatchingWords(R.raw.words_common_o);
          break;
        case 'P':
          getMatchingWords(R.raw.words_common_p);
          break;
        case 'Q':
          getMatchingWords(R.raw.words_common_q);
          break;
        case 'R':
          getMatchingWords(R.raw.words_common_r);
          break;
        case 'S':
          getMatchingWords(R.raw.words_common_s);
          break;
        case 'T':
          getMatchingWords(R.raw.words_common_t);
          break;
        case 'U':
          getMatchingWords(R.raw.words_common_u);
          break;
        case 'V':
          getMatchingWords(R.raw.words_common_v);
          break;
        case 'W':
          getMatchingWords(R.raw.words_common_w);
          break;
        case 'X':
          getMatchingWords(R.raw.words_common_x);
          break;
        case 'Y':
          getMatchingWords(R.raw.words_common_y);
          break;
        case 'Z':
          getMatchingWords(R.raw.words_common_z);
          break;
      }
    }
    getMatchingWords(currentDictionary);
  }

  // Get all words matching currentNineLetter and magicLetter
  private void getMatchingWords(int dictionary) {
    InputStream is = MainActivity.currentInstance.getResources().openRawResource(dictionary);
    BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8192);
    String word;
    try {
      /*
      previousWord = rd.readLine();
      previousWord = previousWord.trim();
      if (isValidWord(previousWord))
        validWords.add(previousWord);
      
      while((word = rd.readLine())!=null) {
        word = word.trim();
        previousCount = Integer.parseInt(word.substring(0, 1));
        newWord = previousWord.substring(0, previousCount) +
                word.substring(1);
        if (isValidWord(newWord))
          validWords.add(newWord);
        previousWord = newWord;
      }
      */

      while((word = rd.readLine())!=null) {
        word = word.trim();
        if (isValidWord(word))
          validWords.add(word);
      }
      is.close();
    } catch (IOException e) {
      //pass
    }
    Log.d("Target", "Found matches, " + validWords.size() + " words.");
  }

  // Determines if a given word matches the current nine letter
  private boolean isValidWord(String word) {
    if (!word.contains(currentNineLetter.magicLetter))
      return false;

    char checkingLetter;
    int i;
    int j;
    char[] wordArray = word.toCharArray();
    int wordLength = wordArray.length;
    currentNineLetter.array = currentNineLetter.word.toCharArray();

    for (i = 0 ; i < 9 ; i++) {
      checkingLetter = currentNineLetter.array[i];
      for (j = 0 ; j < wordLength ; j++) {
        if (wordArray[j] == checkingLetter) {
          wordArray[j] = '0';
          break;
        }
      }
    }
    for (i = 0 ; i < wordLength ; i++) {
      if (wordArray[i] != '0')
        return false;
    }
    // Log.d("Target", "Found word: " + word);
    return true;
  }
  
  private void getDictionary() {
    String dict = PreferenceManager.getDefaultSharedPreferences(MainActivity.currentInstance).getString("dictpref", "2");
    if (dict.equals("2")) {
      Toast.makeText(MainActivity.currentInstance, "Select your dictionary in Options, defaulting to UK.", Toast.LENGTH_LONG).show();
      dict = "0";
    }
    if (dict.equals("0")) {
      this.nineLetterDictionary = R.raw.nineletterwords_uk;
      this.currentDictionary = R.raw.words_uk;
      Log.d("Target", "Reading British dictionary");
    }
    else if (dict.equals("1")) {
      this.nineLetterDictionary = R.raw.nineletterwords_us;
      this.currentDictionary = R.raw.words_us;
      Log.d("Target", "Reading American dictionary");
    }
  }

}
