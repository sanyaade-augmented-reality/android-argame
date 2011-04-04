/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.peterd.zombierun.constants.ImageBundle;
import net.peterd.zombierun.util.GRSConverter;

import org.mixare.data.ARMapDB;
import org.mixare.gui.PaintScreen;
import org.mixare.reality.PhysicalPlace;
import org.mixare.render.Matrix;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import edu.fsu.cs.argame.InitActivity;
import edu.fsu.cs.argame.CameraView;
import edu.fsu.cs.argame.CompassView;
import edu.fsu.cs.argame.SearchActivity;
import edu.fsu.cs.argame.marker.MapOverlay;
import edu.fsu.cs.argame.marker.Marker;
import edu.fsu.cs.argame.marker.MarkersOrder;

public class MixView extends MapActivity implements SensorEventListener, OnClickListener {

    // TAG for logging
    public static final String TAG = "Mixare";

    protected FrameLayout frameLayout;
    protected View mapFrame;
    private CameraView cameraView;
    private static AugmentedView augScreen;
    private CompassView compassView;
    public static MixContext ctx;
//    MenuItem menuItemFav;
//    MenuItem menuItemSearch;
    MenuItem menuItemMyLocation;
    MenuItem menuItemMapMode;
//    MenuItem menuItemMapModeMap;
//    MenuItem menuItemMapModeSat;
    MenuItem menuItemNewGame;
    MenuItem menuItemAbout;
    
    private ARMapDB dbHelper;

    private LayoutInflater layoutInflater;
    private View poiDetails;
    private View moreInformation;
    private ImageView poiIcon;
    private TextView poiTitle;
    private TextView poiDistance;
    private TextView poiAddress;
    private TextView poiTelephone;
    private Button callButton;
    private Button showInfoButton;
    private Button hideInfoButton;
    private Button rightArrow;
    private Button leftArrow;

    private SharedPreferences arSettings;
    private static Thread downloadThread;
    protected boolean isRunning = true;
    private static boolean firstRun = true;
    public static boolean isInited = false;
    protected boolean flag = true;

    // public static final String AR_WEBSERVER =
    // "http://map.gg.go.kr:8099/gg_pb/content/was/ContentArroundSearch.do?";
    // public static String AR_WEBSERVER_URL;

    float RTmp[] = new float[9];
    float R[] = new float[9];
    float I[] = new float[9];
    float grav[] = new float[3];
    float mag[] = new float[3];
    int rHistIdx = 0;
    Matrix tempR = new Matrix();
    Matrix finalR = new Matrix();
    Matrix smoothR = new Matrix();
    Matrix histR[] = new Matrix[60];
    Matrix m1 = new Matrix();
    Matrix m2 = new Matrix();
    Matrix m3 = new Matrix();
    Matrix m4 = new Matrix();
    double angleX;
    double angleY;

    public static float orientation[] = new float[3];
    public static int windowOrientation = 0;
    public final static int PORTRAIT = 0;
    public final static int LANDSCAPE = 1;

    private WakeLock mWakeLock;
    private SensorManager sensorMgr;
    private Sensor sensorGrav;
    private Sensor sensorMag;
    public static LocationManager locationMgr;
    private LocationListener locationListenerGps;
    private LocationListener locationListenerNetwork;
    protected long currentTime = -1;
    protected final static long requiredTime = 60000;

    private float time = 0;
    private float speed = 0;
    private boolean speedFlag = false;
    public static boolean downloadFailed = false;
    private Location prevLocation = new Location("");
    private boolean startThread = false;

    private MapView mapView;
    private MapController mc;
    public static MapOverlay mapOverlay;
    GeoPoint currentLocation;
    protected static Location startingGpsLocation = null;
    public static int distanceDownload = 0;

    public static Bitmap poiBg[];
    public static int radarIconArray[] = new int[] { edu.fsu.cs.argame.R.drawable.compass_inactive, edu.fsu.cs.argame.R.drawable.compass_active, edu.fsu.cs.argame.R.drawable.compass_radar,
	    edu.fsu.cs.argame.R.drawable.compass_arrow };
    public static Bitmap radarBitmap[];
    public static Bitmap angle;

    public final static int DEFAULT = 0;
    public final static int POI_LOADED = 1;
    public final static int COMPASS_RADAR = 2;
    public final static int COMPASS_ARROW = 3;

    private int pitchAngle;
    private int touch = 1;

    public static String[] categoryArray;
    public final static String SHOW_ALL = "show_all";
    public static String category = SHOW_ALL;

    public final static String CATEGORY = "category";
    public final static String FAVORITES = "favorites";
    public final static String FAVORITES_ID = "favorites_id";

    protected static final int SHOW_POI_DETAILS = 0;
    public static ArrayList<Marker> arMarkers = new ArrayList<Marker>();
    public static Marker pressedMarker = null;
    public final static String STACK_SIZE = "STACK_SIZE";

    protected CountDownTimer countDownTimer;
    protected ProgressDialog progressDialog;
    private AlertDialog.Builder speedDialogBuilder;
    private AlertDialog speedDialogAlert;

    protected final static int NOT_YET_DISPLAYED = 0;
    protected final static int DISPLAYED = 1;
    protected static final int WIFI_3G_TOAST = 0;
    protected static final int CANCEL_WIFI_3G_DIALOG = 1;
    protected static final long MINTIME = 1000;
    protected final int SECONDS = 1000;
    protected final int SECONDS_IN_MINUTES = 60;
    protected final int MINUTES = 1;
    protected final int TOTAL_TIME = SECONDS * SECONDS_IN_MINUTES * MINUTES;

    public static Handler handler;
    public static boolean isOnScreen = false;

    protected static String currentLocationString;
    protected static String kilometerString;
    protected static String meterString;
    protected final static float MAX_SPEED = 5.56f;

    private final int DISMISS_DIALOG = 1;
    private ArrayList<Float> distanceArray;
    private ArrayList<Float> timeArray;
    private int dataCount = 0;
    private boolean mapModeSat = false;

    /**
     * called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// checking if Splash screen was already shown
	if (InitActivity.AR_MAP_STATE == InitActivity.NOT_STARTED) {
	    Intent intent = new Intent(this, InitActivity.class);
	    startActivity(intent);
	    finish();
	    System.exit(1);
	}

	initWindowParameters();
	initComponents();
	initMapComponents();
	initLayers();
	initSensors();
	startLocationListeners();
	getRequestLocationThread().start();

    }

    /**
     * initialize parameters for the current window
     */
    private void initWindowParameters() {
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(edu.fsu.cs.argame.R.layout.main);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	PowerManager lPwrMgr = (PowerManager) getSystemService(POWER_SERVICE);
	mWakeLock = lPwrMgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

    }

