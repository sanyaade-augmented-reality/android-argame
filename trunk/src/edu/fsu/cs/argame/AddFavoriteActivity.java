package edu.fsu.cs.argame;

import org.mixare.MixView;
import org.mixare.data.ARMapDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * The activity responsible for adding a favorite in the database
 */
public class AddFavoriteActivity extends Activity implements OnClickListener, OnKeyListener, OnFocusChangeListener, TextWatcher {

	/** ListView that contains favorite's icon and favorite's phone number**/
	private ListView listView;
	
	/** ListView that contains the list of recommended names **/
	private ListView listViewRec;
	
	/** GridView that contains icons of all categories **/
	private GridView gridView;
	
	private LinearLayout linearLayoutLV;
	private LinearLayout linearLayoutGV;
	private LinearLayout linearLayoutLvRec;
	private LinearLayout linearLayoutPhone;
	private LinearLayout linearLayoutPhoneDivider;
	
	/** Edittext for favorite's title **/
	private EditText titleEditText;
	
	/** Edittext for favorite's phone number **/
	private EditText phoneEditText;
	
	private TextView header;
	
	/** Button that saves the input to the favorites database when clicked **/
	private Button saveButton;
	
	/** Button that finishes the activity when clicked **/
	private Button cancelButton;
	
	/** Button that displays the recommended list when clicked **/
	private Button recommendedButton;
	
	/** The flag for each category, false when the category in a certain index is selected **/
	private boolean[] selectedRadio = {false, false, false, false, false, false, false, false };

	private TypedArray favArray;
	
	/** Contains the recommended names list **/
	private TypedArray recArray;
	
	/** Contains the id of all category icons **/
	private TypedArray catIconArray;
	
	/** Contains the category list **/
	private TypedArray categoryArray;
	
	private ARMapDB dbHelper;
	private Context context;
	private WakeLock mWakeLock;
	private AlertDialog.Builder builder;
	private AlertDialog alert;
	
	/** String for the default category **/
	public static String DEFAULT = "DEFAULT";
	
	/** Initial category or when no category is selected before saving the favorite **/
	private String category = DEFAULT;
	
	private String TAG = "AddFavorite";
	
	/** Corresponds to the phoneNumber input **/
	private String phoneNumber = "";
	
	/** Contains the id of the selected favorite **/
	private int selectFavId = 0;
	
	/** The flag to determine which listview or gridview is visible. 0 if listView is visible 1 otherwise **/
	private int stack = 0;
	
	/** Contains the selected category position in category array **/
	private int categoryPos;
	
	/** Image id of the category selected **/
	private int image;
	
	/** Determines the number of instance of favorites in the database with the same name as the input favorite name**/
	private int count = 0;
	
	/** Id of the favorite with the same name with the inputed name **/
	private int id = -1;
	
	/** Flag for the number of items to be displayed in the listview **/
	private int listCount = 2;
	
	private final int START_DIALOG = 0;
	private final int DISMISS_DIALOG = 1;
	
