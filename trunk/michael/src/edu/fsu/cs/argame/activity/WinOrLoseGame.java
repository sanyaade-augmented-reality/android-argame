package edu.fsu.cs.argame.activity;

import edu.fsu.cs.argame.R;
import net.peterd.zombierun.constants.BundleConstants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class WinOrLoseGame extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getIntent().getExtras().getBoolean(BundleConstants.GAME_WON)) {
      setContentView(R.layout.win);
    } else {
      setContentView(R.layout.lose);
    }

    Button newGameButton = (Button) findViewById(R.id.button_new_game);
    final Intent mainMenuIntent = new Intent(this, Main.class);
    newGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          startActivity(mainMenuIntent);
        }
      });

    //Util.configureAds(this);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      startActivity(new Intent(this, Main.class));
      return true;
    }
    return false;
  }
}
