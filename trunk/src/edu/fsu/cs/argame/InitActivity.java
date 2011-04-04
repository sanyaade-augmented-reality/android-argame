package edu.fsu.cs.argame;

import org.mixare.MixView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import edu.fsu.cs.argame.R;

/**
 * The class that displays the start up screen and creates a sharedPreferences file for the application.
 * It adds the default values for setting types at first application run.
 */
public class InitActivity extends Activity implements OnClickListener {
	
	public static final int NOT_STARTED = 0;
	private final int END_SPLASH = 1;
	public static int AR_MAP_STATE = NOT_STARTED;
	
	/** contains the splash screen image **/
	private LinearLayout splash1;
	
	/** LinearLayout that contains the usage information screen and the checkbox for remembering an action **/
	private LinearLayout splash2;
	private Button button;
	private CheckBox checkbox;
	private Context context;
	
	/** String keys for accessing shared preferences values **/
	public final static String SETUP_DOWNLOAD_TYPE = "download_type";
	public final static String SETUP_SEARCH_RADIUS = "search_radius";
	public final static String SETUP_ANGLE = "angle";
	public final static String SETUP_SEARCH_TAB = "search_tab";
	public final static String SETUP_OPTION_RESET = "option_reset";
	public final static String SETUP_STARTUP_REMEMBER = "startup_remember";
	
	/******************** Constants to be put in sharedPrefs file ***********************************/
	
	/** Constants used for Startup Remember **/
	public final static int NO = 0;
	public final static int YES = 1;
	
	/** Constants used for Search Radius **/
	public final static int RADIUS_100m = 100;
	public final static int RADIUS_500m = 500;
	public final static int RADIUS_1km = 1000;
	public final static int RADIUS_2km = 2000;
	
	/** Constants used for Angle **/
	public final static int ANGLE_40 = 40;
	public final static int ANGLE_50 = 50;
	public final static int ANGLE_60 = 60;
	public final static int ANGLE_80 = 80;
	
	/** Constants used for Download Type **/
	public final static String DOWNLOAD_AUTO = "Auto Download";
	public final static String DOWNLOAD_MANUAL = "Manual Download";

	/** Constants used for Option Reset **/
	public final static String RESET_NO = "Don't reset settings";
	public final static String RESET_YES = "Reset to defaults";

	/** Constants used for Search Tabs **/
	public static String TAB_NEARBY = "Nearby";
	public static String TAB_CATEGORY = "Categories";
	public static String TAB_FAVORITE = "Favorites";
	
	/** The contants that contain the default values for sharedPrefs file, default values are inserted in the sharedPreferences file at first application run **/
	public final static int DEFAULT_SEARCH_RADIUS = RADIUS_1km;
	public final static int DEFAULT_ANGLE = ANGLE_50;
	public final static int DEFAULT_REMEMBER = NO;
	public final static String DEFAULT_DOWNLOAD_TYPE = DOWNLOAD_AUTO;
	public final static String DEFAULT_OPTION_RESET = RESET_NO;
	public final static String DEFAULT_SEARCH_TAB = TAB_NEARBY;

	/** Arrays that contain all possible values in each type **/
	public final static int[] SEARCH_RADIUS = {RADIUS_100m, RADIUS_500m, RADIUS_1km, RADIUS_2km};
	public final static int[] ANGLE = {ANGLE_40, ANGLE_50, ANGLE_60, ANGLE_80};
	public final static String[] DOWNLOAD_TYPE = {DOWNLOAD_AUTO, DOWNLOAD_MANUAL};
	public final static String[] OPTION_RESET = {RESET_YES, RESET_NO};
	public final static String[] SEARCH_TAB = {TAB_NEARBY, TAB_CATEGORY, TAB_FAVORITE};

	public final static String[] SEARCH_RADIUS_STR = {RADIUS_100m+" m", RADIUS_500m+" m", (RADIUS_1km/1000)+" km", (RADIUS_2km/1000)+" km"};
	public final static String[] ANGLE_STR = {ANGLE_40+"°", ANGLE_50+"°", ANGLE_60+"°", ANGLE_80+"°"};

