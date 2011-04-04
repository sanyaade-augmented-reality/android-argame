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
package edu.fsu.cs.argame.marker;

import org.mixare.DataView;
import org.mixare.MixContext;
import org.mixare.MixState;
import org.mixare.MixView;
import org.mixare.gui.PaintScreen;
import org.mixare.gui.ScreenObj;
import org.mixare.gui.ScreenLine;
import org.mixare.gui.TextObj;
import org.mixare.reality.PhysicalPlace;
import org.mixare.render.Camera;
import org.mixare.render.MixVector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;

public class Marker {

	private int index;
	public String mOnPress;
	public PhysicalPlace mGeoLoc = new PhysicalPlace();
	public boolean isVisible;
	private boolean isLookingAt;
	float deltaCenter;
	MixVector cMarker = new MixVector();
	MixVector signMarker = new MixVector();
	MixVector oMarker = new MixVector();
	Label txtLab = new Label();
	
	MixVector tmpa = new MixVector();
	MixVector tmpb = new MixVector();
	MixVector tmpc = new MixVector();

	public MixVector loc = new MixVector();
	MixVector origin = new MixVector(0, 0, 0);
	MixVector upV = new MixVector(0, 1, 0);

	
	private String iconPathOrId;
	private Bitmap icon;
	private Bitmap focusIcon;
	private Bitmap bigIcon;
	private Bitmap mapIcon;
	private Bitmap mapFocusIcon;

	private String mText;
	private String category;
	private String building;
	private String address;
	private String pnu;
	private String id;
	TextObj textBlock;

	private boolean isDistanceVisible = false;

	private boolean isOnScreen;
	private boolean isTarget = false;

	private String mainCategory;
	
	private ScreenLine[] bound;
	public static final int UPPER_BOUND = 0;
	public static final int LOWER_BOUND = 1;
	private Rect bounds = new Rect();
	
	private int favoriteId;
	
	void cCMarker(MixVector originalPoint, Camera viewCam, float addX,
			float addY) {
		tmpa.set(originalPoint); // 1
		tmpc.set(upV);
		tmpa.add(loc); // 3
		tmpc.add(loc); // 3
		tmpa.sub(viewCam.lco); // 4
		tmpc.sub(viewCam.lco); // 4
		tmpa.prod(viewCam.transform); // 5
		tmpc.prod(viewCam.transform); // 5

		viewCam.projectPoint(tmpa, tmpb, addX, addY); // 6
		cMarker.set(tmpb); // 7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); // 6
		signMarker.set(tmpb); // 7

	}

	void calcV(Camera viewCam) {
		isVisible = false;
		isLookingAt = false;
		isOnScreen = false;
		deltaCenter = Float.MAX_VALUE;
		
		if (cMarker.z < -1f) {
			
			float upperBoundX = 0;
			float upperBoundY = 0;
			float lowerBoundX = viewCam.width;
			float lowerBoundY = viewCam.height;
			
			
			isVisible = true;
			
			if (pointInside(cMarker.x, cMarker.y, upperBoundX, upperBoundY, lowerBoundX,
					lowerBoundY)) {

				isOnScreen = true;
				
				float xDist = cMarker.x - viewCam.width / 2;
				float yDist = cMarker.y - viewCam.height / 2;
				float dist = xDist * xDist + yDist * yDist;

				deltaCenter = (float) Math.sqrt(dist);

				if (dist < 50 * 50)
					isLookingAt = true;
			}
		}
	}

	private boolean pointInside(float P_x, float P_y, float r_x,
			float r_y, float r_w, float r_h) {
			return (P_x > r_x && P_x < r_x + r_w && P_y > r_y && P_y < r_y + r_h);
		}
	
