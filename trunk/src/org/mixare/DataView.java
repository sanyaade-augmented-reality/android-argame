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

import org.mixare.gui.PaintScreen;
import org.mixare.render.Camera;

import edu.fsu.cs.argame.marker.Marker;
import edu.fsu.cs.argame.marker.MarkersOrder;

import android.graphics.Rect;
import android.location.Location;

/**
 * @author daniele
 * 
 */
public class DataView {

	private static final float POI_LEVEL_UPPER_BOUND_PORTRAIT = 200;
	private static final float POI_LEVEL_LOWER_BOUND_PORTRAIT = -300;
	
	private static final float POI_LEVEL_UPPER_BOUND_LANDSCAPE = 95;
	private static final float POI_LEVEL_LOWER_BOUND_LANDSCAPE = -200;
	
	private static final float POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER = 200;
	private static final float POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER = -95;
	

	/** is the view Inited? */
	boolean isInit = false;
	/** width and height of the view */
	private int width;
	private int height;
	
	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	Camera cam;
	
	/** */
	private MixState state = new MixState();
	
	/** The view can be "frozen" for debug purposes */
	boolean frozen = false;
	
	private Location curFix;
	
	public float screenWidth;
	public float screenHeight;

	public ArrayList<Marker> markers = new ArrayList<Marker>();

	public static float radius = 0; // 20

	public boolean isLauncherStarted = false;
	
	ClickEvent clickEvent = null;	
	public static ArrayList<Marker> onScreenMarkers = new ArrayList<Marker>();

