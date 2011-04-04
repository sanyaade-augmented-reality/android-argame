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

package edu.fsu.cs.argame;

import java.util.ArrayList;

import org.mixare.MixView;
import org.mixare.gui.PaintScreen;
import org.mixare.gui.RadarPoints;

import edu.fsu.cs.argame.R;
import edu.fsu.cs.argame.marker.Marker;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * The view that renders the compass on the upper right side of the screen
 */
public class CompassView extends View {

	public final static int GALAXY_S = 0;
	public final static int HERO_MOTOROI = 1;
	public static int DEVICE = GALAXY_S;

	protected PaintScreen canvas = null;
	private RadarPoints radarPoints = new RadarPoints();
	private int roll = 0;
		
	public CompassView(Context context) {
		super(context);
		canvas = new PaintScreen();
		radarPoints.view = this;
	}

	@Override
	protected void onDraw(Canvas main) {
		if (canvas.getCanvas() == null) {
			canvas.setCanvas(main);
			canvas.setWidth(main.getWidth());
			canvas.setHeight(main.getHeight());
		}
		
		ArrayList<Marker> markers = MixView.arMarkers;
		if (markers != null) {
			int width = main.getWidth();

			canvas.getCanvas().save();
			
			if (DEVICE == GALAXY_S) {
				canvas.getCanvas().scale((float).7, (float).7);
				canvas.getCanvas().translate((MixView.windowOrientation==MixView.PORTRAIT)?200:340, 0);
			}
			
			/** Draws the inactive compass icon when there is no location available **/
			if (MixView.ctx.curLoc == null)
				canvas.paintBitmap(MixView.radarBitmap[MixView.DEFAULT], (width-MixView.radarBitmap[MixView.DEFAULT].getWidth()), 0);
			else {
				/** Renders the active compass icon and the radar when a location is already available **/
				canvas.paintBitmap(MixView.radarBitmap[MixView.POI_LOADED], (width-MixView.radarBitmap[MixView.POI_LOADED].getWidth()), 0);
				canvas.paintBitmap(MixView.radarBitmap[MixView.COMPASS_RADAR],
						(width-MixView.radarBitmap[MixView.POI_LOADED].getWidth()),
						0,roll  = (roll+10) % 360);
			}
			
			if (markers.size() != 0){
				
				if (DEVICE == GALAXY_S)
					canvas.paintObj(radarPoints, width-(MixView.radarBitmap[MixView.DEFAULT].getWidth())+30, 30, -MixView.ctx.getDataView().getState().getCurBearing(), 1);
				else
					canvas.paintObj(radarPoints, width-(MixView.radarBitmap[MixView.DEFAULT].getWidth())+6, 6, -MixView.ctx.getDataView().getState().getCurBearing(), 1);
				
				try {
					/** Draws the arrow depending on the angle from the current camera focus to the current location of the target poi**/
					Marker ma = MixView.pressedMarker;
					if ((ma != null) && (Math.round(ma.mGeoLoc.getDistance()) != 0)) {
						float targetAngle = (float)(ma.mGeoLoc.getBearing() - (MixView.ctx.getDataView().getState().getCurBearing()));
						
						MixView.ctx.getMixView().setArrowVisibility(targetAngle, ma);
						
						float xComponent = width-MixView.radarBitmap[MixView.POI_LOADED].getWidth();
						xComponent += (MixView.radarBitmap[MixView.POI_LOADED].getWidth()/2);
						xComponent -= MixView.radarBitmap[MixView.COMPASS_ARROW].getWidth()/2;
						
						float yComponent = (MixView.radarBitmap[MixView.POI_LOADED].getHeight() - MixView.radarBitmap[MixView.COMPASS_ARROW].getHeight())/2;
						
						canvas.paintBitmap(MixView.radarBitmap[MixView.COMPASS_ARROW], xComponent, yComponent, targetAngle);
					}
					
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			
			if (DEVICE == GALAXY_S)
				canvas.getCanvas().restore();
		}
		 
		invalidate();
	}

}