	public void update(Location curGPSFix, long time) {
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, loc);
	}

	/**
	 * computation of the projection of marker based on current sensor values
	 * @param viewCam
	 * @param addX
	 * @param addY
	 */
	public void calcPaint(Camera viewCam, float addX, float addY) {
		try {
			
			synchronized (mGeoLoc) {
				Location poiLoc = new Location(MixView.ctx.curLoc);
				poiLoc.setLongitude(mGeoLoc.getLongitude());
				poiLoc.setLatitude(mGeoLoc.getLatitude());
				
				double currentDistance = MixView.ctx.curLoc.distanceTo(poiLoc);
				mGeoLoc.setDistance(currentDistance);
				mGeoLoc.setBearing(MixView.ctx.curLoc.bearingTo(poiLoc));	
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		cCMarker(origin, viewCam, addX, addY);
		calcV(viewCam);
		bound = getBounds(cMarker, MixView.windowOrientation);
				
		switch (MixView.windowOrientation) {
			case MixView.PORTRAIT:
				bounds.set((int)bound[UPPER_BOUND].x, (int)bound[LOWER_BOUND].y, (int)bound[LOWER_BOUND].x, (int)bound[UPPER_BOUND].y);
				break;
			default:
				bounds.set((int)bound[UPPER_BOUND].x, (int)bound[UPPER_BOUND].y, (int)bound[LOWER_BOUND].x, (int)bound[LOWER_BOUND].y);
			break;
		}

	}

	void calcPaint(Camera viewCam) {
		cCMarker(origin, viewCam, 0, 0);
	}

	/**
	 * checking for validity of click on the overlay
	 * @param x
	 * @param y
	 * @return
	 */
	boolean isClickValid(float x, float y) {
		return bounds.contains((int)x, (int)y);
	}
	
	/**
	 * return bounds of overlay based on current orientation of device
	 * @param point
	 * @param windowOrientation
	 * @return
	 */
	public static ScreenLine[] getBounds(MixVector point, int windowOrientation){
		ScreenLine bounds[] = new ScreenLine[2];
		
		switch (windowOrientation) {
			case MixView.PORTRAIT:
				bounds[UPPER_BOUND] = new ScreenLine(point.x - 40, point.y + 50);
				bounds[LOWER_BOUND] = new ScreenLine(point.x + 80, point.y - 50);
				break;
			case MixView.LANDSCAPE:
				bounds[UPPER_BOUND] = new ScreenLine(point.x - 50, point.y - 40);
				bounds[LOWER_BOUND] = new ScreenLine(point.x + 50, point.y + 80);
				break;
			default:
				bounds[UPPER_BOUND] = new ScreenLine(point.x - 50, point.y - 80);
				bounds[LOWER_BOUND] = new ScreenLine(point.x + 50, point.y + 40);
				
				break;
		}
		
		return bounds;
	}

	/**
	 * draw the marker overlay
	 * @param dw
	 */
	public void draw(PaintScreen dw) {
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		if (textBlock == null) {
			textBlock = new TextObj(mText, Math.round(maxHeight / 2f) + 1, 1,
					dw, this);
		}
		
		if (isVisible) {
			txtLab.prepare(textBlock);
			paintMarkerDetails(dw);
		}
	}

	/**
	 * drawing of the marker overlay details (i.e. distance from current location, icon, backround of distance)
	 * @param dw
	 */
	private void paintMarkerDetails(PaintScreen dw) {

		dw.setFontSize(30);
		dw.setColor(Color.WHITE);

		double ratio = .5 / (DataView.radius * 1000f);
		double computedValue = ratio * mGeoLoc.getDistance();
		double finalValue = 1 - computedValue;
		
		if (finalValue < .5)
			finalValue = .5;

		double scaledBitmapWidth = icon.getWidth() * finalValue;
		double scaledBitmapHeight = icon.getHeight() * finalValue;

		Canvas canvas = dw.getCanvas();
		canvas.save();

		canvas.translate(cMarker.x, cMarker.y);

		canvas.scale((float) finalValue, (float) finalValue);
		if (MixView.windowOrientation == MixView.PORTRAIT)
			canvas.rotate(-90);
		else if(MixView.windowOrientation != MixView.LANDSCAPE)
			canvas.rotate(180);
		canvas.translate(-(float) scaledBitmapWidth / 2,
				-(float) scaledBitmapHeight / 2);
		
		
		if (isTarget)
			dw.paintBitmap(focusIcon, -15, -30);
		else
			dw.paintBitmap(icon, 0, 0);
		
		dw.paintDistanceContainer(-52, icon.getHeight() + 5,
				isTarget ? PaintScreen.TARGET : PaintScreen.DEFAULT);

		String distance = (int) mGeoLoc.getDistance() + "m";
		int xPos = distance.length() * 12;
		xPos = (104 / 2) - (xPos / 2);

		dw.paintText(-32 + xPos, icon.getHeight()
				+ ((MixView.poiBg[0].getHeight() / 2) + 15), MixView.getDistanceString(mGeoLoc.getDistance()));

		canvas.restore();
	}

	/**
	 * handling of clicked overlay
	 * @param x
	 * @param y
	 * @param ctx
	 * @param state
	 * @return
	 */
	public boolean fClick(float x, float y, MixContext ctx, MixState state) {
		boolean evtHandled = false;
		if (isOnScreen && isClickValid(x, y)) {
			isTarget = true;
			evtHandled = state.handleEvent(ctx, this);
		}
		return evtHandled;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the isLookingAt
	 */
	public boolean isLookingAt() {
		return isLookingAt;
	}

	/**
	 * @param isLookingAt the isLookingAt to set
	 */
	public void setLookingAt(boolean isLookingAt) {
		this.isLookingAt = isLookingAt;
	}

	/**
	 * @return the iconPathOrId
	 */
	public String getIconPathOrId() {
		return iconPathOrId;
	}

	/**
	 * @param iconPathOrId the iconPathOrId to set
	 */
	public void setIconPathOrId(String iconPathOrId) {
		this.iconPathOrId = iconPathOrId;
	}

	/**
	 * @return the icon
	 */
	public Bitmap getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	/**
	 * @return the focusIcon
	 */
	public Bitmap getFocusIcon() {
		return focusIcon;
	}

	/**
	 * @param focusIcon the focusIcon to set
	 */
	public void setFocusIcon(Bitmap focusIcon) {
		this.focusIcon = focusIcon;
	}

	/**
	 * @return the bigIcon
	 */
	public Bitmap getBigIcon() {
		return bigIcon;
	}

	/**
	 * @param bigIcon the bigIcon to set
	 */
	public void setBigIcon(Bitmap bigIcon) {
		this.bigIcon = bigIcon;
	}

	/**
	 * @return the mapIcon
	 */
	public Bitmap getMapIcon() {
		return mapIcon;
	}

	/**
	 * @param mapIcon the mapIcon to set
	 */
	public void setMapIcon(Bitmap mapIcon) {
		this.mapIcon = mapIcon;
	}

	/**
	 * @return the mapFocusIcon
	 */
	public Bitmap getMapFocusIcon() {
		return mapFocusIcon;
	}

	/**
	 * @param mapFocusIcon the mapFocusIcon to set
	 */
	public void setMapFocusIcon(Bitmap mapFocusIcon) {
		this.mapFocusIcon = mapFocusIcon;
	}

	/**
	 * @return the mText
	 */
	public String getmText() {
		return mText;
	}

	/**
	 * @param mText the mText to set
	 */
	public void setmText(String mText) {
		this.mText = mText;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the building
	 */
	public String getBuilding() {
		return building;
	}

	/**
	 * @param building the building to set
	 */
	public void setBuilding(String building) {
		this.building = building;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the pnu
	 */
	public String getPnu() {
		return pnu;
	}

	/**
	 * @param pnu the pnu to set
	 */
	public void setPnu(String pnu) {
		this.pnu = pnu;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the isDistanceVisible
	 */
	public boolean isDistanceVisible() {
		return isDistanceVisible;
	}

	/**
	 * @param isDistanceVisible the isDistanceVisible to set
	 */
	public void setDistanceVisible(boolean isDistanceVisible) {
		this.isDistanceVisible = isDistanceVisible;
	}

	/**
	 * @return the isOnScreen
	 */
	public boolean isOnScreen() {
		return isOnScreen;
	}

	/**
	 * @param isOnScreen the isOnScreen to set
	 */
	public void setOnScreen(boolean isOnScreen) {
		this.isOnScreen = isOnScreen;
	}

	/**
	 * @return the isTarget
	 */
	public boolean isTarget() {
		return isTarget;
	}

	/**
	 * @param isTarget the isTarget to set
	 */
	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	/**
	 * @return the mainCategory
	 */
	public String getMainCategory() {
		return mainCategory;
	}

	/**
	 * @param mainCategory the mainCategory to set
	 */
	public void setMainCategory(String mainCategory) {
		this.mainCategory = mainCategory;
	}

	/**
	 * @return the bound
	 */
	public ScreenLine[] getBound() {
		return bound;
	}

	/**
	 * @param bound the bound to set
	 */
	public void setBound(ScreenLine[] bound) {
		this.bound = bound;
	}

	/**
	 * @return the bounds
	 */
	public Rect getBounds() {
		return bounds;
	}

	/**
	 * @param bounds the bounds to set
	 */
	public void setBounds(Rect bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the favoriteId
	 */
	public int getFavoriteId() {
		return favoriteId;
	}

	/**
	 * @param favoriteId the favoriteId to set
	 */
	public void setFavoriteId(int favoriteId) {
		this.favoriteId = favoriteId;
	}
	
	
	

}
/**
 * Class for encapsulation of overlay position on screen
 * @author winifredo
 *
 */
class Label implements ScreenObj {
	float x, y, w, h;
	float width, height;
	ScreenObj obj;

	public void prepare(ScreenObj drawObj) {
		obj = drawObj;
		w = obj.getWidth();
		h = obj.getHeight();

		x = w / 2;
		y = 0;

		width = w * 2;
		height = h * 2;
	}

	public void paint(PaintScreen dw) {
		dw.paintObj(obj, x, y, 0, 1);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	
	
}



