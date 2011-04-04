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
package org.mixare.gui;

import java.util.ArrayList;

import org.mixare.DataView;
import org.mixare.MixView;


import edu.fsu.cs.argame.CompassView;
import edu.fsu.cs.argame.marker.Marker;

import android.graphics.Color;

/** Takes care of the small radar in the top right corner and of its points.
 * This class performs the conversion/scaling of the markers location to the radar points coordinates
 * @author daniele
 */
public class RadarPoints implements ScreenObj {
	
	/** The screen */
	public CompassView view;
	
	/** The radar's range */
	float range;
	
	/** Radius in pixel on screen */
	public static float RADIUS = 40;
	
	/** Position on screen */
	static float originX = 0 , originY = 0;
	
	/** Color */
	static int radarColor = Color.argb(100, 0, 0, 200);
	
	/** Default color of the radar points **/
	int defaultPoiColor = Color.YELLOW;
	
	/** Array of possible colors for the radar points **/
	int colorArray[] = new int[]{Color.YELLOW, Color.RED, Color.WHITE, Color.BLACK};
	
	private final int POI_RADIUS_HERO = 2;
	private final int POI_RADIUS_GALAXY_S = 3;
	
	public void paint(PaintScreen dw) {
		
		if (MixView.radarBitmap != null) {
			range = DataView.radius * 1000; 
			float scale = range / RADIUS;
			
			if (MixView.pressedMarker != null) {
				synchronized (MixView.pressedMarker) {
					/** When the target marker is the current location, a big red icon is draw on the center of the compass **/
					if (Math.round(MixView.pressedMarker.mGeoLoc.getDistance()) == 0) {
						
						ArrayList<Marker> markers = MixView.arMarkers;
						/** Draws all the markers on the radar points with the default radar color **/
						for (int i = 0; i < markers.size(); i++) {
							Marker pm = markers.get(i);
							float x = pm.loc.x / scale;
							float y = pm.loc.z / scale;

							if ((pm.mGeoLoc.getDistance() / 1000f < DataView.radius) && (x * x + y * y < RADIUS * RADIUS)) {
								dw.setFill(true);
								dw.setColor(defaultPoiColor);
								dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
								dw.paintCircle(x + RADIUS - 1, y + RADIUS - 1, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
							}
						}

						/** Draws the target marker on the radar points with the big red radar color on the center of the compass **/
						float x = MixView.pressedMarker.loc.x / scale;
						float y = MixView.pressedMarker.loc.z / scale;
						dw.setFill(true);
						dw.setColor(Color.RED);
						dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, 10);
						dw.paintCircle(x + RADIUS - 1, y + RADIUS - 1, 10);
						
					} else {
						/** When the target marker is not the current location, the target marker is drawn in red circle **/
						
						ArrayList<Marker> markers = MixView.arMarkers;
						/** Draws all the markers on the radar points with the default radar color **/
						for (int i = 0; i < markers.size(); i++) {
							Marker pm = markers.get(i);
							float x = pm.loc.x / scale;
							float y = pm.loc.z / scale;

							if ((pm.mGeoLoc.getDistance() / 1000f < DataView.radius) && (x * x + y * y < RADIUS * RADIUS)) {
								dw.setFill(true);
								dw.setColor(defaultPoiColor);
								dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
								dw.paintCircle(x + RADIUS - 1, y + RADIUS - 1, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
							}
						}
						
						float x = MixView.pressedMarker.loc.x / scale;
						float y = MixView.pressedMarker.loc.z / scale;
						
						/** Draws the target marker on the radar points with the red radar color **/
						if ((MixView.pressedMarker.mGeoLoc.getDistance() / 1000f < DataView.radius) && (x * x + y * y < RADIUS * RADIUS)) {
							dw.setFill(true);
							dw.setColor(Color.RED);
							dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
							dw.paintCircle(x + RADIUS - 1, y + RADIUS - 1, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
						}
					}
				}
			} else {
				ArrayList<Marker> markers = MixView.arMarkers;
				
				/** If there is no target marker, all markers are drawn on the radar points with the default radar color **/
				for (int i = 0; i < markers.size(); i++) {
					Marker pm = markers.get(i);
					float x = pm.loc.x / scale;
					float y = pm.loc.z / scale;

					if ((pm.mGeoLoc.getDistance() / 1000f < DataView.radius) && (x * x + y * y < RADIUS * RADIUS)) {
						dw.setFill(true);
						dw.setColor(defaultPoiColor);
						dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
						dw.paintCircle(x + RADIUS - 1, y + RADIUS - 1, (CompassView.DEVICE==CompassView.GALAXY_S)?POI_RADIUS_GALAXY_S:POI_RADIUS_HERO);
					}
				}
			}
		}
	}

	/** @return Width on screen */
	public float getWidth() {
		return RADIUS * 2;
	}

	/** @return Height on screen */
	public float getHeight() {
		return RADIUS * 2;
	}
}

