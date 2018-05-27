package net.cactii.target;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class DictClient implements Runnable {

  public static final String DICT_SERVER = "216.93.242.2";
  public static final int DICT_PORT = 2628;
  public static final String DICT_DATABASE = "gcide";
  public static final String DICT_CLIENT_NAME = "Android Target http://android-target.googlecode.com";

  public static final int RESULT_FOUND = 0;
  public static final int RESULT_NOTFOUND = 1;
  public static final int RESULT_ERROR = 2;
  
  private Socket socket;

  private String response;
  private ArrayList<Definition> definitions;
  public String wordToDefine;
 
  public void run() {

    Log.d("DictC", "Will define: " + wordToDefine);
    Message msg_resp = Message.obtain();
    msg_resp.what = DictionaryThread.MESSAGE_HAVE_DEFINITION;
    msg_resp.arg1 = GetRawDefinitions();
    msg_resp.obj = definitions;
    DefineActivity.currentInstance.handler.sendMessage(msg_resp);

  }
  
  public int GetRawDefinitions() {
    int result = RESULT_ERROR;
    this.response = "";
    try {
      this.socket = new Socket(DICT_SERVER, DICT_PORT);
   // Create input and output streams to socket
      PrintStream out = new PrintStream( this.socket.getOutputStream()) ;
      DataInputStream in = new DataInputStream(this.socket.getInputStream());
      out.println("client \"" + DICT_CLIENT_NAME + "\"");
      out.println(String.format("define " + DICT_DATABASE + " \"%s\"", this.wordToDefine));
      String line;
      while ((line = in.readLine()) != null) {
        // Log.d("DictC", "Response: " + line);
        if (line.startsWith("1")) {
          result = RESULT_FOUND;
          this.definitions = ProcessResults(in, line);
          out.println("quit");
        }
        if (line.startsWith("5")) {
          out.println("quit");
          result = RESULT_NOTFOUND;
        }
        this.response += line + "\n"; 
      }
      Log.d("Target", "Closed socket");
      this.socket.close();
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  private ArrayList<Definition> ProcessResults(DataInputStream in, String line) throws IOException {
    // int numDefs = Integer.parseInt(line.split(" ")[1]);
    ArrayList<Definition> definitions = new ArrayList<Definition>();
    Definition definition = null;
    boolean readingDefinition = false;
    while ((line = in.readLine()) != null) {
      if (line.startsWith("151")) {
        definition = new Definition();
        definition.word = line.split("\"")[1];
        definition.source = line.split("\"")[3];
        definition.definition = "";
        readingDefinition = true;
        continue;
      }
      if (readingDefinition) {
        if (line.equals(".")) {
          readingDefinition = false;
          definitions.add(definition);
          continue;
        } else {
          definition.definition += line + "\n";
        }
      }
      if (line.startsWith("250")) {
        break;
      }
    }
    Log.d("Target", "Fetched " + definitions.size() + " defs.");
    return definitions;
  }
  
  public class Definition {
    // Actual word this definition is for
    public String word;
    // Where the definition was found
    public String source;
    // String of this definition
    public String definition;
  }
}