	public float addX = 0;
	public float addY = 0;
	public void doStart() {
		MixState.nextLStatus = MixState.NOT_STARTED;
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit) {
		try {

			width = widthInit;
			height = heightInit;
			
			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}

	/**
	 * updating of AR overlay values, prioritize selected POI, next are the previous POI on screen
	 * @param dw the current handler for AR overlay
	 */
	@SuppressWarnings("unchecked")
	public void draw(PaintScreen dw) {
		float poiLevel;
		Marker ma;
		boolean isValidMarker;
		ArrayList<Marker> markers = MixView.arMarkers;
		boolean evtHandled = false;
		ArrayList<Marker> tempMarkers = (ArrayList<Marker>)onScreenMarkers.clone();
		
		int height = dw.getHeight();
		double ratio = (height / 2) / (radius * 1000f);

		MixView.ctx.getRM(cam.transform);
		curFix = MixView.ctx.getCurrentLocation();

		state.calcPitchBearing(cam.transform);
		screenWidth = width;
		screenHeight = height;
		 
		onScreenMarkers = new ArrayList<Marker>();
		
		//updating of selected POI making it the first priority in AR overlay
		if (MixView.pressedMarker != null) {
			Marker pressedMarker = MixView.pressedMarker;
			//if (pressedMarker.mGeoLoc.getDistance() / 1000f < radius) {
				if (!frozen)
					pressedMarker.update(curFix, System.currentTimeMillis());
				
				switch (MixView.windowOrientation) {
					case MixView.PORTRAIT:
						poiLevel = -(float) ((ratio * pressedMarker.mGeoLoc.getDistance()) - ratio) + 200;
						if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_UPPER_BOUND_PORTRAIT;
						else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_LOWER_BOUND_PORTRAIT;
						pressedMarker.calcPaint(cam,poiLevel, 0);
						
						break;
					case MixView.LANDSCAPE:
						poiLevel = -(float) ((ratio * pressedMarker.mGeoLoc.getDistance()) - ratio) + 100;
						if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE;
						else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE;
						pressedMarker.calcPaint(cam, 0, poiLevel);
						
						 break;
					 default:
						 poiLevel = (float) ((ratio * pressedMarker.mGeoLoc.getDistance()) - ratio)-100;
						 
						 if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER;
						else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER;
						 
						 
						 pressedMarker.calcPaint(cam, 0, poiLevel);
					break;

				}

				if (pressedMarker.isVisible) {
					if (clickEvent != null)
						evtHandled = pressedMarker.fClick(clickEvent.x,
								clickEvent.y, MixView.ctx, state);
					onScreenMarkers.add(pressedMarker);
				}
		}

		
		//updating of previous set of POI on screen
		 for (Marker marker : tempMarkers) {
			 isValidMarker = true;
			 
			// if (marker.mGeoLoc.getDistance() / 1000f < radius) {
				 if(!frozen)marker.update(curFix, System.currentTimeMillis());
			 
				 switch (MixView.windowOrientation) {
					 case MixView.PORTRAIT:
						 poiLevel = -(float) ((ratio * marker.mGeoLoc.getDistance()) - ratio) + 200;
							if (poiLevel > 0) 
								poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_UPPER_BOUND_PORTRAIT;
							else
								poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_LOWER_BOUND_PORTRAIT;
							marker.calcPaint(cam,poiLevel, 0);
						 break;
					 case MixView.LANDSCAPE:
						 
						poiLevel = -(float) ((ratio * marker.mGeoLoc.getDistance()) - ratio) + 100;
						if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE;
						else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE;
						marker.calcPaint(cam, 0, poiLevel);
						
						 break;
					 default:
						 poiLevel = (float) ((ratio * marker.mGeoLoc.getDistance()) - ratio)-100;
						 if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER;
						 else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER;
						 marker.calcPaint(cam, 0, poiLevel);
						 break;
				 }
				 
				 if (marker.isVisible){
					 
					 isValidMarker = isMarkerValid(marker, onScreenMarkers);
				  
					 if (isValidMarker){
						 if ((clickEvent != null) && !evtHandled)
							 evtHandled = marker.fClick(clickEvent.x, clickEvent.y, MixView.ctx, state);
						 	onScreenMarkers.add(marker);
					 }
				 } 
			 //} 
		 }
		 
		//updating of remaining POI
		switch (MixView.windowOrientation) {
			case MixView.PORTRAIT:
				for (int i = 0; i < markers.size(); i++) {
	
					isValidMarker = true;
					ma = markers.get(i);
					//if (ma.mGeoLoc.getDistance() / 1000f < radius) {
						if (!frozen)
							ma.update(curFix, System.currentTimeMillis());
						
						poiLevel = -(float) ((ratio * ma.mGeoLoc.getDistance()) - ratio) + 200;
						if (poiLevel > 0) 
							poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_UPPER_BOUND_PORTRAIT;
						else
							poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_PORTRAIT)?poiLevel:POI_LEVEL_LOWER_BOUND_PORTRAIT;
						
						ma.calcPaint(cam,poiLevel, 0);
						
						if (ma.isVisible) {
													
							isValidMarker = isMarkerValid(ma, onScreenMarkers);
	
							if (isValidMarker) {
								if ((clickEvent != null) && !evtHandled)
									evtHandled = ma.fClick(clickEvent.x,
											clickEvent.y, MixView.ctx, state);
								onScreenMarkers.add(ma);
							}
						}
					//}
				}
				dw.getCanvas().save();
				dw.getCanvas().translate(dw.getWidth() / 2, dw.getHeight() / 2);
				dw.getCanvas().rotate(90);
				dw.getCanvas().translate(-(dw.getWidth() / 2),
						-(dw.getHeight() / 2));
				dw.getCanvas().translate(-170, 170);
				
				sortAndDrawMarkers(onScreenMarkers, dw);
				
				dw.getCanvas().restore();
	
			break;

		case MixView.LANDSCAPE:
			for (int i = 0; i < markers.size(); i++) {

				ma = markers.get(i);
				//if (ma.mGeoLoc.getDistance() / 1000f < radius) {
					if (!frozen)
						ma.update(curFix, System.currentTimeMillis());
					
					poiLevel = -(float) ((ratio * ma.mGeoLoc.getDistance()) - ratio) + 100;
					
					if (poiLevel > 0) 
						poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE;
					else
						poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE;
					
					
					ma.calcPaint(cam, 0, poiLevel);

					isValidMarker = true;

					if (ma.isVisible) {
						isValidMarker = isMarkerValid(ma, onScreenMarkers);
						if (isValidMarker) {
							if ((clickEvent != null) && !evtHandled)
								evtHandled = ma.fClick(clickEvent.x,
										clickEvent.y, MixView.ctx, state);
							onScreenMarkers.add(ma);
						}
					}
				//}
			}
			
			sortAndDrawMarkers(onScreenMarkers, dw);

			break;
			
		default:
			for (int i = 0; i < markers.size(); i++) {

				ma = markers.get(i);
				//if (ma.mGeoLoc.getDistance() / 1000f < radius) {
					if (!frozen)
						ma.update(curFix, System.currentTimeMillis());
					
					poiLevel = (float) ((ratio * ma.mGeoLoc.getDistance()) - ratio)-100;
					 if (poiLevel > 0) 
						poiLevel = (poiLevel <= POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_UPPER_BOUND_LANDSCAPE_OTHER;
					else
						poiLevel = (poiLevel >= POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER)?poiLevel:POI_LEVEL_LOWER_BOUND_LANDSCAPE_OTHER;
					 ma.calcPaint(cam, 0, poiLevel);

					isValidMarker = true;

					if (ma.isVisible) {
						isValidMarker = isMarkerValid(ma, onScreenMarkers);

						if (isValidMarker) {
							if ((clickEvent != null) && !evtHandled)
								evtHandled = ma.fClick(clickEvent.x,
										clickEvent.y, MixView.ctx, state);
							onScreenMarkers.add(ma);
						}
					}
				//}
			}
			
			dw.getCanvas().save();
			dw.getCanvas().translate(dw.getWidth() / 2, dw.getHeight() / 2);
			dw.getCanvas().rotate(180);
			dw.getCanvas().translate(-(dw.getWidth() / 2),
					-(dw.getHeight() / 2));
			sortAndDrawMarkers(onScreenMarkers, dw);
			
			dw.getCanvas().restore();

			break;
		}

		clickEvent = null;
	}

	/**
	 * checking for validy of AR overlay based on bounds
	 * @param ma the marker the will be checked for validity
	 * @param onScreenMarkers2 current markers on screen
	 * @return
	 */
	private boolean isMarkerValid(Marker ma, ArrayList<Marker> onScreenMarkers2) {
		Rect bounds = ma.getBounds();
		
		for (Marker marker : onScreenMarkers) {
			if (Rect.intersects(marker.getBounds(), bounds)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Sort the markers in ascending order before drawing on the canvas
	 * @param onScreenMarkers2 markers to draw
	 * @param dw instance of PaintScreen that will be used for drawing the overlay
	 */
	private void sortAndDrawMarkers(ArrayList<Marker> onScreenMarkers2,
			PaintScreen dw) {
		Collections.sort(onScreenMarkers, new MarkersOrder());
		
		for (int i = onScreenMarkers.size() - 1; i >= 0; i--) {
			Marker marker = onScreenMarkers.get(i);
			
			if ((MixView.pressedMarker == marker) || (marker.mGeoLoc.getDistance() / 1000f < radius)) {
				try {
					if (marker.mGeoLoc.getDistance() != 0)
						marker.draw(dw);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

	/**
	 * method for handling clicks on POI
	 * @param x
	 * @param y
	 */
	public void clickEvent(float x, float y) {
		float xValue;
		float yValue;

		switch (MixView.windowOrientation) {
			case MixView.PORTRAIT:
				xValue = y - 30;
				yValue = 480 - x;
				break;
	
			case MixView.LANDSCAPE:
				xValue = x;
				yValue = y - 20;
				break;
				
			default:
				xValue = 800-x;
				yValue = 480- y+20;
				break;
		}

		clickEvent = new ClickEvent(xValue, yValue);
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width	the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the state
	 */
	public MixState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(MixState state) {
		this.state = state;
	}

}

/**
 * Class for encapsulation of click event
 * @author winifredo
 */
class UIEvent {
	public static int CLICK = 0, KEY = 1;
	public int type;
}

/**
 * Class for encapsulation of click event
 * @author winifredo
 */
class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
