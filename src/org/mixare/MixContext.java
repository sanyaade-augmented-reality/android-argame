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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import net.peterd.zombierun.constants.ImageBundle;

import org.mixare.render.Matrix;

import edu.fsu.cs.argame.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MixContext{
	
	private MixView mixView;
	Context ctx;

	public DownloadManager downloadManager;

	public Location curLoc=null;
	Matrix rotationM = new Matrix();

	float declination = 0f;
	private boolean actualLocation=false;

	LocationManager locationMgr;
	
	protected DataView dataView;

	private HashMap<String, String> categoryHash;
	private ArrayList<ImageBundle> poiImagesList;
	private HashMap<String, Integer> categoryIndexMap;
	
	private final static int[] subCategoryCount = {0, 13, 20, 24, 39, 52};
	private static final int READ_TIME_OUT = 30000;
	private static final int CONNECT_TIME_OUT = 30000;
	protected static TypedArray categoryArray;
	protected static TypedArray subCategoryArray;
	
	
	public MixContext(Context appCtx) {
		this.mixView = (MixView) appCtx;
		this.ctx = appCtx.getApplicationContext();

		rotationM.toIdentity();
		
		categoryHash = new HashMap<String, String>();
		poiImagesList = new ArrayList<ImageBundle>();
		categoryIndexMap = new HashMap<String, Integer>();
		
		Resources resources = getMixView().getResources();
		String category[] = resources.getStringArray(edu.fsu.cs.argame.R.array.category);
		TypedArray drawables = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable);
		TypedArray drawablesFocus = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable_focus);
		TypedArray drawablesBig = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable_big);
		
		for(int i=0; i<category.length; i++)
			categoryHash.put(category[i], drawables.getResourceId(i, 0)+":"+drawablesFocus.getResourceId(i, 0)+":"+drawablesBig.getResourceId(i, 0));
		

		
		categoryArray = resources.obtainTypedArray(R.array.category_list);
		subCategoryArray = resources.obtainTypedArray(R.array.category);
		
	}

	public boolean isActualLocation(){
		return actualLocation;
	}

	public DownloadManager getDownloader() {
		return downloadManager;
	}
	public void setLocationManager(LocationManager locationMgr){
		this.locationMgr= locationMgr;
	}
	public LocationManager getLocationManager(){
		return this.locationMgr;
	}


	public String getStartUrl() {
		Intent intent = ((Activity) mixView).getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) { 
			return intent.getData().toString(); 
		} 
		else { 
			return ""; 
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	public Location getCurrentLocation() {
		synchronized (curLoc) {
			return curLoc;
		}
	}

	/**
	 * returns the inputstream that will be used for downloading data from server
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public InputStream getHttpGETInputStream(String urlStr)
	throws Exception {
		InputStream is = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(READ_TIME_OUT);
			conn.setConnectTimeout(CONNECT_TIME_OUT);
			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {
			try {
				is.close();
			} catch (Exception ignore) {			
			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {			
			}
			
			throw ex;				

		}
	}

	/**
	 * return the data from the input stream represented as string
	 * @param is
	 * @return
	 */
	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				8 * 1024);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {			

				e.printStackTrace();
			}
		}			

		return sb.toString();
	}

	private static HashMap<String, String> htmlEntities;
	static {
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "à");
		htmlEntities.put("&Agrave;", "À");
		htmlEntities.put("&acirc;", "â");
		htmlEntities.put("&auml;", "ä");
		htmlEntities.put("&Auml;", "Ä");
		htmlEntities.put("&Acirc;", "Â");
		htmlEntities.put("&aring;", "å");
		htmlEntities.put("&Aring;", "Å");
		htmlEntities.put("&aelig;", "æ");
		htmlEntities.put("&AElig;", "Æ");
		htmlEntities.put("&ccedil;", "ç");
		htmlEntities.put("&Ccedil;", "Ç");
		htmlEntities.put("&eacute;", "é");
		htmlEntities.put("&Eacute;", "É");
		htmlEntities.put("&egrave;", "è");
		htmlEntities.put("&Egrave;", "È");
		htmlEntities.put("&ecirc;", "ê");
		htmlEntities.put("&Ecirc;", "Ê");
		htmlEntities.put("&euml;", "ë");
		htmlEntities.put("&Euml;", "Ë");
		htmlEntities.put("&iuml;", "ï");
		htmlEntities.put("&Iuml;", "Ï");
		htmlEntities.put("&ocirc;", "ô");
		htmlEntities.put("&Ocirc;", "Ô");
		htmlEntities.put("&ouml;", "ö");
		htmlEntities.put("&Ouml;", "Ö");
		htmlEntities.put("&oslash;", "ø");
		htmlEntities.put("&Oslash;", "Ø");
		htmlEntities.put("&szlig;", "ß");
		htmlEntities.put("&ugrave;", "ù");
		htmlEntities.put("&Ugrave;", "Ù");
		htmlEntities.put("&ucirc;", "û");
		htmlEntities.put("&Ucirc;", "Û");
		htmlEntities.put("&uuml;", "ü");
		htmlEntities.put("&Uuml;", "Ü");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public String unescapeHTML(String source, int start) {
		int i, j;

		i = source.indexOf("&", start);
		if (i > -1) {
			j = source.indexOf(";", i);
			if (j > i) {
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String) htmlEntities.get(entityToLookFor);
				if (value != null) {
					source = new StringBuffer().append(source.substring(0, i))
					.append(value).append(source.substring(j + 1))
					.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		return source;
	}

	public InputStream getHttpPOSTInputStream(String urlStr,
			String params) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, params);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(20000);

			if (params != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.write(params);
				wr.close();
			}

			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {

			try {
				is.close();
			} catch (Exception ignore) {			

			}
			try {
				os.close();
			} catch (Exception ignore) {			

			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr);
			} else {		

				throw ex;
			}
		}
	}

	public InputStream getContentInputStream(String urlStr, String params)
	throws Exception {
		ContentResolver cr = mixView.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result
					.getBytes());
		} else {
			cur.deactivate();

			throw new Exception("Invalid content:// mode " + mode);
		}
	}

	public void returnHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	public InputStream getResourceInputStream(String name) throws Exception {
		AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}

	public void returnResourceInputStream(InputStream is) throws Exception {
		if (is != null)
			is.close();
	}

	public void loadMixViewWebPage(String url) throws Exception {
		WebView webview = new WebView(mixView);
		
		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
			     view.loadUrl(url);
				return true;
			}

		});
				
		Dialog d = new Dialog(mixView) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		
		webview.loadUrl(url);
	}
	public void loadWebPage(String url, Context context) throws Exception {
		WebView webview = new WebView(context);
		
		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
			     view.loadUrl(url);
				return true;
			}

		});
				
		Dialog d = new Dialog(context) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		
		webview.loadUrl(url);
	}

	/**
	 * @return the mixView
	 */
	public MixView getMixView() {
		return mixView;
	}

	/**
	 * @param mixView the mixView to set
	 */
	public void setMixView(MixView mixView) {
		this.mixView = mixView;
	}

	/**
	 * @return the dataView
	 */
	public DataView getDataView() {
		return dataView;
	}

	/**
	 * @param dataView the dataView to set
	 */
	public void setDataView(DataView dataView) {
		this.dataView = dataView;
	}

	/**
	 * @return the poiImagesList
	 */
	public ArrayList<ImageBundle> getPoiImagesList() {
		return poiImagesList;
	}

	/**
	 * @param poiImagesList the poiImagesList to set
	 */
	public void setPoiImagesList(ArrayList<ImageBundle> poiImagesList) {
		this.poiImagesList = poiImagesList;
	}

	/**
	 * @return the categoryIndexMap
	 */
	public HashMap<String, Integer> getCategoryIndexMap() {
		return categoryIndexMap;
	}

	/**
	 * @param categoryIndexMap the categoryIndexMap to set
	 */
	public void setCategoryIndexMap(HashMap<String, Integer> categoryIndexMap) {
		this.categoryIndexMap = categoryIndexMap;
	}

	/**
	 * @return the categoryHash
	 */
	public HashMap<String, String> getCategoryHash() {
		return categoryHash;
	}

	/**
	 * @param categoryHash the categoryHash to set
	 */
	public void setCategoryHash(HashMap<String, String> categoryHash) {
		this.categoryHash = categoryHash;
	}
		
	/**
	 * returns the category of the marker
	 * @param categoryValue
	 * @return
	 */
	public static String getCategory(String categoryValue) {
		int cat = 0;
		
		for(int j = 0; j < subCategoryArray.length(); j++)
			if(categoryValue.equals(subCategoryArray.getString(j))) {
				cat = j;
			}
		
		for(int i = 1; i < subCategoryCount.length; i++)
			if(cat < subCategoryCount[i]) return categoryArray.getString(i-1);
		
		return categoryArray.getString(categoryArray.length()-1);
	}
	
}
