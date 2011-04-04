package edu.fsu.cs.argame;

import java.util.ArrayList;
import java.util.Collections;

import org.mixare.DataView;
import org.mixare.MixContext;
import org.mixare.MixState;
import org.mixare.MixView;
import org.mixare.data.ARMapDB;
import org.mixare.data.XMLHandler;

import edu.fsu.cs.argame.marker.Marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * The activity that displays the downloaded markers in a listview depending on the given mode.<br>
 * It has 5 modes:
 * <ul><li>TAB_NEARBY - displays up to 200 nearby markers</li>
 * <li>TAB_CATEGORY - displays all the categories</li>
 * <li>TAB_FAVORITES - displays all favorites from the database</li>
 * <li>CATEGORY MODE - displays all markers with the same category selected from the previous activity</li>
 * <li>NEARBY_CATEGORY MODE - displays all categories. This mode has radioButtons which can be selected and deselected</li></ul>
 */

public class SearchActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {

	/** Contains all markers displayed on the listview in Nearby Search Tab **/
	private ArrayList<Marker> nearbyMarkers;
	
	/** Contains all markers displayed on the listview after choosing a category in Category Search Tab **/
	private ArrayList<Marker> categoryMarkers;
	
	/** Contains all markers displayed on the listview in Favorites Search Tab **/
	private ArrayList<Favorites> favorites;
	
	/** Contains the list of main categories **/
	private TypedArray categoryArray;

	/** Contains the resId list of each category, mapped with subCategoryArray **/
	private TypedArray catIconArray;
	
	/** The listview that contain the markers in all Search Tabs, the markers can be nearbyMarkers, categoryMarkers or favorites depending on the Search Tab chosen **/
	private ListView listView;
	
	/** The LinearLayout that contains the headerTitle, invisible in TabMode and visible in category modes **/
	private LinearLayout header;
	
	/** The TextView for the title, changes according to mode **/
	private TextView headerTitle;
	private TextView emptyTextView;
	private Context context;
	private ARMapDB dbHelper;
	private ProgressDialog dialog;
	
	/** String that determines the category of markers to be displayed in the listview, this changes according to users choice **/
	private String categoryValue = "";
	
	/** String that determines the current tab.<br><b>Search Tabs: </b><ul><li>TAB_NEARBY</li><li>TAB_CATEGORY</li><li>TAB_FAVORITE</li></ul>**/
	private String tabValue = "";
	
	/** Contains the first index of the subCategory mapped to the category array **/
	private final int[] subCategoryCount = {0, 13, 20, 24, 39, 52};
	
	/** Constants used in Handler **/
	private final int INVALIDATE_LV = 0;
	private final int LOAD_MARKERS = 1;
	private final int START_DIALOG = 2;
	private final int DISMISS_DIALOG = 3;
	private final int ENABLE_MENU = 4;
	private final int DISABLE_MENU = 5;

	/** Stores the position in the listView of the selected Favorite **/
	private int selectedFav;
	
	/** A flag for checking if Refresh menu is selected **/
	public static boolean nearbyDownload = false;
	
	/** Boolean array mapped with the selected category in Nearby Search Tab, true if the category is selected in the multilist, false otherwise 
	 * <br><br><b>default : </b>all categories are selected**/
	private boolean[] radioSelected = {true, true, true, true, true, true};
	
	/** A flag for checking if the state of the activity is in CATEGORY, the case when the user has chosen a category in the Category Search Tab, the listview should contain all the markers with that category **/
	private boolean CATEGORY = false;
	
	/** A flag for checking if the state of the activity is in NEARBY_CATEGORY, the case when the user selected Category in the menu button, the listview should contain all category list **/
	private boolean NEARBY_CATEGORY = false;
	
	private boolean stopThread = false;
	private MenuItem menuItemRefresh;
	
	/** Constants used for passing bundles **/
	private final String NEARBY_CAT_SELECT = "select_category";
	private final String RADIO_SELECTED = "selected_radio";
	public static final String CATEGORY_VAL = "category";
	public static final String FAV_EDIT = "fav_edit";
	
	public static final String DELIMITER = ":";
	public static final double ONE_KILOMETER = 1000;
	
	protected MixContext ctx;
	protected MixState state;
	
