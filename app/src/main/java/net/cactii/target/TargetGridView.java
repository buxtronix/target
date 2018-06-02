/*
 * A view that describes a nine letter target grid.
 */
package net.cactii.target;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TargetGridView extends View implements OnTouchListener {

  public TargetGridView.LetterTouchedHandler _letterTouchedHandler = null;
  
  public abstract class LetterTouchedHandler {
    public abstract void handleLetterTouched(int index);
  }
  
  // Initialise some colours
  private static final int backgroundColor = 0x00FFFFFF;
  private static final int gridColor = Color.BLACK;
  private static final int centerBackgroundColor = 0x80000000;
  private static final int centerLetterColor = Color.WHITE;
  private static final int letterColor = Color.BLACK;
  private static final int letterHighlightColor = 0x90FFFF00;
  private static final int middleHighlightColor = 0x90BFBF00;
  private static final double gridFill = 1.0;

  // Set paint objects
  private Paint backgroundPaint;		// Overall background
  private Paint gridPaint;			// Grid lines
  private Paint letterPaint;			// Outside letters
  private Paint centerLetterPaint;	// Center letter
  private Paint centerPaint;			// Center background
  private Paint highlightPaint;		// Square highlight
  private Paint middleHighlightPaint; // Middle square's highlight
  private int currentWidth; // Current width of view

  // An array to indicate which letters are displayed as highlighted
  private boolean[] highlights = {
      false, false, false, false, false,
      false, false, false, false};

  // Array of grid indices of selected letters
  private int[] selectedword = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1};

  // Variable to indicate the letters on the grid.
  private String letters = "";

  public boolean gameActive = false;
  
  public Activity mContext;

  public TargetGridView(Context context) {
    super(context);
    initTargetView();
  }
  public TargetGridView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initTargetView();
  }
  public TargetGridView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initTargetView();
  }
  protected void initTargetView() {
    //setFocusable(true);

    this.letters = "";
    this.gridPaint = new Paint();
    this.gridPaint.setColor(gridColor);

    this.backgroundPaint = new Paint();
    this.backgroundPaint.setColor(backgroundColor);

    this.letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.letterPaint.setColor(letterColor);
    this.letterPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

    this.centerPaint = new Paint();
    this.centerPaint.setColor(centerBackgroundColor);

    this.centerLetterPaint = new Paint();
    this.centerLetterPaint.setColor(centerLetterColor);
    this.centerLetterPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

    this.highlightPaint = new Paint();
    this.highlightPaint.setColor(letterHighlightColor);
    
    this.middleHighlightPaint = new Paint();
    this.middleHighlightPaint.setColor(middleHighlightColor);
    
    this.currentWidth = 0;

    this.setOnTouchListener(this);
    this.gameActive = false;
  }
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Our target grid is a square, measuring 90% of the minimum dimension
    int measuredWidth = measure(widthMeasureSpec);
    int measuredHeight = measure(heightMeasureSpec);

    int dim = Math.min(measuredWidth, measuredHeight);

    setMeasuredDimension(dim, dim);
  }
  private int measure(int measureSpec) {

    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);

    if (specMode == MeasureSpec.UNSPECIFIED)
      return 180;
    else
      return (int)(specSize * gridFill);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int size = getMeasuredWidth();
    int offset = (int)((double)size * (1-gridFill)/2);

    if (size != this.currentWidth) {
      this.centerLetterPaint.setTextSize((int)(size/5.5));
      this.letterPaint.setTextSize((int)(size/5.5));
      this.gridPaint.setStrokeWidth(size/50 + 1);
      this.currentWidth = size;
    }

    canvas.drawARGB(0, 255, 255, 255);

    // Draw the letters.
    if (!this.letters.equals(""))
      for (int index = 0 ; index < 9 ; index++)
        drawLetter(canvas, index, this.highlights[index]);
    // Draw the x grid lines.
    for (float x = 0 ; x <= size ; x += size/3)
      canvas.drawLine(x, 0, x, size-1, this.gridPaint);
    // Draw the y grid lines.
    for (float y = 0 ; y <= size ; y += size/3)
      canvas.drawLine(0, y, size-1, y, this.gridPaint);

  }

  // Draws a single letter in the grid, with appropriate highlight
  protected void drawLetter(Canvas canvas, int index, boolean highlighted) {
    Paint textPaint;
    Paint squarePaint;

    int size = getMeasuredWidth(); // Measure one as its a square

    String letter = this.letters.substring(index, index+1);
    float squareLeft = (index % 3) * size/3;
    float squareTop = (float) Math.floor(index/3) * size/3;
    float squareSize = size/3;
    float letterWidth = this.letterPaint.measureText(letter);
    float letterHeight = this.letterPaint.ascent();

    if (!highlighted) {
      if (index == 4) {
        textPaint = this.centerLetterPaint;
        squarePaint = this.centerPaint;
      } else {
        textPaint = this.letterPaint;
        squarePaint = this.backgroundPaint;
      }
    } else {
      if (index == 4)
        squarePaint = this.middleHighlightPaint;
      else
        squarePaint = this.highlightPaint;
      textPaint = this.letterPaint;
    }

    canvas.drawRect(squareLeft, squareTop,
        squareLeft + squareSize, squareTop + squareSize, squarePaint);
    canvas.drawText(letter,
        squareLeft + squareSize/2 - letterWidth/2,
        squareTop + squareSize/2 - letterHeight/2, textPaint);
  }

  // Supplies a new word to the grid.
  // Arg must be a 9 letter string, filling the grid L-R, T-B
  // Words that arent 9 letters are ignored.
  public void setLetters(String word) {
    if (word.length() == 9) {
      this.letters = word;
      clearGrid(); // Calls invalidate() for us.
    }
  }

  // Clears (unhighlights) the most recently selected letter from the grid.
  public void clearLastLetter() {
    int gridIndex;
    for (int i = 8 ; i >= 0 ; i--) {
      gridIndex = this.selectedword[i];
      if (gridIndex != -1) {
        this.highlights[gridIndex] = false;
        this.selectedword[i] = -1;
        invalidate();
        return;
      }
    }
  }

  // Unhighlights the entire grid
  public void clearGrid() {
    this.highlights = new boolean[] {false, false, false, false,
        false, false, false, false, false};
    this.selectedword = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1};
    invalidate();
  }

  public void shuffleGrid() {
    char[] letterArray = this.letters.toCharArray();
    char magicLetter = this.letters.charAt(4);
    char[] otherLetters = new char[8];
    int j = 0;
    for (int i = 0 ; i < 9 ; i++) {
      if (i != 4) {
        otherLetters[j] = letterArray[i];
        j++;
      }
    }

    Random rnd = new Random();
    for (int i = otherLetters.length - 1; i > 0; i--)
    {
      int index = rnd.nextInt(i + 1);
      // Simple swap
      char a = otherLetters[index];
      otherLetters[index] = otherLetters[i];
      otherLetters[i] = a;
    }

    this.letters = "";
    for (int i = 0 ; i < 8 ; i++) {
      this.letters = this.letters.concat(new String(Character.toString(otherLetters[i])));
      if (i == 3) {
        this.letters = this.letters.concat(new String(Character.toString(magicLetter)));
      }
    }
    invalidate();
    Log.d("target", "Letters are: " + otherLetters.toString());
  }

  // Returns the string of the currently tapped out word
  public String getSelectedWord() {
    String word = "";
    int gridIndex;
    for (int i = 0 ; i < 9 ; i++) {
      gridIndex = this.selectedword[i];
      if (gridIndex > -1)
        word += this.letters.substring(gridIndex, gridIndex+1);
      else
        return word;
    }

    return word;
  }

  public void setLetterTouchedListener(LetterTouchedHandler handler) {
    this._letterTouchedHandler = handler;
  }

  // Handles touch events to the grid.
  //
  // Marks the letter to be highlighted, and updates the
  // ordered list of selected letters.
  //
  // Finally calls the LetterTouchedHandler for further actions.
  public boolean onTouch(View v, MotionEvent event) {
    if (this.gameActive == false)
      return false;
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN : {
        int gridIndex = eventToLetterIndex(event);
        if (this.highlights[gridIndex]) {
          unSelectIfLastLetter(gridIndex);
          return true; // return if letter already highlighted
        }
        this.highlights[gridIndex] = true;
        invalidate();
        for (int i = 0 ; i < 9 ; i++) {
          if (this.selectedword[i] == -1) {
            this.selectedword[i] = gridIndex;
            break;
          }
        }
        if (this._letterTouchedHandler != null)
          this._letterTouchedHandler.handleLetterTouched(gridIndex);
        return true;
      }
    }
    return false;
  }
  
  public boolean onTrackballEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    Log.d("target", "Trackball: x " + x + " y " + y);
    return true;
  }
  
  private void unSelectIfLastLetter(int gridIndex) {
    int lastIndex = -2;
    int i;
    for (i = 0 ; i < 9 ; i++) {
      if (this.selectedword[i] == -1) {
        lastIndex = i-1;
        break;
      }
    }
    if (lastIndex == -2 && i == 9)
      lastIndex = 8;

    if (this.selectedword[lastIndex] == gridIndex) {
      clearLastLetter();
      this._letterTouchedHandler.handleLetterTouched(gridIndex);
    }
  }

  // Takes an onTouch event and returns the grid index of the touched letter.
  private int eventToLetterIndex(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    int size = getMeasuredWidth(); // Measure one side only as its a square

    int row = (int)((size - (size-y))/(size/3));
    if (row > 2) row = 2;
    if (row < 0) row = 0;

    int col = (int)((size - (size-x))/(size/3));
    if (col > 2) col = 2;
    if (col < 0) col = 0;

    int index = row*3 + col;

    // Log.d("Target", "Row " + row + ", col " + col + ", index " + index);
    return index;
  }
}
