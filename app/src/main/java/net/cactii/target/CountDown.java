package net.cactii.target;

import java.util.Date;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CountDown {
  
  // Abstract class for the coutdown listener
  public abstract class CountDownTimeExpiredListener {
    public abstract void OnCountDownTimeExpired();
  }
  
  public static final int COUNTDOWN_PING = 0;
  
  // The MainActivity that this class is part of
  public MainActivity main;
  // Thread which 'pings' us every 0.1 sec
  private Thread timerThread;
  // View which is the visible progress bar
  private ProgressBar progressBar;
  // Initial countdown duration, in 100ms increments
  public int initialTime;
  // Remaining countdown, in 100ms increments
  public int remainingTime;
  private TextView timeRemainingLabel;
  // Whether coutdown is enabled for the game
  public boolean enabled;
  // If countdown is active
  public boolean active;
  // Tick timer (ie keeps track of realtime
  public long lastRealTime;
  // Listener for countdown expired
  public CountDownTimeExpiredListener _countDownExpiredListener = null;
  
  CountDown(MainActivity activity) {
    this.main = activity;
    this.progressBar = this.main.countDownBar;
    this.timeRemainingLabel = this.main.timeRemaining;
    this.initialTime = 0;
    this.remainingTime = 0;
    this.active = false;
    this.enabled = false;
  }
  
  // Start the countdown timer.
  // Called on the start of a new game.
  public void begin(int initialTime, int remainingTime) {
    if (!this.enabled) {
      this.main.timeRemaining.setVisibility(View.INVISIBLE);
      this.progressBar.setVisibility(View.INVISIBLE);
      return;
    }
    this.main.timeRemaining.setVisibility(View.VISIBLE);
    this.progressBar.setVisibility(View.VISIBLE);
    if (initialTime == 0) {
      int numWords = DictionaryThread.currentInstance.validWords.size();
      this.initialTime = numWords * 40;
      this.remainingTime = this.initialTime;
      this.lastRealTime = new Date().getTime();
    } else {
      this.initialTime = initialTime;
      this.remainingTime = remainingTime;
    }
    this.active = true;
    startCountdown();
    this.progressBar.setMax(1000);
    updateProgressBar();
    this.progressBar.setVisibility(View.VISIBLE);
  }
  
  // Finish the countdown. Resets things and kills the
  // ping thread.
  public void end() {
    if (!this.enabled) return;
    remainingTime = 0;
    this.progressBar.setVisibility(View.INVISIBLE);
    updateProgressBar();
    this.active = false;
    if (timerThread != null)
      timerThread.interrupt();
  }
  
  public void pause() {
    this.active = false;
  }
  
  public void resume() {
    this.active = true;
  }
  
  // Extend the time by 6*wordLen
  // Called after successfully submitting a word.
  public int addWord(int wordLen) {
    if (!this.enabled || !this.active) return 0;
    int timeAdded = wordLen * 50;
    this.remainingTime += timeAdded;
    if (this.remainingTime > this.initialTime)
      this.initialTime = this.remainingTime;
    updateProgressBar();
    return timeAdded/10;
  }
  
  // Called when the coutdown timer expires
  private void timerExpired() {
    Toast.makeText(this.main, "Out of time!", Toast.LENGTH_LONG).show();
    if (this._countDownExpiredListener != null)
      this._countDownExpiredListener.OnCountDownTimeExpired();
  }
 
  // Set handler for when countdown timer is expired.
  public void setCountDownTimeExpiredListener(CountDownTimeExpiredListener handler) {
    this._countDownExpiredListener = handler;
  }
  
  private void updateProgressBar() {
    if (this.initialTime > 0) {
      int percent = (this.remainingTime * 1000) / this.initialTime;
      updateProgressHue();
      this.progressBar.setProgress(percent);
    }
  }
  
  private void updateTimeRemainingLabel() {
    int seconds = this.remainingTime / 10;
    int minutes = seconds / 60;
    seconds = seconds - (minutes*60);
    this.timeRemainingLabel.setText(String.format("%d:%02d", minutes, seconds));
  }
  
  private void updateProgressHue() {
    int progress = this.progressBar.getProgress();
    float green;
    float red;
    float alpha;
    if (progress > 500) {
      green = 0xFF;
      red = 255 * ((float)(1000-progress) / 500);
    } else {
      green = 255 * ((float)progress/500);
      red = 255;
    }
    alpha = (255 * ((float)(1000-progress) / 1000))/3;
    int color = ((int)alpha << 24) + ((int)red << 16) + ((int)green << 8) ;
    this.progressBar.setBackgroundColor(color);
  }
  
  public Handler timerHandler = new Handler() {
    public void handleMessage(Message msg) {
      if (CountDown.this.active && CountDown.this.enabled) {
        Date curTime = new Date();
        if (curTime.getTime() - lastRealTime < 500)
          return;
        lastRealTime = curTime.getTime();
        remainingTime-=5;
        if (remainingTime < 1) {
          end();
          timerExpired();
        } else {
          updateProgressBar();
        }
        updateTimeRemainingLabel();
      } else if (!CountDown.this.enabled) {
        remainingTime = initialTime = 0;
      }
    }
  };
  
  public void startCountdown() {
    this.timerThread = new Thread (new Runnable() {
      public void run() {
        boolean completed = false;
        while (!completed) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            completed = true;
          }
          if (Thread.currentThread().isInterrupted())
            completed = true;
          if (!completed)
            CountDown.this.timerHandler.sendEmptyMessage(0);
        }
      }
    });
    this.timerThread.start();
  }
}
