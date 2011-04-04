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

package org.mixare.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.peterd.zombierun.constants.ImageBundle;

import org.mixare.DownloadManager;
import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.reality.PhysicalPlace;


import edu.fsu.cs.argame.marker.Marker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;

/**
 * Handles the adding of all attributes to a new marker
 */
public class DataHandler {
	public static final int MAX_OBJECTS = 50;
	
	protected MixContext ctx;
	
	public HashMap<String, String> categoryHash;
	protected ArrayList<ImageBundle> poiImagesList;
	protected HashMap<String, Integer> categoryIndexMap;
	
	protected ArrayList<Marker> arMarkers;
	
	protected DownloadManager downloadManager;

	/**
	 * Convenience method for adding basic marker attributes
	 * @param title The title/name of the marker
	 * @param latitude the location latitude of the marker
	 * @param longitude the location in longitude of the marker
	 * @param elevation the elevation of the marker, set to 0
	 * @param link web link of the marker if ther's any
	 * @param category the category where the marker belongs
	 * @param building building name where the marker is located
	 * @param address address of the marker
	 * @param pnu phone number of the marker
	 * @param id id of the marker obtained from the data downloaded
	 */
	protected void createMarker(String title, double latitude, double longitude, int elevation, String link, String category, String building, String address, String pnu, String id) {
		arMarkers = MixView.arMarkers;
		
		/** Does no add the marker if it is already the target marker **/
		if(MixView.pressedMarker != null && !MixView.category.equals(MixView.FAVORITES) && MixView.pressedMarker.getId().equals(id)) return;
		
		PhysicalPlace refpt = new PhysicalPlace();
		Marker ma = new Marker();
		
		if(link != null && link.length()>0) {
			ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(link);
		}
		
		Location poiLoc = new Location(ctx.getCurrentLocation());
		poiLoc.setLatitude(latitude);
		poiLoc.setLongitude(longitude);
		
		ma.setmText(title);
		ma.setCategory(category);
		ma.setBuilding(building);
		ma.setAddress(address);
		ma.setPnu(pnu);
		ma.setId(id);
		
		refpt.setLatitude(latitude);
		refpt.setLongitude(longitude);
		refpt.setAltitude(elevation);
		refpt.setDistance(ctx.getCurrentLocation().distanceTo(poiLoc));
		refpt.setBearing(ctx.getCurrentLocation().bearingTo(poiLoc));
		ma.mGeoLoc.setTo(refpt);
		
		ArrayList<Marker> dataViewMarkers = ctx.getDataView().markers;
		
		try {
			/** Checks if the category index is already existing in the hashmap. It decodes new bitmap if index does not exist otherwise, it uses the existing bitmap **/
			Integer listIndex = categoryIndexMap.get(category);
			
			if (listIndex == null) {
				Resources resources = ctx.getMixView().getResources();
				
				String stringId = categoryHash.get(category);
				String[] drawables = stringId.split(":");
				int size = poiImagesList.size();
				
				/** Decodes bitmap images **/
				ImageBundle imageBundle = new ImageBundle();
				imageBundle.setIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[0])));
				imageBundle.setFocusIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[1])));
				imageBundle.setBigIcon(BitmapFactory.decodeResource(resources, Integer.parseInt(drawables[2])));
									
				Matrix matrix = new Matrix();
		        matrix.postScale(0.5f, 0.5f);
				imageBundle.setMapIcon(Bitmap.createBitmap(imageBundle.getIcon(), 0, 0, imageBundle.getIcon().getWidth(), imageBundle.getIcon().getHeight(), matrix, false));
				imageBundle.setMapFocusIcon(Bitmap.createBitmap(imageBundle.getFocusIcon(), 0, 0, imageBundle.getFocusIcon().getWidth(), imageBundle.getFocusIcon().getHeight(), matrix, false));
				
				poiImagesList.add(imageBundle);
				categoryIndexMap.put(category, size);
				
				listIndex = size;
			}
			
			/** Sets image attributes **/
			ImageBundle imageBundle = poiImagesList.get(listIndex);
			ma.setIcon(imageBundle.getIcon());
			ma.setFocusIcon(imageBundle.getFocusIcon());
			ma.setBigIcon(imageBundle.getBigIcon());
			ma.setMapIcon(imageBundle.getMapIcon());
			ma.setMapFocusIcon(imageBundle.getMapFocusIcon());
			
			ma.setMainCategory(MixContext.getCategory(category));
			
			if(!downloadManager.isStop()) {
				synchronized (dataViewMarkers) {
					dataViewMarkers.add(ma);	
					Collections.sort(dataViewMarkers, new MarkersOrder());
				}
			}
			
			if (!MixView.category.equals("") && !MixView.category.equals(MixView.FAVORITES)) {
				if (MixView.category.equals(MixView.SHOW_ALL) || MixView.isInCategory(ma.getMainCategory())) {
					if(!downloadManager.isStop()) {
						arMarkers.add(ma);
						Collections.sort(arMarkers, new MarkersOrder());
						ctx.getMixView().addMapMarkers();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
	 * Compares the markers. The closer they are the higher in the stack.
	 * @author daniele
	 */
	class MarkersOrder implements java.util.Comparator<Object> {
		public int compare(Object left, Object right) {
			Marker leftPm = (Marker) left;
			Marker rightPm = (Marker) right;

			return Double.compare(leftPm.mGeoLoc.getDistance(), rightPm.mGeoLoc.getDistance());
		}
	}
}