	private SharedPreferences arSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.poi_search);

		initComponents();
		initViews();
	}
	
	private void initComponents() {

        dbHelper = new ARMapDB(this);
		ctx = MixView.ctx;
        DataView dataView = ctx.getDataView();
        state = dataView.getState();
        
		context = this;
		nearbyMarkers = new ArrayList<Marker>();
		categoryMarkers = new ArrayList<Marker>();
		favorites = new ArrayList<Favorites>();

		categoryArray = this.getResources().obtainTypedArray(R.array.category_list);
		catIconArray = this.getResources().obtainTypedArray(R.array.category_drawable_big);
		
		arSettings = this.getSharedPreferences(InitActivity.AR_SETTINGS_FILENAME, MODE_WORLD_WRITEABLE);
	}
	
	/**
	 * Initializes all Views used in the activity and determines what action to do in the current Tab Mode.
	 */
	private void initViews() {

		header = (LinearLayout) findViewById(R.id.poi_search_header);
		headerTitle = (TextView) findViewById(R.id.poi_search_header_text);

		getBundles();

		if(CATEGORY) downloadCheckerThread().start();
		else if(NEARBY_CATEGORY);
		else if(tabValue.equals(InitActivity.TAB_NEARBY));
		else if(tabValue.equals(InitActivity.TAB_CATEGORY));
		else if(tabValue.equals(InitActivity.TAB_FAVORITE)) loadFavorites();
		
		emptyTextView = (TextView) findViewById(R.id.empty_view);
		listView = (ListView) findViewById(R.id.poi_search_lv);
		listView.setAdapter(new ListViewAdapter(context));
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}
	
	/**
	 * Accesses bundles passed by the previous activity, either intent from MixView to TabMode, TabMode to Category mode, or NearbySearchTab to NearbyCategory
	 */
	private void getBundles() {
		try {
	        Bundle b = getIntent().getExtras();
	        
	        /** MixView to TabMode case */
	        if(b.getString(InventoryTabActivity.TAB) != null) {
	        	tabValue = b.getString(InventoryTabActivity.TAB);
	        	return;
	        }
	        categoryValue = b.getString(CATEGORY_VAL);
	        
	        /** TabMode to Category mode*/
	        if(categoryValue != null) {
		        header.setVisibility(View.VISIBLE);
		        headerTitle.setText(categoryValue);
		        CATEGORY = true;
	        } else {
	        	try {
	        		/** NearbySearchTab to NearbyCategory */
	    	        NEARBY_CATEGORY = b.getBoolean(NEARBY_CAT_SELECT);
	    	        radioSelected = b.getBooleanArray(RADIO_SELECTED);
	    	        header.setVisibility(View.VISIBLE);
	    	        headerTitle.setText(getResources().getString(R.string.search_nearbycat_header));
	            } catch (Exception e1) {
	            	NEARBY_CATEGORY = false;
	            }
	        }
        } catch (Exception e) {
        	CATEGORY = false;
        }
	}
	
	/**
	 * Clears the current listView markers and gets the updated downloaded markers.
	 */
	private void loadNearbyMarkers() {
		nearbyMarkers.clear();
		categoryMarkers.clear();
		
		ArrayList<Marker> markers = ctx.getDataView().markers;

		if(CATEGORY) {
			/** Adds markers with category same to the categoryValue, the category selected in Category Search Tab*/
			for(int i = 0; i < markers.size() && categoryMarkers.size() < XMLHandler.MAX_POI; i++) {
				if(markers.get(i).getMainCategory().equals(categoryValue) &&
						markers.get(i).mGeoLoc.getDistance() <= arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS))
						categoryMarkers.add(markers.get(i));
			}	
			if(categoryMarkers.isEmpty() && MixState.nextLStatus == MixState.DONE) emptyTextView.setVisibility(View.VISIBLE);
			else emptyTextView.setVisibility(View.GONE);
		} else {
			/** Adds the marker if the current category is selected in the radioButton */
			for(int i = 0; i < markers.size() && nearbyMarkers.size() < XMLHandler.MAX_POI; i++) {
				for(int k = 0; k < radioSelected.length; k++) {
					if(radioSelected[k]) {
						if(markers.get(i).getMainCategory().equals(categoryArray.getString(k)) &&
								markers.get(i).mGeoLoc.getDistance() <= arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS))
							nearbyMarkers.add(markers.get(i));
					}
				}	
			}
			
		} 
		if(nearbyMarkers.isEmpty() && MixState.nextLStatus == MixState.DONE && !CATEGORY) emptyTextView.setVisibility(View.VISIBLE);
		else if(!CATEGORY) emptyTextView.setVisibility(View.GONE);
		
		if(MixState.nextLStatus == MixState.NOT_STARTED && MixView.ctx.getDownloader()!= null && MixView.ctx.getDownloader().isStop()) {
			if((!CATEGORY && nearbyMarkers.isEmpty()) || (CATEGORY && MixView.pressedMarker != null && !MixView.pressedMarker.getCategory().equals(categoryValue)))
				emptyTextView.setVisibility(View.VISIBLE);
			handler.sendEmptyMessage(DISMISS_DIALOG);
		}
		handler.sendEmptyMessage(INVALIDATE_LV);
				
	}
		
	/**
	 * Accesses all favorites from the database, adds it to the list used by the listview in Favorite Search Tab.
	 */
	private void loadFavorites() {
		favorites.clear();
        dbHelper.open();
		Cursor cursor;
		try {
			cursor = dbHelper.fetch(dbHelper.getDb(), null, null);
			if(cursor.moveToFirst()) {
				do {
					int id = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_ID));
					String title = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_TITLE));
					String titleCount = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_TITLE_COUNT));
					String phoneNum = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_PHONE_NUM));
					String category = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_CATEGORY));
					int categoryPos = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_CATEGORY_POS));
					double longitude = cursor.getDouble(cursor.getColumnIndex(ARMapDB.COLUMN_LONGITUDE));
					double latitude = cursor.getDouble(cursor.getColumnIndex(ARMapDB.COLUMN_LATITUDE));
					favorites.add(new Favorites(id, title, titleCount, phoneNum, category, categoryPos, longitude, latitude));
				} while(cursor.moveToNext());
			}
			dbHelper.close();
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Collections.sort(favorites, new FavoritesOrder());
	}
	
	private String getDistanceString(double distance) {
		 if(Math.round(distance) == 0) return getResources().getString(R.string.search_current_loc);
          else return ((distance >= ONE_KILOMETER)?(int)(distance/ONE_KILOMETER)+"."+Math.round((distance%ONE_KILOMETER)/100.0)+"km":Math.round(distance)+"m");
	}
	
	public class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
	    
	    public ListViewAdapter(Context c) {
	        mInflater = LayoutInflater.from(c);
	    }
	    
	    /**
	     * Returns the size of the marker used by the mode of the tab.
	     * @see android.widget.Adapter#getCount()
	     */
	    public int getCount() {
	    	if(CATEGORY) return categoryMarkers.size();
	    	else if(tabValue.equals(InitActivity.TAB_NEARBY) && !NEARBY_CATEGORY) return nearbyMarkers.size();
			else if(tabValue.equals(InitActivity.TAB_CATEGORY) || NEARBY_CATEGORY) return categoryArray.length();
			else if(tabValue.equals(InitActivity.TAB_FAVORITE)) return favorites.size();
			else return 0;
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
                convertView = mInflater.inflate(R.layout.poi_search_listview, null);

                holder = new ViewHolder();
                holder.poiImage = (ImageView) convertView.findViewById(R.id.poi_image);
                holder.poiName = (TextView) convertView.findViewById(R.id.poi_name);
                holder.poiNum = (TextView) convertView.findViewById(R.id.poi_number);
                holder.poiDistance = (TextView) convertView.findViewById(R.id.poi_distance);
                holder.radio = (RadioButton) convertView.findViewById(R.id.radio);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(CATEGORY) {
            	holder.poiImage.setImageBitmap(categoryMarkers.get(position).getBigIcon());
            	holder.poiName.setText(categoryMarkers.get(position).getmText());
 	            holder.poiDistance.setText(getDistanceString(categoryMarkers.get(position).mGeoLoc.getDistance()));
            } else if(tabValue.equals(InitActivity.TAB_NEARBY) && !NEARBY_CATEGORY) {
            	holder.poiImage.setImageBitmap(nearbyMarkers.get(position).getBigIcon());
	            holder.poiName.setText(nearbyMarkers.get(position).getmText());
 	            holder.poiDistance.setText(getDistanceString(nearbyMarkers.get(position).mGeoLoc.getDistance()));
            } else if(tabValue.equals(InitActivity.TAB_CATEGORY)) {
            	holder.poiImage.setImageResource(catIconArray.getResourceId(subCategoryCount[position],0));
            	holder.poiName.setText(categoryArray.getString(position));
            	holder.poiDistance.setText("");
            } else if(NEARBY_CATEGORY) {
            	holder.poiImage.setVisibility(View.GONE);
            	holder.poiName.setText(categoryArray.getString(position));
            	holder.poiName.setPadding(15, 30, 0, 20);
            	holder.poiDistance.setVisibility(View.GONE); 
            	holder.radio.setVisibility(View.VISIBLE);
            	if(radioSelected[position]) holder.radio.setChecked(true);
            } else if(tabValue.equals(InitActivity.TAB_FAVORITE)) {
            	holder.poiImage.setImageResource(catIconArray.getResourceId(favorites.get(position).categoryPos, categoryArray.length()-1));
            	holder.poiName.setText(favorites.get(position).title);
            	holder.poiNum.setText(favorites.get(position).phoneNum);
            	holder.poiDistance.setText(getDistanceString(favorites.get(position).distance));
            	holder.poiNum.setVisibility(View.VISIBLE);
            }
    		
            return convertView;
	    }

        class ViewHolder {
        	ImageView poiImage;
            TextView poiName;
            TextView poiNum;
            TextView poiDistance;
            RadioButton radio;
        }
        
	}

	/**
	 * Determines the action to be done when an listView item is clicked. Action depends on the Tab Mode.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		if(CATEGORY) {
			/** Passes through intent the category of the marker selected */
			Intent intent = new Intent(this, MixView.class);
			Bundle b = new Bundle();
			b.putString(CATEGORY_VAL, categoryValue);
			
			if (MixView.pressedMarker != null)
				MixView.pressedMarker.setTarget(false);
			
			MixView.pressedMarker = categoryMarkers.get(position);
			intent.putExtras(b);
			startActivity(intent);
		}
		else if(NEARBY_CATEGORY) {
			/* Manually selects and deselects the radioButton it flags the radioSelected array */
			RadioButton radio = (RadioButton) ((LinearLayout) arg1).getChildAt(3);
			
			if(radioSelected[position]) {
				radioSelected[position] = false;
				radio.setChecked(false);
			} else {
				radioSelected[position] = true;
				radio.setChecked(true);
			}			
		}
		else if(tabValue.equals(InitActivity.TAB_NEARBY)) {
			/** Passes through intent the category/categories selected. It concats all the categories in a single string delimited by ':'. It send SHOW_ALL when all categories are selected */
			Intent intent = new Intent(this, MixView.class);
			Bundle b = new Bundle();
			String categoryConcat = "";
			int count = 0;
			for(int k = 0; k < radioSelected.length; k++) {
				if(radioSelected[k]) {
					categoryConcat += categoryArray.getString(k) + DELIMITER;
					count++;
				}
			}

			if(count == categoryArray.length()) b.putString(CATEGORY_VAL, MixView.SHOW_ALL);
			else if(count == 0) b.putString(CATEGORY_VAL, "");
			else {
				if(categoryConcat.endsWith(DELIMITER))
					b.putString(CATEGORY_VAL, categoryConcat.substring(0, categoryConcat.length()-1));
			}
			intent.putExtras(b);
			if (MixView.pressedMarker != null)
				MixView.pressedMarker.setTarget(false);
			MixView.pressedMarker = nearbyMarkers.get(position);
			startActivity(intent);
			finish();
		}
		else if(tabValue.equals(InitActivity.TAB_CATEGORY)) {
			/** Passes through intent the category selected */
			Intent intent = new Intent(this, SearchActivity.class);
			Bundle b = new Bundle();
			b.putString(CATEGORY_VAL, categoryArray.getString(position));
			intent.putExtras(b);
			startActivity(intent);
		}
		else if(tabValue.equals(InitActivity.TAB_FAVORITE)) {
			/** Passes through intent the id of the favorite selected */
			Intent intent = new Intent(this, MixView.class);
			Bundle b = new Bundle();
			b.putString(MixView.CATEGORY, MixView.FAVORITES);
			b.putInt(MixView.FAVORITES_ID, favorites.get(position).id);
			
			intent.putExtras(b);
			startActivity(intent);
		}
		
	}

	/**
	 * A thread for checking the status of download. Stops only when the activity is in onPause state.
	 * @return The thread.
	 */
	private Thread downloadCheckerThread() {
		return new Thread(new Runnable(){   	
			
			public void run() {    			 	
				int millisecond = 300;
				if(MixState.nextLStatus == MixState.PROCESSING) nearbyDownload = true;
				while(!stopThread) {
					switch (MixState.nextLStatus) {
						case MixState.NOT_STARTED:
							handler.sendEmptyMessage(ENABLE_MENU);
						case MixState.PROCESSING:
							handler.sendEmptyMessage(DISABLE_MENU);
							if(nearbyDownload) {
								handler.sendEmptyMessage(DISMISS_DIALOG);
								handler.sendEmptyMessage(START_DIALOG);
								nearbyDownload = false;
							}
							handler.sendEmptyMessage(LOAD_MARKERS);
							break;
						case MixState.DONE:
							handler.sendEmptyMessage(ENABLE_MENU);
							handler.sendEmptyMessage(LOAD_MARKERS);
							handler.sendEmptyMessage(DISMISS_DIALOG);
							break;
						default:
							break;
					}
					try {
						Thread.sleep(millisecond);	
					}catch (InterruptedException e) {}
				}
			}
		});    
    }
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case START_DIALOG:
					if(CATEGORY || tabValue.equals(InitActivity.TAB_NEARBY)) {
						nearbyMarkers.clear();
						categoryMarkers.clear();
						listView.invalidateViews();
						
						dialog = new ProgressDialog(SearchActivity.this);
						dialog.setTitle(getResources().getString(R.string.dialog_download_title));
						dialog.setMessage(getResources().getString(R.string.dialog_download_body));
						dialog.show();
					}
					break;
				case LOAD_MARKERS:
					loadNearbyMarkers();
					break;
				case INVALIDATE_LV:
					listView.invalidateViews();
					break;
				case DISMISS_DIALOG:
					try {
						dialog.dismiss();
						dialog = null;
					} catch (Exception e) {}
					break;
				case ENABLE_MENU:
						if(menuItemRefresh != null && (CATEGORY || tabValue.equals(InitActivity.TAB_NEARBY))) menuItemRefresh.setEnabled(true);			
					break;
				case DISABLE_MENU:
					try {
						if(menuItemRefresh != null && menuItemRefresh.isEnabled()) menuItemRefresh.setEnabled(false);	
					} catch (Exception e) {}
					break;
			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(edu.fsu.cs.argame.R.menu.search_menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case edu.fsu.cs.argame.R.id.search_menu_refresh:
				/** Case Refresh: toasts if there is no network connection. it forks another download thread if network is available */
				if(checkNetworkStatus())
					MixView.forkDownloadThread();
				else
					Toast.makeText(this, getResources().getString(R.string.search_no_network), Toast.LENGTH_SHORT).show();
				
				break;
			case edu.fsu.cs.argame.R.id.search_menu_category:
				/** Case Category: passes the array of categories selected */
				Intent intent = new Intent(this, SearchActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(NEARBY_CAT_SELECT, true);
				b.putBooleanArray(RADIO_SELECTED, radioSelected);
				intent.putExtras(b);
				startActivityForResult(intent, RESULT_FIRST_USER);
				break;
			default:
				break;
		}
		
		return true;
	}
	
	
	/**
	 * Sets the visibility of menu items depending on the Tab Mode.
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItemRefresh = menu.findItem(R.id.search_menu_refresh);
        MenuItem menuItemCategory= menu.findItem(R.id.search_menu_category);
        this.menuItemRefresh = menuItemRefresh;
        
        if(CATEGORY) {
        	/** Disables refresh option when download is not yet done */
        	if(MixState.nextLStatus == MixState.DONE || MixState.nextLStatus == MixState.NOT_STARTED) menuItemRefresh.setEnabled(true);
        	else menuItemRefresh.setEnabled(false);
        	menuItemRefresh.setVisible(true);
        	menuItemCategory.setVisible(false);
        	menuItemCategory.setEnabled(false);
        } else if(NEARBY_CATEGORY || tabValue.equals(InventoryTabActivity.TAB_CATEGORY)) {
        	menuItemRefresh.setVisible(false);
        	menuItemRefresh.setEnabled(false);
        	menuItemCategory.setVisible(false);
        	menuItemCategory.setEnabled(false);
        } else if(tabValue.equals(InventoryTabActivity.TAB_NEARBY)) {
        	/** Disables refresh option when download is not yet done */
        	if(MixState.nextLStatus == MixState.DONE) menuItemRefresh.setEnabled(true);
        	else menuItemRefresh.setEnabled(false);
        	menuItemRefresh.setVisible(true);
        	menuItemCategory.setVisible(true);
        	menuItemCategory.setEnabled(true);
        } else if(tabValue.equals(InventoryTabActivity.TAB_FAVORITE)) {
        	menuItemRefresh.setVisible(false);
        	menuItemRefresh.setEnabled(false);
        	menuItemCategory.setVisible(false);
        	menuItemCategory.setEnabled(false);
        }

        return true;
    }

	
	/**
	 * A class container of a favorite item. Equal to the Favorites database column with an additional distance attribute.
	 */
	public class Favorites {

		public int id;
		public String title;
		public String titleCount;
		public String phoneNum;
		public String category;
		public int categoryPos;
		public double longitude;
		public double latitude;
		public double distance;
		
		public Favorites(int id, String title, String titleCount, String phoneNum, String category, int categoryPos, double longitude, double latitude) {

			this.id = id;
			this.title = title;
			this.titleCount = titleCount;
			this.phoneNum = phoneNum;
			this.category = category;
			this.categoryPos = categoryPos;
			this.longitude= longitude;
			this.latitude = latitude;
			getDistance();
		}
		
		/** a method for computing the distance of favorite from the current location */
		public void getDistance() {
			try {
            	Location favLoc = new Location(MixView.ctx.getCurrentLocation());
            	favLoc.setLatitude(latitude);
            	favLoc.setLongitude(longitude);
            	
	            distance = MixView.ctx.getCurrentLocation().distanceTo(favLoc);
        	} catch (Exception e) {
        		distance = 0;
        	}
		}
	}
	
	/**
	 * Displays a dialog box when an item has been clicked in Favorite Search Tab
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.setup_search_tab_favorite);
		this.selectedFav = position;
		
		if(tabValue.equals(InventoryTabActivity.TAB_FAVORITE)) {
			alert.setItems(R.array.dialog_fav_items, dialogOnClickListener);
			alert.show();
		}
		return false;
	}
	
	private DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int item) {
	    	if(item == 0) deleteFavorite(favorites.get(selectedFav).id);
	    	else {
				Intent intent = new Intent(context, AddFavoriteActivity.class);
				Bundle b = new Bundle();
				b.putInt(FAV_EDIT, favorites.get(selectedFav).id);
				intent.putExtras(b);
				startActivity(intent);
	    	}
	        dialog.dismiss();
	    }
	};
	
	/**
	 * Deletes the row equal to the given id in the database.
	 * @param id the id of the favorite to be deleted.
	 */
	private void deleteFavorite(int id) {
		dbHelper.open();
		try {
			dbHelper.delete(dbHelper.getDb(), ARMapDB.COLUMN_ID + " = " + id);
			favorites.clear();
			loadFavorites();
			listView.invalidateViews();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbHelper.close();
	}
	
	/**
	 * Checks if a network provider(Wifi or 3g) is enabled
	 * @return true if either wifi or 3g is enabled, false if neither of the two is available
	 */
	private boolean checkNetworkStatus() {
    	final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    	final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    	 if(!wifi.isAvailable() && !mobile.isAvailable())
    		return false;
    	 
    	 return true;
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if(NEARBY_CATEGORY) {
					Intent intent = new Intent();
					Bundle b = new Bundle(); 
					b.putBooleanArray(RADIO_SELECTED, radioSelected);
					intent.putExtras(b); 
					setResult(RESULT_FIRST_USER, intent); 
					finish(); 
					return true;
				}
				
		}
		return super.onKeyDown(keyCode, event);
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopThread = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/** refreshes favorites list */
		if(tabValue.equals(InitActivity.TAB_FAVORITE)) {
			loadFavorites();
			listView.invalidateViews();
		} else if(tabValue.equals(InitActivity.TAB_NEARBY)) {
			stopThread = false;
			downloadCheckerThread().start();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULT_FIRST_USER) {
			Bundle b = data.getExtras(); 
			radioSelected = b.getBooleanArray(RADIO_SELECTED);
			NEARBY_CATEGORY = false;
			loadNearbyMarkers();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/** A comparator which sorts the distance values in ascending order. **/
	class FavoritesOrder implements java.util.Comparator<Object> {
		public int compare(Object left, Object right) {
			Favorites leftFav = (Favorites) left;
			Favorites rightFav = (Favorites) right;
			return Double.compare(leftFav.distance, rightFav.distance);
		}
	}
}