    /**
     * initialize the major components the activity will use (i.e. views,
     * location listeners, etc)
     */
    private void initComponents() {

	speedDialogBuilder = new AlertDialog.Builder(this);
	currentLocationString = getText(edu.fsu.cs.argame.R.string.search_current_loc).toString();
	kilometerString = getText(edu.fsu.cs.argame.R.string.kilometer_abbreviation).toString();
	meterString = getText(edu.fsu.cs.argame.R.string.meter_abbreviation).toString();
	distanceArray = new ArrayList<Float>();
	timeArray = new ArrayList<Float>();

	dbHelper = new ARMapDB(this);

	arSettings = this.getSharedPreferences(InitActivity.AR_SETTINGS_FILENAME, MODE_WORLD_WRITEABLE);

	windowOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

	layoutInflater = getLayoutInflater();

	frameLayout = (FrameLayout) findViewById(edu.fsu.cs.argame.R.id.frame);

	compassView = new CompassView(this);
	cameraView = new CameraView(this);

	poiDetails = findViewById(edu.fsu.cs.argame.R.id.poi_details);
	poiIcon = (ImageView) findViewById(edu.fsu.cs.argame.R.id.poi_icon);
	poiTitle = (TextView) findViewById(edu.fsu.cs.argame.R.id.poi_title);
	poiDistance = (TextView) findViewById(edu.fsu.cs.argame.R.id.poi_distance);
	poiAddress = (TextView) findViewById(edu.fsu.cs.argame.R.id.poi_address);
	poiTelephone = (TextView) findViewById(edu.fsu.cs.argame.R.id.poi_telephone);
	callButton = (Button) findViewById(edu.fsu.cs.argame.R.id.call_btn);
	callButton.setOnClickListener(this);
	moreInformation = findViewById(edu.fsu.cs.argame.R.id.more_information);

	initInfoButtons();

	rightArrow = (Button) findViewById(edu.fsu.cs.argame.R.id.right_arrow);
	leftArrow = (Button) findViewById(edu.fsu.cs.argame.R.id.left_arrow);

	// listeners for location from GPS
	locationListenerGps = new LocationListener() {

	    @Override
	    public void onLocationChanged(Location location) {
		location.setAltitude(0);

		currentTime = location.getTime();

		/* used in speed computations */
		if (dataCount == 0)
		    prevLocation = location;
		distanceArray.add(prevLocation.distanceTo(location));
		timeArray.add(time);
		time = 0;
		prevLocation = location;
		dataCount++;

		/* compute speed if number of data is already equal to 5 */
		if (dataCount == 5) {
		    float totalDistance = 0, totalTime = 0;
		    for (int l = 0; l < distanceArray.size(); l++) {
			totalDistance += distanceArray.get(l);
			totalTime += timeArray.get(l);
		    }
		    speed = totalDistance / totalTime;
		    distanceArray.remove(0);
		    timeArray.remove(0);
		    dataCount--;
		}

		if (!startThread) {
		    startThread = true;
		    try {
			speedThread().start();
		    } catch (Exception e) {
		    }
		}
//		if (menuItemFav != null)
//		    menuItemFav.setEnabled(true);
//		if (menuItemSearch != null)
//		    menuItemSearch.setEnabled(true);
		if (menuItemMyLocation != null)
		    menuItemMyLocation.setEnabled(true);
		if (menuItemMapMode != null)
		    menuItemMapMode.setEnabled(true);
		if (menuItemNewGame != null)
		    menuItemNewGame.setEnabled(true);
		if (menuItemAbout != null)
		    menuItemAbout.setEnabled(true);

		if (progressDialog != null) {
		    progressDialog.dismiss();
		    countDownTimer.cancel();
		}

		// case for first location
		if (ctx.curLoc == null) {
		    startingGpsLocation = location;
		    ctx.curLoc = location;

		    mc.setZoom(20);
		    mc.animateTo(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));
		    mc.setCenter(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));
		}

		synchronized (ctx.curLoc) {
		    ctx.curLoc = location;
		    computeGeomagneticField();

		    // updateUrlRequest();

		    mapOverlay.currentlocation = new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6));

		    mapView.invalidate();

		    float currentDistance = ctx.curLoc.distanceTo(startingGpsLocation);

		    if ((MixState.nextLStatus == MixState.NOT_STARTED) || currentDistance >= distanceDownload) {
			if (currentDistance >= distanceDownload) {
			    startingGpsLocation = location;

			    mc.animateTo(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));
			    mc.setCenter(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));

