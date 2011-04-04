package edu.fsu.cs.argame;

import org.mixare.MixContext;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.widget.TabHost;
import edu.fsu.cs.argame.R;

/**
 * A TabActivity that contains three different activities (Nearby, Category, Favorites)
 */
public class InventoryTabActivity extends TabActivity {
	
	public static String TAB_NEARBY;
	public static String TAB_CATEGORY;
	public static String TAB_FAVORITE;
	public static String TAB = "tab"; 
	public static String FAVORITE = "favorite"; 
	private String nearbyCategory = "";
	private String TAG = "TabMode";
	private boolean favoriteTab = false;
	
	private SharedPreferences arSettings;
	private WakeLock mWakeLock;
	private TabHost tabHost;
    private TabHost.TabSpec spec;
	
	protected MixContext ctx;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tab_mode);

		arSettings = this.getSharedPreferences(InitActivity.AR_SETTINGS_FILENAME, MODE_WORLD_WRITEABLE);
        
		TAB_NEARBY = getResources().getString(R.string.setup_search_tab_nearby);
        TAB_CATEGORY = getResources().getString(R.string.setup_search_tab_category);
        TAB_FAVORITE = getResources().getString(R.string.setup_search_tab_favorite);
        
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
        initTabs();

		PowerManager lPwrMgr = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = lPwrMgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
		
		if(favoriteTab) tabHost.setCurrentTab(2);
        else if(nearbyCategory != null && !nearbyCategory.equals("")) tabHost.setCurrentTab(0);
        else {
	        for(int i = 0; i < InitActivity.SEARCH_TAB.length; i++)
				if(InitActivity.SEARCH_TAB[i].equals(arSettings.getString(InitActivity.SETUP_SEARCH_TAB, InitActivity.DEFAULT_SEARCH_TAB))) {
					tabHost.setCurrentTab(i);
					break;
				}
        }
	}
	
	/** Initializes tab values **/
	private void initTabs() {

        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();
        Bundle bundle3 = new Bundle();
        Intent intent1 = new Intent(this, SearchActivity.class);
        Intent intent2 = new Intent(this, SearchActivity.class);
        Intent intent3 = new Intent(this, SearchActivity.class);
        
        try {
        	Bundle b = getIntent().getExtras();
	        if(b.getString(SearchActivity.CATEGORY_VAL) != null) {
	        	nearbyCategory = b.getString(SearchActivity.CATEGORY_VAL);
	        } else if(b.getBoolean(FAVORITE) != false) {
	        	favoriteTab = b.getBoolean(FAVORITE);
	        }
        } catch (Exception e) {}
        
		bundle1.putString(TAB, TAB_NEARBY);
		bundle1.putString(SearchActivity.CATEGORY_VAL, nearbyCategory);
        intent1.putExtras(bundle1);
        spec = tabHost.newTabSpec(TAB_NEARBY).setIndicator(TAB_NEARBY, getResources().getDrawable(R.drawable.tap_icon_search)).setContent(intent1);
        tabHost.addTab(spec);
        
        bundle2.putString(TAB, TAB_CATEGORY);
        intent2.putExtras(bundle2);
        spec = tabHost.newTabSpec(TAB_CATEGORY).setIndicator(TAB_CATEGORY, getResources().getDrawable(R.drawable.tap_icon_category)).setContent(intent2);
        tabHost.addTab(spec);

        bundle3.putString(TAB, TAB_FAVORITE);
        intent3.putExtras(bundle3);
        spec = tabHost.newTabSpec(TAB_FAVORITE).setIndicator(TAB_FAVORITE, getResources().getDrawable(R.drawable.tap_icon_favorite)).setContent(intent3);
        tabHost.addTab(spec);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			this.mWakeLock.release();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			this.mWakeLock.acquire();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
