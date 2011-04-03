//Benjamin Salazar, Trent, Rick
//FSUfm
//Project #2
//EEL4930
package edu.fsu.cs.argame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;



public class splash extends MixView{

	private static final int STOPSPLASH = 0;//time in milliseconds
	private static final long SPLASHTIME = 4000;	
	private ImageView splash;	//handler for splash screen
	private Handler splashHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				//remove SplashScreen from view
				splash.setVisibility(View.GONE);
				startActivity(new Intent(splash.this, menu.class));
				splash.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splash);
		splash = (ImageView) findViewById(R.id.Splash);
		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.splash);
		splash = (ImageView) findViewById(R.id.Splash);
		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);	}
}