	/** filename of the shared_preferences file used**/
	public final static String AR_SETTINGS_FILENAME = "ARSettings";
	private SharedPreferences arSettings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        context = this;
        
        initViews();
        initSettings();
        splashThread().start();
    }
    
    /** Intitalizes strings and all views used in the activity **/
    private void initViews() {
    	
    	TAB_NEARBY = getResources().getString(R.string.setup_search_tab_nearby);
        TAB_CATEGORY = getResources().getString(R.string.setup_search_tab_category);
        TAB_FAVORITE = getResources().getString(R.string.setup_search_tab_favorite);
        
    	splash1 = (LinearLayout) findViewById(R.id.splash1);
    	splash2 = (LinearLayout) findViewById(R.id.splash2);   
    	checkbox = (CheckBox) findViewById(R.id.usage_checkbox);
    	button = (Button) findViewById(R.id.usage_button);
    	button.setOnClickListener(this);
    }
    
   /** A thread that displays the splash screen for 3 secs **/
    private Thread splashThread() {
		return new Thread(new Runnable(){   			
			public void run() {    		
				try {
					for(int i = 0; i < 7; i++) Thread.sleep(500);
					handler.sendEmptyMessage(END_SPLASH);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});    
    }

    public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			splash1.setVisibility(View.GONE);
			/* checks the shared_prefs if the startup screen is set to be shown or not */
			if(arSettings.getInt(SETUP_STARTUP_REMEMBER, NO) == 0) splash2.setVisibility(View.VISIBLE);
			else {
				AR_MAP_STATE = END_SPLASH;
				Intent i = new Intent(context, MixView.class);
				startActivity(i);
				finish();
			}
		}
    };
    
    /** Opens or creates a sharedPreferences file. It adds the default values for setting types at first application run.
	 *  <br/>
	 *  <b>Setting Types: </b>
	 *  <ul>
	 *  <li>SETUP_DOWNLOAD_TYPE</li>
	 *  <li>SETUP_SEARCH_RADIUS</li>
	 *  <li>SETUP_ALARM, SETUP_ANGLE</li>
	 *  <li>SETUP_SEARCH_TAB</li>
	 *  <li>SETUP_OPTION_RESET</li>
	 *  <li>SETUP_STARTUP_REMEMBER</li>
	 *  <ul>
	 *  **/
	private void initSettings() {
    	arSettings = this.getSharedPreferences(AR_SETTINGS_FILENAME, MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor arSettingsEditor = arSettings.edit();
        
        /* checks if settings already exist */
        if(arSettings.contains(SETUP_DOWNLOAD_TYPE)) return;
        
        /* adds default setting value */
        arSettingsEditor.putString(SETUP_DOWNLOAD_TYPE, DEFAULT_DOWNLOAD_TYPE);
        arSettingsEditor.putInt(SETUP_SEARCH_RADIUS, DEFAULT_SEARCH_RADIUS);
        arSettingsEditor.putInt(SETUP_ANGLE, DEFAULT_ANGLE);
        arSettingsEditor.putString(SETUP_SEARCH_TAB, DEFAULT_SEARCH_TAB);
        arSettingsEditor.putString(SETUP_OPTION_RESET, DEFAULT_OPTION_RESET);
        arSettingsEditor.putInt(SETUP_STARTUP_REMEMBER, DEFAULT_REMEMBER);
        arSettingsEditor.commit();
        
    }

	@Override
	public void onClick(View v) {
		if(v == button) {
			
			/* edits the shared_pref file if the checkbox has been checked */
			if(checkbox.isChecked()) {
				SharedPreferences.Editor arSettingsEditor = arSettings.edit();
				arSettingsEditor.putInt(SETUP_STARTUP_REMEMBER, YES);
				arSettingsEditor.commit();
			}

			AR_MAP_STATE = END_SPLASH;
			Intent i = new Intent(context, MixView.class);
			startActivity(i);
			finish();
		}
	}
    
}