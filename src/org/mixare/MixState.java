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

import org.mixare.render.Matrix;
import org.mixare.render.MixVector;

import edu.fsu.cs.argame.marker.Marker;

public class MixState{
	public final static int NOT_STARTED = 0; 
	public final static int PROCESSING = 1; 
	public final static int READY = 2; 
	public final static int DONE = 3; 

	public static int nextLStatus = MixState.NOT_STARTED;
	public String downloadId;
	
	private float curBearing;
	private float curPitch;


	boolean detailsView = false;

	/**
	 * 
	 * @return current bearing of the device
	 */
	public float getCurBearing() {
		return curBearing;
	}
	
	/**
	 * 
	 * @return current pitch of the device
	 */
	public float getCurPitch() {
		return curPitch;
	}

	/**
	 * computation of current bearing and pitch of the device
	 * @param rotationM
	 */
	public void calcPitchBearing(Matrix rotationM) {
	
		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		
		curBearing = (int) (getAngle(0, 0, looking.x, looking.z)  + 360 ) % 360 ;
		
		switch (MixView.windowOrientation) {
			case MixView.PORTRAIT:
				//if (MixView.orientation[1] > -20)
				if (MixView.orientation[1] > -20)
					curBearing = MixView.orientation[0];
				else
					curBearing -= 90;
				break;
			case MixView.LANDSCAPE:
				
				break;
			default:
				curBearing -= 180;
				break;
		}
		
		
		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		this.curPitch = -getAngle(0, 0, looking.y, looking.z);

		
	}

	/**
	 * showing details of selected POI
	 * @param ctx
	 * @param marker
	 * @return
	 */
	public boolean handleEvent(MixContext ctx, Marker marker) {
		MixView.ctx.getMixView().showPoiDetails(marker);
		return true;
	}
	
	/**
	 * 
	 * @param center_x
	 * @param center_y
	 * @param post_x
	 * @param post_y
	 * @return
	 */
	private float getAngle(float center_x, float center_y, float post_x,
			float post_y) {
		float tmpv_x = post_x - center_x;
		float tmpv_y = post_y - center_y;
		float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
		float cos = tmpv_x / d;
		float angle = (float) Math.toDegrees(Math.acos(cos));

		angle = (tmpv_y < 0) ? angle * -1 : angle;

		return angle;
	}
	
}
