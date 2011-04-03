package edu.fsu.cs.argame.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    startActivity(new Intent(this, MainMenuWithMultiplayerDisabled.class));
  }
}
