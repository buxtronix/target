package net.cactii.target;

import java.util.HashMap;

import android.util.Log;

public class NineLetterWord {
  // The unscrambled word.
  public byte[] wordArray;
  public String word;
  // The scrambled word, element 4 is the 'magic' word
  public String shuffled;
  // Array representation of the scrambled word.
  public char[] array;
  // Map of valid word counts for a given magic Letter.
  public HashMap<String, Integer> wordCounts;
  public byte[] wordCountsArray;
  // The current magic letter.
  public String magicLetter;
  // Line from file
  public String wordFileLine;
  
  // Constructor for just a word
  public NineLetterWord(String line) {
    this.word = line;
    this.magicLetter = this.word.substring(4, 5);
  }
  
  // Constructor for a pair of byte arrays
  public NineLetterWord(byte[] wordRecord, byte[] wordRecordCount) {
    this.wordArray = wordRecord;
    this.wordCountsArray = wordRecordCount;
  }

  private void populateWordCounts() {
    this.wordCounts = new HashMap<String, Integer>();
    for (int i = 0 ; i < 9 ; i++) {
      byte[] letter = new byte[1];
      letter[0] = this.wordArray[i];
      this.wordCounts.put(new String(letter), Integer.valueOf(this.wordCountsArray[i]));
    }
  }
  
  // Shuffles, iff we can shuffle word with valid word
  // count between lower and upper.
  // Returns boolean, if ranges could be satisfied.
  public static boolean shuffleWithRange(NineLetterWord nlw, int lower, int upper) {
    nlw.word = new String(nlw.wordArray);
    shuffle(nlw);
    Log.d("Target", "Shuffling word: " + nlw.word);
    nlw.populateWordCounts();
    if (nlw.wordCounts.size() == 0)
      return true; // Be on the safe side
    String letter = "";
    // First, find out if we have a combination with valid words in the range.
    for (int i = 0 ; i < 9 ; i++) {
      letter = nlw.shuffled.substring(i, i+1);
      if (nlw.wordCounts.get(letter) >= lower && nlw.wordCounts.get(letter) <= upper)
        break;
      letter = "";
    }
    if (letter.equals(""))
      return false; // None found, return
    // At this point, 'letter' is a magic Letter valid with the supplied range.
    // Shuffle until the magic letter is the one we want.
    String original = nlw.shuffled.substring(4, 5);
    nlw.shuffled = nlw.shuffled.replaceFirst(letter, original);
    nlw.shuffled = nlw.shuffled.substring(0, 4).concat(letter).concat(nlw.shuffled.substring(5, 9));
    nlw.magicLetter = letter;
    return true;
  }
  
  // Sets the shuffled word, and associated variables, to the
  // supplied string.
  public void setShuffledWord(String shuffledWord) {
    this.shuffled = shuffledWord;
    this.array = this.shuffled.toCharArray();
    this.magicLetter = this.shuffled.substring(4, 5);
  }
  
  // Public shuffle, shuffled and sets this.shuffled
  public static String shuffle(NineLetterWord nlw) {
    nlw.setShuffledWord(NineLetterWord.shuffleWord(nlw.word));
    return nlw.shuffled;
  }
  
  // Shuffles the letters in a word, returns the shuffled result
  private static String shuffleWord(String my_word){
    if (my_word.length() <= 1)
      return my_word;
    
    int split=my_word.length()/2;

    String temp1=shuffleWord(my_word.substring(0,split));
    String temp2=shuffleWord(my_word.substring(split));

    if (Math.random() > 0.5) 
      return temp1 + temp2;
    else 
      return temp2 + temp1;
  }
}