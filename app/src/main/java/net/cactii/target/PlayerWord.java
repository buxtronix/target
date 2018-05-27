package net.cactii.target;

public class PlayerWord implements Comparable<PlayerWord> {
  public static final int RESULT_UNSCORED = 0; // Word yet to be scored
  public static final int RESULT_OK = 1;       // Word is valid
  public static final int RESULT_INVALID = 2;  // Word not found in dict
  public static final int RESULT_MISSED = 3;   // A word the player didnt get
  public static final int RESULT_HEADER = 4;   // Just a list header line
  
  public String word; // Text of the player's word
  public int result;  // Word correctness
  public boolean willAnimate; // Will animated in the list
  
  public PlayerWord(String word) {
    super();
    this.word = word;
    this.result = RESULT_UNSCORED;
    this.willAnimate = false;
  }
  
  public int compareTo(PlayerWord word) {
    if (this.result == RESULT_HEADER) {
      return -1;
    } else if (word.result == RESULT_HEADER)
      return 1;
    return this.word.compareTo(word.word);
  }
}