	/** Contains the value of the current phone orientation **/
	private int windowOrientation = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_favorite);

		context = this;
        dbHelper = new ARMapDB(this);
		initViews();
		getBundles();

	}
	
	/**
	 * Initializes UI components
	 */
	private void initViews() {
		favArray = this.getResources().obtainTypedArray(R.array.add_fav_list);
		recArray = this.getResources().obtainTypedArray(R.array.add_fav_rec_name);
		catIconArray = this.getResources().obtainTypedArray(R.array.category_drawable_big);
		categoryArray = this.getResources().obtainTypedArray(R.array.category);
		categoryPos = categoryArray.length()-1;
				
		header = (TextView) findViewById(R.id.fav_header);
		titleEditText = (EditText) findViewById(R.id.title_edittext);
		phoneEditText = (EditText) findViewById(R.id.phone_edittext);
		phoneEditText.setOnKeyListener(this);
		phoneEditText.addTextChangedListener(this);
		phoneEditText.setOnFocusChangeListener(this);
        phoneEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				showPhoneNumberLV();
				return false;
			}
		});
				
		linearLayoutLV = (LinearLayout) findViewById(R.id.frame_listview);
		linearLayoutPhone = (LinearLayout) findViewById(R.id.phone_layout);
		linearLayoutGV = (LinearLayout) findViewById(R.id.frame_gridview);
		linearLayoutLvRec = (LinearLayout) findViewById(R.id.frame_listview_rec);
		linearLayoutPhoneDivider = (LinearLayout) findViewById(R.id.phone_divider);

		listView = (ListView) findViewById(R.id.add_fav_lv);
		listViewRec = (ListView) findViewById(R.id.listview_rec);
		gridView = (GridView) findViewById(R.id.gridview);
		
		listView.setAdapter(new ListViewAdapter(this, 0));
		listViewRec.setAdapter(new ListViewAdapter(this, 1));
	    gridView.setAdapter(new ImageAdapter(this));
	    
		listView.setOnItemClickListener(itemClickListenereLV);
		listViewRec.setOnItemClickListener(itemClickListenereLvRec);
	    gridView.setOnItemClickListener(itemClickListenereGV);
	    
		saveButton = (Button) findViewById(R.id.save);
		cancelButton = (Button) findViewById(R.id.cancel);
		recommendedButton = (Button) findViewById(R.id.rec_text);
		
		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		recommendedButton.setOnClickListener(this);
		
		windowOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
		if(windowOrientation == MixView.PORTRAIT) gridView.setNumColumns(4);
		else gridView.setNumColumns(7);
		
		PowerManager lPwrMgr = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = lPwrMgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
	}
	
	/**
	 * Determines the source activity. When bundle is not null, it initializes the values based on the values passed by Search.class
	 */
	private void getBundles() {
		Bundle b = getIntent().getExtras();
    	dbHelper.open();
		try {
	        if(b.getInt(SearchActivity.FAV_EDIT) != 0) {
	    		Cursor cursor;
	    		try {
	    			cursor = dbHelper.fetch(dbHelper.getDb(), ARMapDB.COLUMN_ID + " = " + b.getInt(SearchActivity.FAV_EDIT), null);
	    			if(cursor.moveToFirst()) {
						selectFavId = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_ID));
						titleEditText.setText(cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_TITLE)));
						phoneNumber = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_PHONE_NUM));
						phoneEditText.setText(cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_PHONE_NUM)));
						
						category = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_CATEGORY));
						categoryPos = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_CATEGORY_POS));
						image = catIconArray.getResourceId(categoryPos, 0);
						header.setText(getResources().getString(R.string.addfav_edit_mode));
					}
	    			cursor.close();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	        }
		} catch (Exception e) {}
		dbHelper.close();
	}
	
	public class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
	    private int mode = 0;
	    public ListViewAdapter(Context c, int mode) {
	        mInflater = LayoutInflater.from(c);
	        this.mode = mode;
	    }
	    
	    public int getCount() {
	    	if(mode == 0) return listCount;
	    	else return recArray.length();
	        
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
                holder.poiName = (TextView) convertView.findViewById(R.id.setup_title);
                holder.poiValue = (TextView) convertView.findViewById(R.id.setup_value);
                holder.arrow = (ImageView) convertView.findViewById(R.id.arrow_img);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.radio = (RadioButton) convertView.findViewById(R.id.radio);
                holder.poiValue.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            /**
             * mode 0 is for displaying the initial listview (inputing name and phone number)
             * mode not equal to 0 is for displaying recommended names
             */
            if(mode == 0) {
            	if(position == 0) {
	            	holder.image.setVisibility(View.VISIBLE);
	            	if(image == 0) holder.image.setImageResource(R.drawable.default_icon);
	            	else holder.image.setImageResource(image);
	            	holder.poiName.setText(favArray.getString(position));
	            	holder.poiName.setTextColor(Color.WHITE);
            	} else {
	            	holder.image.setVisibility(View.VISIBLE);
	            	holder.image.setImageResource(R.drawable.btn_call);
	            	if(phoneNumber != null && phoneNumber.equals(""))
	            		holder.poiName.setText(favArray.getString(position));
	            	else holder.poiName.setText(phoneNumber);
	            	holder.poiName.setTextColor(Color.parseColor("#00b0f0"));
            	}
            }
            else {
            	holder.poiName.setText(recArray.getString(position));
            	holder.radio.setVisibility(View.VISIBLE);
            	if(selectedRadio[position]) holder.radio.setChecked(true);
            	else holder.radio.setChecked(false);
            }
        	holder.arrow.setVisibility(View.GONE);
            
            return convertView;
	    }

        class ViewHolder {
            TextView poiName;
            TextView poiValue;
            ImageView arrow;
            ImageView image;
            RadioButton radio;
        }
        
	}

	/**
	 * Adapter for displaying category images
	 */
	public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

	    public ImageAdapter(Context c) {
	    	mInflater = LayoutInflater.from(c);
	    }

	    public int getCount() {
	        return catIconArray.length()-1;
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
	        	convertView = mInflater.inflate(R.layout.add_favorite_gv, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
	        } else {
                holder = (ViewHolder) convertView.getTag();
	        }
	        holder.image.setImageResource(catIconArray.getResourceId(position, 0));
	        return convertView;
	    }
	    
        class ViewHolder {
            ImageView image;
        }

	}
	
	OnItemClickListener itemClickListenereLV = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) { 
			switch(position) {
				case 0: 
					/** Displays the image gridview **/
					if(windowOrientation == MixView.PORTRAIT) gridView.setNumColumns(4);
					else gridView.setNumColumns(7);
					
					linearLayoutLV.setVisibility(View.GONE);
					linearLayoutGV.setVisibility(View.VISIBLE);
					linearLayoutLvRec.setVisibility(View.GONE);
					stack = 1;

					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
					break;
				case 1:
					/** Displays the phone edittext **/
					listCount = 1;
					listView.invalidateViews();
					linearLayoutPhone.setVisibility(View.VISIBLE);
					linearLayoutPhoneDivider.setVisibility(View.VISIBLE);
					phoneEditText.requestFocus();
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(phoneEditText, 0);
					break;
			}
		}
	};
	
	OnItemClickListener itemClickListenereLvRec = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) { 
			RadioButton radio = (RadioButton) ((LinearLayout) arg1).getChildAt(3);
			
			for(int i = 0; i < selectedRadio.length; i++) selectedRadio[i] = false;
			selectedRadio[position] = true;
			radio.setChecked(true);
			listViewRec.invalidateViews();
			linearLayoutLV.setVisibility(View.VISIBLE);
			linearLayoutGV.setVisibility(View.GONE);
			linearLayoutLvRec.setVisibility(View.GONE);
			titleEditText.setText(recArray.getString(position));
			stack = 0;
		
		}
	};

	OnItemClickListener itemClickListenereGV = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			/** Hides the image gridview and displays the image clicked on the listview **/
			linearLayoutLV.setVisibility(View.VISIBLE);
			linearLayoutGV.setVisibility(View.GONE);
			linearLayoutLvRec.setVisibility(View.GONE);
			
			image = catIconArray.getResourceId(position, 0);
			category = categoryArray.getString(position);
			categoryPos = position;
			stack = 0;
			showPhoneNumberLV();

		}
	};
	
	/**
	 * Displays the phone number item in the listview and hides the edittext for the phone number
	 */
	private void showPhoneNumberLV() {
        listCount = 2;
        linearLayoutPhone.setVisibility(View.GONE);
        linearLayoutPhoneDivider.setVisibility(View.GONE);
        phoneNumber = parseNumber(phoneEditText.getEditableText().toString());
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
        
        listView.invalidateViews();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if(stack == 1) {
					/** Sets the screen to the initial listview if the view is currently on gridview or listview with recommended names**/
					linearLayoutLV.setVisibility(View.VISIBLE);
					linearLayoutGV.setVisibility(View.GONE);
					linearLayoutLvRec.setVisibility(View.GONE);
					
					stack = 0;
					return true;
				} else return super.onKeyDown(keyCode, event);
			default:
				return super.onKeyDown(keyCode, event);
				
		}
	}

	@Override
	public void onClick(View v) {
		if(v == saveButton) {
			Cursor cursor;
			dbHelper.open();
			
			/** checks if there is an existing item in the favorites with the same name as the inputed title **/
			cursor = dbHelper.fetch(dbHelper.getDb(), ARMapDB.COLUMN_TITLE + " = \"" + titleEditText.getText().toString() + "\"", null);
			phoneNumber = parseNumber(phoneEditText.getEditableText().toString());
			builder = new AlertDialog.Builder(this);
			Builder duplicateBuilder = new AlertDialog.Builder(this);
			
			if(cursor.moveToFirst()) {
				id = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_ID));
				count = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_TITLE_COUNT));
			} else {
				id = -1;
				count = 0;
			}
			
			dbHelper.close();
			cursor.close();
			
			/** Alerts the user that there is no input favorite title **/
			if(titleEditText.getText().toString().equals("")) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
				builder.setMessage(getResources().getString(R.string.addfav_dialog_fillinmissing));
				alert = builder.create();
				dialogThread().start();
			} else if(selectFavId == 0 && count > 0) {
				/** Alerts the user that there is an existing favorite with same title **/
				duplicateBuilder.setTitle(getResources().getString(R.string.addfav_dialog_existing_entry));
				duplicateBuilder.setPositiveButton(getResources().getString(R.string.addfav_dialog_update), new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	updatePoi(id);
						Intent intent = new Intent(context, InventoryTabActivity.class);
						Bundle b = new Bundle();
						b.putBoolean(InventoryTabActivity.FAVORITE, true);
						intent.putExtras(b);
						startActivity(intent);
						finish();
				    }
				});
				duplicateBuilder.setNegativeButton(getResources().getString(R.string.addfav_dialog_create_new), new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	dbHelper.open();
						dbHelper.insert(dbHelper.getDb(), titleEditText.getText().toString() + "(" + count + ")", phoneNumber,
										category, categoryPos, MixView.ctx.getCurrentLocation().getLongitude(), MixView.ctx.getCurrentLocation().getLatitude());
						ContentValues values = new ContentValues();
						values.put(ARMapDB.COLUMN_TITLE_COUNT, count+1);
						dbHelper.update(dbHelper.getDb(), values, ARMapDB.COLUMN_ID + " = " + id);
						dbHelper.close();
						Intent intent = new Intent(context, InventoryTabActivity.class);
						Bundle b = new Bundle();
						b.putBoolean(InventoryTabActivity.FAVORITE, true);
						intent.putExtras(b);
						startActivity(intent);
						finish();
				    }
				});
				duplicateBuilder.show();
			} else {
				/** Saves the favorite to the database and directs the user to the favorites list **/
				if(selectFavId != 0) updatePoi(selectFavId);
				else {
					dbHelper.open();
					dbHelper.insert(dbHelper.getDb(), titleEditText.getText().toString(), phoneNumber,
									category, categoryPos, MixView.ctx.getCurrentLocation().getLongitude(), MixView.ctx.getCurrentLocation().getLatitude());
					dbHelper.close();
				}
				
				Intent intent = new Intent(this, InventoryTabActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(InventoryTabActivity.FAVORITE, true);
				intent.putExtras(b);
				startActivity(intent);
				finish();
			}
		}
		else if(v ==cancelButton) finish();
		else if(v == recommendedButton) {
			/** Displays the listview with the recommended names **/
			linearLayoutLV.setVisibility(View.GONE);
			linearLayoutGV.setVisibility(View.GONE);
			linearLayoutLvRec.setVisibility(View.VISIBLE);
			stack = 1;
			
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);

		}
	}
	
	/**
	 * Updates the favorite item with the given id.
	 * @param id the id of the item to be updated
	 */
	private void updatePoi(int id) {
		dbHelper.open();
		try {
			ContentValues values = new ContentValues();
			values.put(ARMapDB.COLUMN_TITLE, titleEditText.getText().toString());
			values.put(ARMapDB.COLUMN_PHONE_NUM, phoneNumber);
			values.put(ARMapDB.COLUMN_CATEGORY, category);
			values.put(ARMapDB.COLUMN_CATEGORY_POS, categoryPos);
			dbHelper.update(dbHelper.getDb(), values, ARMapDB.COLUMN_ID + " = " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbHelper.close();
	}
		
	/**
	 * Parses the string based on the given cases:
	 * <ul>
	 * <li>02-XXX-XXXX</li>
	 * <li>02-XXXX-XXXX</li>
	 * <li>XXX-XXX-XXXX</li>
	 * <li>XXX-XXXX-XXXX</li>
	 * </ul>
	 * @param string the string to be parsed as phone number
	 * @return the parsed string
	 */
	private String parseNumber(String string) {
        String temp = "", temp2="";
        string = string.replaceAll("-", "");
        int parserState = 0;
        int finalState = 100;
        
        for (int i = 0; (parserState != finalState) && (i < string.length()); i++) {
                char c = string.charAt(i);
                switch (parserState) {
                    case 0: temp +=c;
                            parserState = 1;
                            break;
                    case 1: temp +=c;
                            if (c != '2') parserState++;
                            else parserState = 3;
                            break;
                    case 2: temp +=c;
                            parserState++;
                            break;
                    case 3: temp +='-';
                            temp +=c;
                            parserState++;
                            break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:temp +=c;
			           		parserState++;
			        		break;
                    case 9:temp += c;
                    		temp2 = temp.substring(0, temp.length()-4);
                    		temp2 += '-';
                    		for(int j = temp.length()-4; j < temp.length(); j++) {
                    			temp2 += temp.charAt(j);
                    		}
                    		temp = "";
                    		parserState++;
                    		break;
                    case 10:temp2 += c;
                    		temp = temp2.substring(0, temp2.length()-6);
		            		for(int j = temp2.length()-5, k = 0; j < temp2.length(); j++, k++) {
		            			if(k == 1) temp+= '-';
		            			temp += temp2.charAt(j);
		            		}
		            		temp2 = "";
		            		parserState++;
		            		break;
                }     
        	}
        return (temp2.equals("")?temp:temp2);
	}
	
	/** The thread that starts and dismisses a dialog box after 2 seconds */
	private Thread dialogThread() {
		return new Thread(new Runnable(){   	
			
			public void run() {    			 	
				int millisecond = 1000;
				handler.sendEmptyMessage(START_DIALOG);
				for(int i = 0; 1 < 2; i++) {
					try {
						Thread.sleep(millisecond);	
					}catch (InterruptedException e) {}
					if(i == 1) handler.sendEmptyMessage(DISMISS_DIALOG);
				}
			}
		});    
    }
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case START_DIALOG:
				alert.show();
				break;
			case DISMISS_DIALOG:
				try {
					alert.dismiss();
				} catch (Exception e) {}
				break;
			}
		}
	};
	
	/**
	 * Changes the number of gridView colums depending on the phone's orientation.<br>
	 * (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 **/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		windowOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
		if(windowOrientation == MixView.PORTRAIT) gridView.setNumColumns(4);
		else gridView.setNumColumns(7);
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
	    if ((event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)) {
	        showPhoneNumberLV();
	    }
	    return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) {
			showPhoneNumberLV();
		}
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

	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	/**
	 * Parses the text on the edittext every onTextChange<br>
	 * (non-Javadoc)
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		phoneEditText.removeTextChangedListener(this);
		phoneEditText.setText(parseNumber(phoneEditText.getEditableText().toString()));
		phoneEditText.setSelection(phoneEditText.getEditableText().toString().length());
		phoneEditText.addTextChangedListener(this);
	}
}