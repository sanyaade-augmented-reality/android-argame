package edu.fsu.cs.argame;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import edu.fsu.cs.argame.R;

/**
 * Accesses the shared preferences file for this application and edits its values based on user actions.
 */
public class SettingsActivity extends Activity implements OnItemClickListener {

	private SharedPreferences arSettings;
	private SharedPreferences.Editor arSettingsEditor;
	private ListView setupListView;
	private ArrayList<String> setupValue;
	private Context context;
	
	/** Array containing all the setup names **/
	private TypedArray setupTitle;
	private WakeLock mWakeLock;
	private String TAG = "Settings";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setup);
        
		context = this;
		setupValue = new ArrayList<String>();
		setupTitle = this.getResources().obtainTypedArray(R.array.setup_title);
		arSettings = this.getSharedPreferences(InitActivity.AR_SETTINGS_FILENAME, MODE_WORLD_WRITEABLE);
        arSettingsEditor = arSettings.edit();
        
		getSettings();
		setupListView = (ListView) findViewById(R.id.setup_lv);
		setupListView.setAdapter(new ListViewAdapter(context));
		setupListView.setOnItemClickListener(this);
		
		PowerManager lPwrMgr = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = lPwrMgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
	}
	
	/**
	 * Accesses current settings from the shared preferences.
	 */
	public void getSettings() {
		setupValue.add(arSettings.getString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DEFAULT_DOWNLOAD_TYPE));
		int radius = arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS);
		setupValue.add((radius>=1000)?(radius/1000)+"km":radius+"m");
		setupValue.add(String.valueOf(arSettings.getInt(InitActivity.SETUP_ANGLE, InitActivity.DEFAULT_ANGLE))+getResources().getString(R.string.setup_dialog_degree));
		setupValue.add(arSettings.getString(InitActivity.SETUP_SEARCH_TAB, InitActivity.DEFAULT_SEARCH_TAB));
		setupValue.add("");
		
	}

	public class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
	    
	    public ListViewAdapter(Context c) {
	        mInflater = LayoutInflater.from(c);
	    }
	    
	    public int getCount() {
	        return setupTitle.length();
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.setup_listview, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.setup_title);
                holder.value = (TextView) convertView.findViewById(R.id.setup_value);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(setupTitle.getString(position));
            holder.value.setText(setupValue.get(position));

            return convertView;
	    }

        class ViewHolder {
            TextView title;
            TextView value;
        }
        
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert;
		builder.setTitle(setupTitle.getString(position));
		int checked = 0;
		
		switch(position) {
			case 0: 
				for(int i = 0 ; i < InitActivity.DOWNLOAD_TYPE.length; i++)
					if(arSettings.getString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DEFAULT_DOWNLOAD_TYPE).equals(InitActivity.DOWNLOAD_TYPE[i])) {
						checked = i;
						break;
					}
				
				builder.setSingleChoiceItems(InitActivity.DOWNLOAD_TYPE, checked, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        arSettingsEditor.putString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DOWNLOAD_TYPE[item]);
				        updateSettingValues();
				        dialog.dismiss();
				    }
				});
				break;
			case 1: 
				for(int i = 0 ; i < InitActivity.SEARCH_RADIUS.length; i++)
					if(arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS) == InitActivity.SEARCH_RADIUS[i]) {
						checked = i;
						break;
					}
				
				builder.setSingleChoiceItems(InitActivity.SEARCH_RADIUS_STR, checked, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        arSettingsEditor.putInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.SEARCH_RADIUS[item]);
				        updateSettingValues();
				        dialog.dismiss();
				    }
				});
				break;
			case 2: 
				for(int i = 0 ; i < InitActivity.ANGLE.length; i++)
					if(arSettings.getInt(InitActivity.SETUP_ANGLE, InitActivity.DEFAULT_ANGLE) == InitActivity.ANGLE[i]) {
						checked = i;
						break;
					}
				
				builder.setSingleChoiceItems(InitActivity.ANGLE_STR, checked, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        arSettingsEditor.putInt(InitActivity.SETUP_ANGLE, InitActivity.ANGLE[item]);
				        updateSettingValues();
				        dialog.dismiss();
				    }
				});
				break;
			case 3:  
				for(int i = 0 ; i < InitActivity.SEARCH_TAB.length; i++)
					if(arSettings.getString(InitActivity.SETUP_SEARCH_TAB, InitActivity.DEFAULT_SEARCH_TAB).equals(InitActivity.SEARCH_TAB[i])) {
						checked = i;
						break;
					}
				
				builder.setSingleChoiceItems(InitActivity.SEARCH_TAB, checked, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        arSettingsEditor.putString(InitActivity.SETUP_SEARCH_TAB, InitActivity.SEARCH_TAB[item]);
				        updateSettingValues();
				        dialog.dismiss();
				    }
				});
				break;
			case 4:  
				for(int i = 0 ; i < InitActivity.OPTION_RESET.length; i++)
					if(arSettings.getString(InitActivity.SETUP_OPTION_RESET, InitActivity.DEFAULT_OPTION_RESET) == InitActivity.OPTION_RESET[i]) {
						checked = i;
						break;
					}
				builder.setMessage(getResources().getString(R.string.setup_dialog_option_reset));
				builder.setPositiveButton(getResources().getString(R.string.setup_dialog_yes),  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						arSettingsEditor.putString(InitActivity.SETUP_OPTION_RESET, InitActivity.OPTION_RESET[0]);
						resetSettings();
				        updateSettingValues();
				        dialog.dismiss();
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.setup_dialog_no),  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        updateSettingValues();
				        dialog.dismiss();
					}
				});
				break;
			default: break;
			
		}

		alert = builder.create();
		alert.show();
	}
	
	/** Updates the shared preferences values **/
	private void updateSettingValues() {
		setupValue.clear();
        arSettingsEditor.commit();
        getSettings();
        setupListView.invalidateViews();
	}
	
	/** Resets to default setting values */
	private void resetSettings() {
        arSettingsEditor.putString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DEFAULT_DOWNLOAD_TYPE);
        arSettingsEditor.putInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS);
        arSettingsEditor.putInt(InitActivity.SETUP_ANGLE, InitActivity.DEFAULT_ANGLE);
        arSettingsEditor.putString(InitActivity.SETUP_SEARCH_TAB, InitActivity.DEFAULT_SEARCH_TAB);
        arSettingsEditor.putString(InitActivity.SETUP_OPTION_RESET, InitActivity.DEFAULT_OPTION_RESET);
        arSettingsEditor.putInt(InitActivity.SETUP_STARTUP_REMEMBER, InitActivity.DEFAULT_REMEMBER);        
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