			    String downloadType = arSettings.getString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DEFAULT_DOWNLOAD_TYPE);

			    if (downloadType.equals(InitActivity.DOWNLOAD_AUTO) && speed < MAX_SPEED) {
				speedFlag = false;
				MixState.nextLStatus = MixState.NOT_STARTED;
			    }

			    if (downloadType.equals(InitActivity.DOWNLOAD_AUTO) && speed > MAX_SPEED && (speedDialogAlert == null || !speedDialogAlert.isShowing()) && !speedFlag) {
				speedDialogBuilder.setMessage("In excess of 20km the data can be downloaded automatically???");
				speedDialogAlert = speedDialogBuilder.create();
				speedDialogAlert.show();
				dialogThread().start();
				speedFlag = true;
			    }
			}

			if (augScreen != null && compassView != null) {
			    augScreen.postInvalidate();
			    compassView.postInvalidate();
			}

			repaint();
		    }
		}
	    }

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	};

	// listeners for location from network
	locationListenerNetwork = new LocationListener() {
	    @Override
	    public void onLocationChanged(Location location) {

		location.setAltitude(0);

		if ((currentTime < 0) || (MixView.ctx.curLoc == null) || (location.getTime() - currentTime) > requiredTime) {
		    currentTime = location.getTime();

//		    if (menuItemFav != null)
//			menuItemFav.setEnabled(true);
//		    if (menuItemSearch != null)
//			menuItemSearch.setEnabled(true);		    
			if (menuItemMyLocation != null)
			    menuItemMyLocation.setEnabled(true);
			if (menuItemMapMode != null)
			    menuItemMapMode.setEnabled(true);
			if (menuItemNewGame != null)
			    menuItemNewGame.setEnabled(true);
			if (menuItemAbout != null)
			    menuItemAbout.setEnabled(true);
		    
		    

		    if (progressDialog != null) {
			progressDialog.dismiss();
			countDownTimer.cancel();
		    }

		    if (MixView.ctx.curLoc == null) {
			startingGpsLocation = location;
			MixView.ctx.curLoc = location;

			mc.setZoom(20);
			mc.animateTo(new GeoPoint((int) (MixView.ctx.curLoc.getLatitude() * 1E6), (int) (MixView.ctx.curLoc.getLongitude() * 1E6)));
			mc.setCenter(new GeoPoint((int) (MixView.ctx.curLoc.getLatitude() * 1E6), (int) (MixView.ctx.curLoc.getLongitude() * 1E6)));

		    }

		    synchronized (MixView.ctx.curLoc) {
			ctx.curLoc = location;
			computeGeomagneticField();
			// updateUrlRequest();
			mapOverlay.currentlocation = new GeoPoint((int) (MixView.ctx.curLoc.getLatitude() * 1E6), (int) (MixView.ctx.curLoc.getLongitude() * 1E6));

			mapView.invalidate();

			float currentDistance = ctx.curLoc.distanceTo(startingGpsLocation);

			if ((MixState.nextLStatus == MixState.NOT_STARTED) || currentDistance >= distanceDownload) {

			    if (currentDistance >= distanceDownload) {
				startingGpsLocation = location;

				mc.animateTo(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));
				mc.setCenter(new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6)));

				String downloadType = arSettings.getString(InitActivity.SETUP_DOWNLOAD_TYPE, InitActivity.DEFAULT_DOWNLOAD_TYPE);
				if (downloadType.equals(InitActivity.DOWNLOAD_AUTO)) {
				    MixState.nextLStatus = MixState.NOT_STARTED;
				}
			    }

			    repaint();
			}
		    }
		}
	    }

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	};

	handler = initMixViewHandler();
	isInited = true;
	augScreen = new AugmentedView(this);

	new Thread(new Runnable() {
	    @Override
	    public void run() {
		initBitmaps();
	    }
	}).start();

	ctx = new MixContext(this);

	ctx.downloadManager = new DownloadManager(ctx);
	ctx.setDataView(new DataView());

	setZoomLevel();

    }

    /**
     * Returns a new thread for constantly requesting location updates from
     * network and GPS
     * 
     * @return an instance of the thread
     */
    private Thread getRequestLocationThread() {
	return new Thread(new Runnable() {

	    @Override
	    public void run() {
		while (isRunning) {
		    try {
			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINTIME, distanceDownload, locationListenerGps);
			locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINTIME, distanceDownload, locationListenerNetwork);
		    } catch (Exception e) {
		    }
		    try {
			Thread.sleep(MINTIME * 10);
		    } catch (InterruptedException e) {
		    }
		}
	    }
	});
    }

    /**
     * Returns a new thread for the computation of speed
     * 
     * @return an instance of the thread
     */
    private Thread speedThread() {
	return new Thread(new Runnable() {
	    public void run() {
		while (startThread) {
		    time++;
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
		    }
		}
	    }
	});
    }

    /**
     * initialize the buttons for showing POI details based on the current
     * device orientation
     */
    private void initInfoButtons() {
	switch (windowOrientation) {
	case PORTRAIT:
	    if (showInfoButton == null) {
		showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_portrait);
		showInfoButton.setOnClickListener(this);

		hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_portrait);
		hideInfoButton.setOnClickListener(this);
	    } else {
		switch (showInfoButton.getVisibility()) {
		case View.VISIBLE:
		    showInfoButton.setVisibility(View.INVISIBLE);
		    showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_portrait);
		    showInfoButton.setOnClickListener(this);
		    showInfoButton.setVisibility(View.VISIBLE);
		    break;

		default:
		    showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_portrait);
		    showInfoButton.setOnClickListener(this);
		    break;
		}

		switch (hideInfoButton.getVisibility()) {
		case View.VISIBLE:
		    hideInfoButton.setVisibility(View.INVISIBLE);
		    hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_portrait);
		    hideInfoButton.setOnClickListener(this);
		    hideInfoButton.setVisibility(View.VISIBLE);
		    break;

		default:
		    hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_portrait);
		    hideInfoButton.setOnClickListener(this);
		    break;
		}
	    }
	    break;

	default:
	    if (showInfoButton == null) {
		showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_landscape);
		showInfoButton.setOnClickListener(this);

		hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_landscape);
		hideInfoButton.setOnClickListener(this);
	    } else {
		switch (showInfoButton.getVisibility()) {
		case View.VISIBLE:
		    showInfoButton.setVisibility(View.INVISIBLE);
		    showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_landscape);
		    showInfoButton.setOnClickListener(this);
		    showInfoButton.setVisibility(View.VISIBLE);
		    break;

		default:
		    showInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_show_landscape);
		    showInfoButton.setOnClickListener(this);
		    break;
		}

		switch (hideInfoButton.getVisibility()) {
		case View.VISIBLE:
		    hideInfoButton.setVisibility(View.INVISIBLE);
		    hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_landscape);
		    hideInfoButton.setOnClickListener(this);
		    hideInfoButton.setVisibility(View.VISIBLE);
		    break;

		default:
		    hideInfoButton = (Button) findViewById(edu.fsu.cs.argame.R.id.more_information_button_hide_landscape);
		    hideInfoButton.setOnClickListener(this);
		    break;
		}
	    }
	    break;
	}

    }

    /**
     * CountDownTimer for waiting of current location
     * 
     * @return instance of the CountDownTimer
     */
    private CountDownTimer initTimer() {
	CountDownTimer countDownTimer = new CountDownTimer(TOTAL_TIME, SECONDS) {

	    @Override
	    public void onTick(long millisUntilFinished) {
	    }

	    @Override
	    public void onFinish() {
		handler.sendEmptyMessage(CANCEL_WIFI_3G_DIALOG);
	    };
	};

	return countDownTimer;
    }

    /**
     * Returns the initialized progress dialog while waiting for current
     * location
     * 
     * @returns instace of the ProgressDialog
     */
    private ProgressDialog initLocationProgressDialog() {
	ProgressDialog progressDialog = new ProgressDialog(this);
	progressDialog.setOnCancelListener(new OnCancelListener() {

	    @Override
	    public void onCancel(DialogInterface dialog) {

		countDownTimer.cancel();

		AlertDialog.Builder builder = new AlertDialog.Builder(MixView.this);
		AlertDialog alert;
		builder.setTitle(edu.fsu.cs.argame.R.string.mixview_dialog_title);
		builder.setCancelable(false);
		builder.setPositiveButton(edu.fsu.cs.argame.R.string.setup_dialog_yes, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			pressedMarker = null;
			try {
			    ctx.downloadManager.stop();
			} catch (Exception e) {
			    e.printStackTrace();
			}
			clearArrayList();
			arMarkers.clear();
			clearImageMapping();
			clearBitmaps();
			stopSensors();
			stopLocationListeners();

			startThread = false;
			finish();
			System.exit(1);
		    }
		}).setNegativeButton(edu.fsu.cs.argame.R.string.setup_dialog_no, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();

			if (MixView.ctx.curLoc == null) {
			    MixView.this.progressDialog = initLocationProgressDialog();
			    MixView.this.progressDialog.show();
			    countDownTimer.start();
			}
		    }
		});
		alert = builder.create();
		alert.show();
	    }
	});

	progressDialog.setIndeterminate(true);
	progressDialog.setMessage(getText(edu.fsu.cs.argame.R.string.waiting_for_gps_signal));
	progressDialog.setCancelable(true);

	return progressDialog;
    }

    /**
     * Returns the handler for managing messages from different threads and
     * activities
     * 
     * @return instance of the handler
     */
    private Handler initMixViewHandler() {

	return new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		switch (msg.what) {
		case WIFI_3G_TOAST:
		    if (!downloadFailed) {
			Toast.makeText(MixView.this, edu.fsu.cs.argame.R.string.search_no_network_after_retry, Toast.LENGTH_LONG).show();
			downloadFailed = true;
		    }
		    break;
		case CANCEL_WIFI_3G_DIALOG:

		    if (progressDialog != null)
			progressDialog.dismiss();

		    if (MixView.isOnScreen && MixView.ctx.curLoc == null) {

			Toast.makeText(MixView.this, edu.fsu.cs.argame.R.string.no_gps_signal, Toast.LENGTH_LONG).show();
		    }
		default:
		    break;
		}
	    }
	};
    }

    /**
     * Initialize map components. Sets the visibility of the map GONE at start.
     */
    private void initMapComponents() {

	mapFrame = layoutInflater.inflate(edu.fsu.cs.argame.R.layout.gmap, null);
	mapView = (MapView) mapFrame.findViewById(edu.fsu.cs.argame.R.id.mapview);
	mapView.setBuiltInZoomControls(true);
	mapFrame.setVisibility(View.GONE);

	mc = mapView.getController();
	mapOverlay = new MapOverlay(this, null);
	mapOverlay.setSearchedLocation();
	mapView.getOverlays().add(mapOverlay);
	mapView.invalidate();

    }

    /**
     * adding of map overlay markers
     */
    public void addMapMarkers() {
	touch = 0;
	mapOverlay.clearLocationList();
	mapOverlay.currentlocation = new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6));

	for (int i = 0; i < arMarkers.size(); i++) {
	    if (arMarkers.get(i).mGeoLoc.getDistance() <= arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS))
		mapOverlay.setLocationList(arMarkers.get(i));
	}
	mapOverlay.setSearchedLocation();
	touch = 1;
    }

    /**
     * initialize bitmap for radar, POI background for overlay
     */
    protected void initBitmaps() {
	Resources resources = getResources();
	angle = BitmapFactory.decodeResource(resources, edu.fsu.cs.argame.R.drawable.myposition_dot);
	poiBg = new Bitmap[2];
	poiBg[PaintScreen.DEFAULT] = BitmapFactory.decodeResource(resources, edu.fsu.cs.argame.R.drawable.poi_bg_default);
	poiBg[PaintScreen.TARGET] = BitmapFactory.decodeResource(resources, edu.fsu.cs.argame.R.drawable.poi_bg_target);
	radarBitmap = new Bitmap[radarIconArray.length];
	for (int i = 0; i < radarIconArray.length; i++)
	    radarBitmap[i] = BitmapFactory.decodeResource(resources, radarIconArray[i]);
    }

    /**
     * clears the bitmap used
     */
    private void clearBitmaps() {
	try {

	    angle.recycle();
	    angle = null;

	    for (Bitmap bitmap : poiBg) {
		bitmap.recycle();
		bitmap = null;
	    }
	    poiBg = null;

	    for (Bitmap bitmap : radarBitmap) {
		bitmap.recycle();
		bitmap = null;
	    }
	    radarBitmap = null;
	} catch (Exception e) {
	}

    }

    /**
     * adding of layers on the current window
     */
    private void initLayers() {
	frameLayout.addView(augScreen, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
	frameLayout.addView(cameraView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

	frameLayout.addView(mapFrame);
	frameLayout.bringChildToFront(poiDetails);
	frameLayout.bringChildToFront(showInfoButton);
	frameLayout.bringChildToFront(hideInfoButton);
	frameLayout.addView(compassView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

    }

    /**
     * initialize sensors for accelerometer and location
     */
    protected void initSensors() {
	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
	locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    protected void stopLocationListeners() {
	try {
	    locationMgr.removeUpdates(locationListenerGps);
	} catch (Exception e) {
	}
	try {
	    locationMgr.removeUpdates(locationListenerNetwork);
	} catch (Exception e) {
	}
    }

    /**
     * stop sensor for accelerometer
     */
    protected void stopSensors() {
	try {
	    sensorMgr.unregisterListener(this, sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
	} catch (Exception e) {
	}
	try {
	    sensorMgr.unregisterListener(this, sensorMgr.getDefaultSensor(SensorManager.SENSOR_MAGNETIC_FIELD));
	} catch (Exception e) {
	}
    }

    protected void startLocationListeners() {

	locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINTIME, distanceDownload, locationListenerGps);
	locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINTIME, distanceDownload, locationListenerNetwork);
    }

    /**
     * start sensor for accelerometer
     */
    protected void startSensors() {
	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    /*
     * method overriden for faster change in screen orientation and manual
     * handling of change in orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
	windowOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

	initInfoButtons();
	frameLayout.bringChildToFront(showInfoButton);
	frameLayout.bringChildToFront(hideInfoButton);
	frameLayout.bringChildToFront(compassView);

	if (cameraView != null) {
	    cameraView.closeCamera();

	    if (frameLayout != null) {
		frameLayout.removeView(cameraView);
		cameraView = new CameraView(this);
		frameLayout.addView(cameraView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    }
	}

	super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
	super.onPause();

	if (cameraView != null)
	    cameraView.closeCamera();

	isOnScreen = false;

	if (progressDialog != null) {
	    progressDialog.dismiss();
	}
	DataView.onScreenMarkers.clear();
	hideArrows();
	stopSensors();
	try {
	    this.mWakeLock.release();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    public void onNewIntent(Intent newIntent) {
	setIntent(newIntent);
    }

    @Override
    protected void onResume() {
	super.onResume();

	ctx.setMixView(this);
	initInfoButtons();
	// updateUrlRequest();
	hideArrows();
	isOnScreen = true;
	distanceDownload = (arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS)) / 2;
	pitchAngle = arSettings.getInt(InitActivity.SETUP_ANGLE, InitActivity.DEFAULT_ANGLE);

	frameLayout.bringChildToFront(showInfoButton);
	frameLayout.bringChildToFront(hideInfoButton);
	frameLayout.bringChildToFront(compassView);

	if (cameraView.isCameraNull()) {
	    if (frameLayout != null) {
		frameLayout.removeView(cameraView);
		cameraView = new CameraView(this);
		frameLayout.addView(cameraView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    }
	}

	if (MixView.ctx.curLoc == null) {
	    progressDialog = initLocationProgressDialog();
	    countDownTimer = initTimer();
	    progressDialog.show();
	    countDownTimer.start();
	}

	if (!checkNetworkStatus())
	    Toast.makeText(this, edu.fsu.cs.argame.R.string.search_no_network, Toast.LENGTH_LONG).show();

	try {
	    category = getIntent().getExtras().getString(CATEGORY);
	} catch (Exception e) {
	    category = SHOW_ALL;
	}

	categoryArray = category.split(SearchActivity.DELIMITER);

	if ((pressedMarker != null) && !category.equals(FAVORITES))
	    showPoiDetails(pressedMarker);

	try {
	    this.mWakeLock.acquire();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    startSensors();
	    angleX = Math.toRadians(-90);
	    m1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math.cos(angleX));

	    angleX = Math.toRadians(-90);
	    angleY = Math.toRadians(-90);
	    m2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math.cos(angleX));
	    m3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
	    m4.toIdentity();
	    for (int i = 0; i < histR.length; i++)
		histR[i] = new Matrix();

	    if (MixView.ctx.curLoc != null) {
		mapOverlay.currentlocation = new GeoPoint((int) (ctx.curLoc.getLatitude() * 1E6), (int) (ctx.curLoc.getLongitude() * 1E6));

		computeGeomagneticField();
	    }
	    mapView.invalidate();
	    repaint();
	    updateArMarkers();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    try {
		if (sensorMgr != null) {
		    sensorMgr.unregisterListener(this, sensorGrav);
		    sensorMgr.unregisterListener(this, sensorMag);
		    sensorMgr = null;
		}
		if (locationMgr != null) {
		    locationMgr.removeUpdates(locationListenerGps);
		    locationMgr.removeUpdates(locationListenerNetwork);
		    locationMgr = null;
		}
		if (ctx != null) {
		    if (ctx.downloadManager != null)
			ctx.downloadManager.stop();

		}
	    } catch (Exception ignore) {
	    }
	}
    }

    /**
     * requsting updates from server
     */
    public void repaint() {
	setZoomLevel();
	if ((MixView.ctx.curLoc != null) && (MixState.nextLStatus == MixState.NOT_STARTED)) {
	    try {
		forkDownloadThread();
	    } catch (Exception e) {
	    }
	}

    }

    /**
     * reset variables on new data, start thread for requesting data from server
     */
    public static void forkDownloadThread() {
	MixView.ctx.getDataView().doStart();
	DataView.onScreenMarkers.clear();

	try {
	    ctx.downloadManager.stop();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	clearArrayList();

	SearchActivity.nearbyDownload = true;
	ctx.downloadManager = new DownloadManager(ctx);
	downloadThread = new Thread(ctx.downloadManager);
	downloadThread.start();

    }

    /**
     * clear markers on AR view, map overlay, and retain selected POI
     */
    protected static void clearArrayList() {

	ArrayList<Marker> markers = ctx.dataView.markers;

	if (pressedMarker != null) {
	    List<Marker> retainMarker = new ArrayList<Marker>();
	    retainMarker.add(pressedMarker);
	    markers.retainAll(retainMarker);
	} else
	    markers.clear();
	System.gc();

	if (!category.equals(FAVORITES)) {
	    mapOverlay.clearLocationList();

	    if (pressedMarker != null) {
		List<Marker> retainMarker = new ArrayList<Marker>();
		retainMarker.add(pressedMarker);
		arMarkers.retainAll(retainMarker);
	    } else
		arMarkers.clear();
	}

    }

    @Override
    protected void onDestroy() {

	isRunning = false;
	frameLayout.removeAllViews();
	compassView = null;
	frameLayout = null;
	poiDetails = null;
	poiIcon = null;
	poiTitle = null;
	poiDistance = null;
	poiAddress = null;
	poiTelephone = null;
	callButton = null;
	moreInformation = null;
	showInfoButton = null;
	hideInfoButton = null;
	rightArrow = null;
	leftArrow = null;
	super.onDestroy();

	System.gc();
    }

    /**
     * update the current request parameter to server based on current location
     * and settings of radius
     */
    // protected void updateUrlRequest() {
    //
    // try {
    // ArrayList<Double> result = GRSConverter.LLtoKTM(
    // ctx.curLoc.getLongitude(), ctx.curLoc.getLatitude());
    // AR_WEBSERVER_URL = AR_WEBSERVER
    // + "fx="
    // + result.get(0)
    // + "&fy="
    // + result.get(1)
    // + "&length="
    // + arSettings.getInt(ARMap.SETUP_SEARCH_RADIUS,
    // ARMap.DEFAULT_SEARCH_RADIUS);
    // } catch (NullPointerException e) {
    // AR_WEBSERVER_URL = AR_WEBSERVER;
    // }
    // }

    /*
     * computation of magnetic field, used in augmented reality
     */
    private void computeGeomagneticField() {
	try {
	    GeomagneticField gmf = new GeomagneticField((float) ctx.curLoc.getLatitude(), (float) ctx.curLoc.getLongitude(), (float) ctx.curLoc.getAltitude(), System.currentTimeMillis());

	    angleY = Math.toRadians(-gmf.getDeclination());
	    m4.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
	    ctx.declination = gmf.getDeclination();

	} catch (Exception e) {
	}

    }

    /**
     * update the current markers to overlay in augmented reality based on
     * current category
     */
    protected void updateArMarkers() {
	DataView.onScreenMarkers.clear();
	if (!category.equals("") && !category.equals(FAVORITES)) {
	    ArrayList<Marker> markers = MixView.ctx.getDataView().markers;
	    arMarkers.clear();
	    mapOverlay.clearLocationList();

	    synchronized (markers) {
		if (markers.size() != 0) {
		    synchronized (arMarkers) {
			for (Marker marker : markers) {
			    String markerCategory = marker.getMainCategory();
			    if (category.equals(SHOW_ALL) || isInCategory(markerCategory)) {
				arMarkers.add(marker);
				addMapMarkers();
			    }
			}
			Collections.sort(arMarkers, new MarkersOrder());
		    }
		}
	    }
	} else if (category.equals(FAVORITES)) {// retrieving of favorites from
						// local database
	    pressedMarker = null;
	    poiDetails.setVisibility(View.INVISIBLE);
	    moreInformation.setVisibility(View.INVISIBLE);
	    showInfoButton.setVisibility(View.INVISIBLE);
	    hideInfoButton.setVisibility(View.INVISIBLE);

	    synchronized (arMarkers) {
		try {
		    Bundle bundle = getIntent().getExtras();
		    int favoriteId = bundle.getInt(FAVORITES_ID);
		    Log.i("MixView", "MixView --> FAVORITES --> FAVORITES_ID: " + favoriteId);

		    arMarkers.clear();

		    HashMap<String, String> categoryHash = ctx.getCategoryHash();
		    HashMap<String, Integer> categoryIndexMap = ctx.getCategoryIndexMap();
		    ArrayList<ImageBundle> poiImagesList = ctx.getPoiImagesList();

		    dbHelper.open();
		    Cursor cursor;

		    cursor = dbHelper.fetch(dbHelper.getDb(), null, null);
		    if (cursor.moveToFirst()) {
			do {

			    try {
				int id = cursor.getInt(cursor.getColumnIndex(ARMapDB.COLUMN_ID));
				String title = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_TITLE));
				String phoneNum = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_PHONE_NUM));
				String category = cursor.getString(cursor.getColumnIndex(ARMapDB.COLUMN_CATEGORY));
				double longitude = cursor.getDouble(cursor.getColumnIndex(ARMapDB.COLUMN_LONGITUDE));
				double latitude = cursor.getDouble(cursor.getColumnIndex(ARMapDB.COLUMN_LATITUDE));

				Marker ma = new Marker();
				ma.setFavoriteId(id);

				PhysicalPlace refpt = new PhysicalPlace();
				Location poiLoc = new Location(ctx.getCurrentLocation());
				poiLoc.setLatitude(latitude);
				poiLoc.setLongitude(longitude);

				ma.setmText(title);
				ma.setCategory(category);
				ma.setBuilding("");
				ma.setAddress("");
				ma.setPnu(phoneNum);

				refpt.setLatitude(latitude);
				refpt.setLongitude(longitude);
				refpt.setAltitude(0);
				refpt.setDistance(ctx.getCurrentLocation().distanceTo(poiLoc));
				refpt.setBearing(ctx.getCurrentLocation().bearingTo(poiLoc));
				ma.mGeoLoc.setTo(refpt);
				Integer listIndex = categoryIndexMap.get(category);

				if (listIndex == null) {
				    Resources resources = ctx.getMixView().getResources();

				    String stringId = categoryHash.get(category);
				    String[] drawables = stringId.split(":");
				    int size = poiImagesList.size();

				    ImageBundle imageBundle = new ImageBundle();
				    imageBundle.setIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[0])));
				    imageBundle.setFocusIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[1])));
				    imageBundle.setBigIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[2])));

				    android.graphics.Matrix matrix = new android.graphics.Matrix();
				    matrix.postScale(0.5f, 0.5f);
				    imageBundle.setMapIcon(Bitmap.createBitmap(imageBundle.getIcon(), 0, 0, imageBundle.getIcon().getWidth(), imageBundle.getIcon().getHeight(), matrix, false));
				    imageBundle.setMapFocusIcon(Bitmap.createBitmap(imageBundle.getFocusIcon(), 0, 0, imageBundle.getFocusIcon().getWidth(), imageBundle.getFocusIcon().getHeight(),
					    matrix, false));

				    poiImagesList.add(imageBundle);
				    categoryIndexMap.put(category, size);
				    listIndex = size;
				}

				ImageBundle imageBundle = poiImagesList.get(listIndex);
				ma.setIcon(imageBundle.getIcon());
				ma.setFocusIcon(imageBundle.getFocusIcon());
				ma.setBigIcon(imageBundle.getBigIcon());
				ma.setMapIcon(imageBundle.getMapIcon());
				ma.setMapFocusIcon(imageBundle.getMapFocusIcon());
				ma.setMainCategory(MixContext.getCategory(category));

				if (favoriteId == id)
				    showPoiDetails(ma);

				arMarkers.add(ma);
				addMapMarkers();
				// }
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			} while (cursor.moveToNext());
		    }
		    dbHelper.close();
		    cursor.close();

		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public void showRightArrow() {
	leftArrow.setVisibility(View.INVISIBLE);
	rightArrow.setVisibility(View.VISIBLE);
    }

    public void showLeftArrow() {
	rightArrow.setVisibility(View.INVISIBLE);
	leftArrow.setVisibility(View.VISIBLE);
    }

    public void hideArrows() {
	try {
	    rightArrow.setVisibility(View.INVISIBLE);
	    leftArrow.setVisibility(View.INVISIBLE);
	} catch (NullPointerException e) {
	}
    }

    public void setArrowVisibility(float targetAngle, Marker ma) {
	MixView mixView = MixView.ctx.getMixView();

	if (targetAngle > 0) {
	    targetAngle = targetAngle % 360;
	} else if (targetAngle < 0) {
	    targetAngle = Math.abs(targetAngle);
	    targetAngle = targetAngle % 360;
	    targetAngle = 360 - targetAngle;
	}

	if (ma.isOnScreen())
	    mixView.hideArrows();
	else if ((targetAngle > 0) && (targetAngle < 180))
	    mixView.showRightArrow();
	else
	    mixView.showLeftArrow();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(edu.fsu.cs.argame.R.menu.main_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent;
	switch (item.getItemId()) {
//	case edu.fsu.cs.argame.R.id.main_menu_search:
//	    if (MixState.nextLStatus == MixState.PROCESSING)
//		SearchActivity.nearbyDownload = true;
//	    intent = new Intent(this, edu.fsu.cs.argame.TabModeActivity.class);
//	    startActivity(intent);
//	    mapFrame.setVisibility(View.INVISIBLE);
//	    break;
//	case edu.fsu.cs.argame.R.id.main_menu_favorites:
//	    intent = new Intent(this, edu.fsu.cs.argame.AddFavoriteActivity.class);
//	    startActivity(intent);
//	    break;
	case edu.fsu.cs.argame.R.id.main_menu_my_location:
	      if (currentLocation != null) {
	        mapView.getController().animateTo(currentLocation);
	      }
	    break;
	case edu.fsu.cs.argame.R.id.main_menu_map_mode:
	    if (mapModeSat == false)  {	    
		mapView.setSatellite(false);
		mapModeSat = true;
	    }
	    else if (mapModeSat == true)  {
		mapView.setSatellite(true);
		mapModeSat = false;
	    }
	    break;
	case edu.fsu.cs.argame.R.id.main_menu_new_game:
	    intent = new Intent(this, edu.fsu.cs.argame.SettingsActivity.class);
	    startActivity(intent);
	    break;
	case edu.fsu.cs.argame.R.id.main_menu_about:
	    intent = new Intent(this, edu.fsu.cs.argame.SettingsActivity.class);
	    startActivity(intent);
	    break;
	case edu.fsu.cs.argame.R.id.main_menu_setup:
	    intent = new Intent(this, edu.fsu.cs.argame.SettingsActivity.class);
	    startActivity(intent);
	    break;
//	case edu.fsu.cs.argame.R.id.main_menu_cur_info:
//		Location currentGPSInfo = mixContext.getCurrentGPSInfo();
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(getString(DataView.GENERAL_INFO_TEXT)+ "\n\n" +
//				getString(DataView.GPS_LONGITUDE) + currentGPSInfo.getLongitude() + "\n" +
//				getString(DataView.GPS_LATITUDE) + currentGPSInfo.getLatitude() + "\n" +
//				getString(DataView.GPS_ALTITUDE)+ currentGPSInfo.getAltitude() + "m\n" +
//				getString(DataView.GPS_SPEED) + currentGPSInfo.getSpeed() + "km/h\n" +
//				getString(DataView.GPS_ACCURACY) + currentGPSInfo.getAccuracy() + "m\n" +
//				getString(DataView.GPS_LAST_FIX) + new Date(currentGPSInfo.getTime()).toString() + "\n");
//		builder.setNegativeButton(getString(DataView.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				dialog.dismiss();
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.setTitle(getString(DataView.GENERAL_INFO_TITLE));
//		alert.show();
//		break;
//		/*Case 6: license agreements*/
//	case 7:
//		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//		builder1.setMessage(getString(DataView.LICENSE_TEXT));	
//		/*Retry*/
//		builder1.setNegativeButton(getString(DataView.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				dialog.dismiss();
//			}
//		});
//		AlertDialog alert1 = builder1.create();
//		alert1.setTitle(getString(DataView.LICENSE_TITLE));
//		alert1.show();
//		break;
//	}
	    
	    
	    
	default:
	    break;

	}

	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	super.onPrepareOptionsMenu(menu);
//	MenuItem menuItemSearch = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_search);
//	MenuItem menuItemFav = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_favorites);
	MenuItem menuItemMyLocation = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_my_location);
	MenuItem menuItemMapMode = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_map_mode);
	MenuItem menuItemNewGame = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_new_game);
	MenuItem menuItemAbout = menu.findItem(edu.fsu.cs.argame.R.id.main_menu_about);

	this.menuItemMyLocation = menuItemMyLocation;
	this.menuItemMapMode = menuItemMapMode;
	this.menuItemNewGame = menuItemNewGame;
	this.menuItemAbout = menuItemAbout;
	if (ctx.curLoc == null) {
	    menuItemMyLocation.setEnabled(false);
	    menuItemMapMode.setEnabled(false);
	    menuItemNewGame.setEnabled(false);
	    menuItemAbout.setEnabled(false);
	} else {
	    menuItemMyLocation.setEnabled(true);
	    menuItemMapMode.setEnabled(true);
	    menuItemNewGame.setEnabled(true);
	    menuItemAbout.setEnabled(true);
	}

	return true;
    }

    private void setZoomLevel() {
	float newRadius = (arSettings.getInt(InitActivity.SETUP_SEARCH_RADIUS, InitActivity.DEFAULT_SEARCH_RADIUS)) / 1000f;

	if (DataView.radius != newRadius) {
	    DataView.radius = newRadius;
	    MixView.ctx.getDataView().doStart();
	}

    };

    /**
     * computation of values used in overlay of POI based on accelerometer
     * values
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {

	if (firstRun) {
	    firstRun = false;
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	int newWindowOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

	if (windowOrientation != newWindowOrientation) {
	    windowOrientation = newWindowOrientation;
	    initInfoButtons();
	    frameLayout.bringChildToFront(showInfoButton);
	    frameLayout.bringChildToFront(hideInfoButton);
	    frameLayout.bringChildToFront(compassView);

	    if (cameraView != null) {
		cameraView.closeCamera();

		if (frameLayout != null) {
		    frameLayout.removeView(cameraView);

		    cameraView = new CameraView(this);
		    frameLayout.addView(cameraView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
	    }

	}

	try {

	    if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

		grav[0] = evt.values[0];
		grav[1] = evt.values[1];
		grav[2] = evt.values[2];

	    } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
		;
		mag[0] = evt.values[0];
		mag[1] = evt.values[1];
		mag[2] = evt.values[2];

	    }
	    SensorManager.getRotationMatrix(RTmp, I, grav, mag);

	    SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);
	    SensorManager.getOrientation(R, orientation);

	    orientation[0] = (float) (Math.round((Math.toDegrees(orientation[0])) * 2) / 2) + 180;
	    orientation[1] = (float) Math.round((Math.toDegrees(orientation[1])) * 2) / 2;
	    orientation[2] = (float) Math.round((Math.toDegrees(orientation[2])) * 2) / 2;

	    tempR.set(R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8]);

	    finalR.toIdentity();
	    finalR.prod(m4);
	    finalR.prod(m1);
	    finalR.prod(tempR);
	    finalR.prod(m3);
	    finalR.prod(m2);
	    finalR.invert();

	    histR[rHistIdx].set(finalR);
	    rHistIdx++;
	    if (rHistIdx >= histR.length)
		rHistIdx = 0;

	    smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
	    for (int i = 0; i < histR.length; i++) {
		smoothR.add(histR[i]);
	    }
	    smoothR.mult(1 / (float) histR.length);

	    synchronized (ctx.rotationM) {
		ctx.rotationM.set(smoothR);
	    }
	    // }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	if (augScreen != null && compassView != null) {
	    augScreen.postInvalidate();
	    compassView.postInvalidate();
	}

	hideOrShowMapView();
    }

    private void hideOrShowMapView() {
	float currentPitch = (int) orientation[1];
	mapOverlay.direction = ctx.getDataView().getState().getCurBearing();
	mapFrame.setVisibility(((currentPitch - 10 <= -pitchAngle) || (currentPitch >= pitchAngle)) ? View.VISIBLE : View.INVISIBLE);
	mapView.invalidate();
    }

    /**
     * handling of pressed POI in AR
     */
    @Override
    public boolean onTouchEvent(MotionEvent me) {
	float xPress = me.getX();
	float yPress = me.getY();

	if (me.getAction() == MotionEvent.ACTION_DOWN) {
	    MixView.ctx.getDataView().clickEvent(xPress, yPress);
	}
	return super.onTouchEvent(me);
    }

    /**
     * handlling of pressed overlay on map
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
	try {
	    if (event.getAction() == MotionEvent.ACTION_UP && mapFrame.getVisibility() == View.VISIBLE && touch == 1)
		if (mapOverlay.onTap(event.getX(), event.getY()) != null) {
		    showPoiDetails(mapOverlay.onTap(event.getX(), event.getY()));
		    mapView.invalidate();
		}
	} catch (Exception e) {
	}

	super.dispatchTouchEvent(event);

	return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

	if (keyCode == KeyEvent.KEYCODE_BACK) {
	    showExitPrompt();
	    return true;
	}

	return super.onKeyDown(keyCode, event);
    }

    /**
     * dialog prompt before exiting application
     */
    protected void showExitPrompt() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	AlertDialog alert;
	builder.setTitle(edu.fsu.cs.argame.R.string.mixview_dialog_title).setCancelable(false).setPositiveButton(edu.fsu.cs.argame.R.string.setup_dialog_yes, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		pressedMarker = null;
		try {
		    ctx.downloadManager.stop();
		} catch (Exception e) {
		    e.printStackTrace();
		}

		clearArrayList();
		arMarkers.clear();

		clearImageMapping();
		clearBitmaps();
		stopSensors();
		stopLocationListeners();

		finish();
		System.exit(1);
	    }
	}).setNegativeButton(edu.fsu.cs.argame.R.string.setup_dialog_no, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		dialog.cancel();
	    }
	});
	alert = builder.create();
	alert.show();
    }

    /**
     * clears image mapping of images used in AR
     */
    protected void clearImageMapping() {
	ArrayList<ImageBundle> poiImagesList = ctx.getPoiImagesList();

	for (ImageBundle imageBundle : poiImagesList) {
	    imageBundle.getIcon().recycle();
	    imageBundle.getFocusIcon().recycle();
	    imageBundle.getBigIcon().recycle();
	    imageBundle.getMapIcon().recycle();
	    imageBundle.getMapFocusIcon().recycle();

	    imageBundle.setIcon(null);
	    imageBundle.setFocusIcon(null);
	    imageBundle.setBigIcon(null);
	    imageBundle.setMapIcon(null);
	    imageBundle.setMapFocusIcon(null);
	}
	poiImagesList.clear();
	ctx.getCategoryIndexMap().clear();
	ctx.getCategoryHash().clear();
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case edu.fsu.cs.argame.R.id.more_information_button_show_portrait:
	case edu.fsu.cs.argame.R.id.more_information_button_show_landscape:
	    showInfoButton.setVisibility(View.INVISIBLE);
	    hideInfoButton.setVisibility(View.VISIBLE);
	    moreInformation.setVisibility(View.VISIBLE);
	    break;

	case edu.fsu.cs.argame.R.id.more_information_button_hide_portrait:
	case edu.fsu.cs.argame.R.id.more_information_button_hide_landscape:
	    hideInfoButton.setVisibility(View.INVISIBLE);
	    showInfoButton.setVisibility(View.VISIBLE);
	    moreInformation.setVisibility(View.INVISIBLE);
	    break;
	case edu.fsu.cs.argame.R.id.call_btn:
	    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + poiTelephone.getText()));
	    startActivity(dialIntent);
	    break;
	default:
	    break;
	}

    }

    public void showPoiDetails(Marker marker) {

	try {
	    if (pressedMarker != marker) {
		pressedMarker.setTarget(false);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	marker.setTarget(true);
	pressedMarker = marker;

	mapOverlay.setSearchedLocation();
	mapView.invalidate();

	poiIcon.setImageBitmap(marker.getBigIcon());
	poiTitle.setText(marker.getmText());
	poiDistance.setText(getDistanceString(marker.mGeoLoc.getDistance()));
	poiAddress.setText(marker.getAddress());
	poiTelephone.setText(marker.getPnu());

	if (poiTelephone.getText().equals("")) {
	    callButton.setVisibility(View.INVISIBLE);
	} else
	    callButton.setVisibility(View.VISIBLE);

	if (poiDetails.getVisibility() != View.VISIBLE) {
	    poiDetails.setVisibility(View.VISIBLE);
	    showInfoButton.setVisibility(View.VISIBLE);
	}

	if (category.equals(FAVORITES)) {
	    Intent newIntent = getIntent();
	    Bundle bundle = newIntent.getExtras();
	    bundle.putInt(MixView.FAVORITES_ID, pressedMarker.getFavoriteId());
	    newIntent.putExtras(bundle);
	    setIntent(newIntent);
	}

	if (poiDistance.getText().equals(currentLocationString))
	    hideArrows();

    }

    public static String getDistanceString(double distance) {
	String value = "";
	if (Math.round(distance) == 0)
	    value = currentLocationString;
	else
	    value = (distance >= SearchActivity.ONE_KILOMETER) ? (int) (distance / SearchActivity.ONE_KILOMETER) + "." + Math.round((distance % SearchActivity.ONE_KILOMETER) / 100.0)
		    + kilometerString : Math.round(distance) + meterString;

	return value;
    }

    public static boolean isInCategory(String input) {

	for (String category : categoryArray) {

	    if (category.equals(input))
		return true;
	}
	return false;
    }

    /**
     * Checks if a network provider(Wifi or 3g) is enabled
     * 
     * @return true if either wifi or 3g is enabled, false if neither of the two
     *         is available
     */
    private boolean checkNetworkStatus() {

	final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

	final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	if (!wifi.isAvailable() && !mobile.isAvailable() && !wifi.isConnected() && !mobile.isConnected())
	    return false;

	return true;
    }

    /**
     * When called, plays the default notification sound and vibrates the phone
     * for 1000 milliseconds
     */
    public void startAlarm() {
	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	v.vibrate(1000);
	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	MediaPlayer mMediaPlayer = new MediaPlayer();
	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
	    try {
		mMediaPlayer.setDataSource(this, alert);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	    } catch (Exception e) {
	    }
	}
    }

    private Thread dialogThread() {
	return new Thread(new Runnable() {

	    public void run() {
		int millisecond = 1000;
		for (int i = 0; 1 < 4; i++) {
		    try {
			Thread.sleep(millisecond);
		    } catch (InterruptedException e) {
		    }
		    if (i == 3)
			dialogHandler.sendEmptyMessage(DISMISS_DIALOG);
		}
	    }
	});
    }

    private Handler dialogHandler = new Handler() {
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case DISMISS_DIALOG:
		try {
		    speedDialogAlert.dismiss();
		} catch (Exception e) {
		}
		break;
	    }
	}
    };

    public class MapOverlayItem extends OverlayItem {
	private Marker marker;

	public MapOverlayItem(GeoPoint point, String title, String snippet, Marker marker) {
	    super(point, title, snippet);
	    this.marker = marker;
	}

	public Marker getMarker() {
	    return marker;
	}

    }

}

class AugmentedView extends View {
    protected PaintScreen dWindow;

    public AugmentedView(Context context) {
	super(context);

	dWindow = new PaintScreen();
    }

    @Override
    protected void onDraw(Canvas canvas) {

	dWindow.setWidth(canvas.getWidth());
	dWindow.setHeight(canvas.getHeight());
	dWindow.setCanvas(canvas);

	if (!MixView.ctx.getDataView().isInited()) {
	    dWindow.setFontSize(30);
	    MixView.ctx.getDataView().init(dWindow.getWidth(), dWindow.getHeight());
	}

	try {
	    MixView.ctx.getDataView().draw(dWindow);
	    if ((MixView.pressedMarker != null))
		MixView.ctx.getMixView().showPoiDetails(MixView.pressedMarker);
	} catch (NullPointerException e) {
	}
    }

}
