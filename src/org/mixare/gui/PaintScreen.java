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

import org.mixare.MixView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
/**
 * Class responsible for drawing overlay for AR
 * @author winifredo
 *
 */
/**
 * Contains all canvas operations
 */
public class PaintScreen {
	public static final int BLUE = 0;
	public static final int RED = 1;
	
	public static final int TARGET = 0;
	public static final int DEFAULT = 1;
	
	private final int ALPHA = 25;
	Canvas canvas;
	private int width;
	private int height;
	Paint paint = new Paint();
	Paint transparent = new Paint();

	public PaintScreen() {
		paint.setTextSize(16);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		
		transparent.setColor(Color.BLUE);
		transparent.setAlpha(ALPHA);
		transparent.setAntiAlias(true);
	}

	/** @return the canvas */
	public Canvas getCanvas() {
		return canvas;
	}

	/** @param canvas the canvas to set*/
	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}
	
	/** @param width the width to set*/
	public void setWidth(int width) {
		this.width = width;
	}
	
	/** @param height the height to set*/
	public void setHeight(int height) {
		this.height = height;
	}
	
	/** @return the width */
	public int getWidth() {
		return width;
	}
	
	/** @return the height */
	public int getHeight() {
		return height;
	}

	/**
	 * Determines the paint style (fill or stroke)
	 * @param fill true to set style to fill, false to set it to stroke 
	 */
	public void setFill(boolean fill) {
		if (fill)
			paint.setStyle(Paint.Style.FILL);
		else
			paint.setStyle(Paint.Style.STROKE);
	}
	
	/**
	 * Sets paint color
	 *  @param c the color to set
	**/
	public void setColor(int c) {
		paint.setColor(c);
	}

	/**
	 * Sets paint stroke width
	 *  @param w the width to set
	**/
	public void setStrokeWidth(float w) {
		paint.setStrokeWidth(w);
	}

	/**
	 * Draws a line using the specified coordinates
	 * @param x1 The x-coordinate of the start point of the line
	 * @param y1 The y-coordinate of the start point of the line
	 * @param x2 The x-coordinate of the end point of the line
	 * @param y2 The y-coordinate of the end point of the line
	 */
	public void paintLine(float x1, float y1, float x2, float y2) {
		canvas.drawLine(x1, y1, x2, y2, paint);
	}
	
	/**
	 *  Draws a rectangle using the specified coordinates and dimension
	 * @param x The left side of the rectangle to be drawn
	 * @param y The top side of the rectangle to be drawn
	 * @param width The width of the rectangle to be drawn
	 * @param height The height of the rectangle to be drawn
	 */
	public void paintRect(float x, float y, float width, float height) {
		canvas.drawRect(x, y, x + width, y + height, paint);
	}
	
	/**
	 * Draws a rectangle using the specified coordinates
	 * @param left The left side of the rectangle to be drawn
	 * @param top The top side of the rectangle to be drawn
	 * @param right The right side of the rectangle to be drawn
	 * @param bottom The bottom side of the rectangle to be drawn
	 */
	public void paintRectTest(float left, float top, float right, float bottom) {
		canvas.drawRect(left, top, right, bottom, paint);
	}
	
	/**
	 * Draws a rectangle using the specified bounds
	 * @param bounds the bounds of the rectangle to be drawn
	 */
	public void paintRect(Rect bounds) {
		canvas.drawRect(bounds, paint);
	}
	
	/**
	 * Draws a rectangle with a semi-transparent paint
	 * @param x The left side of the rectangle to be drawn
	 * @param y The top side of the rectangle to be drawn
	 * @param width The width of the rectangle to be drawn
	 * @param height The height of the rectangle to be drawn
	 * @param color The color of the rectangle
	 */
	public void paintTransparentRect(float x, float y, float width, float height, int color) {
		transparent.setColor((color == BLUE)?Color.parseColor("#4b4a79"):Color.RED);
		canvas.drawRect(x, y, x + width, y + height, transparent);
	}

	/**
	 * Draws a circle based on the specified bounds
	 * @param x The x-coordinate of the center of the circle
	 * @param y The y-coordinate of the center of the circle
	 * @param radius The radius of the circle
	 */
	public void paintCircle(float x, float y, float radius) {
		canvas.drawCircle(x, y, radius, paint);
	}

	/**
	 * Draws a text with the origin at (x,y)
	 * @param x The x-coordinate of the origin of the text
	 * @param y The y-coordinate of the origin of the text
	 * @param text The string to be drawn
	 */
	public void paintText(float x, float y, String text) {
		canvas.drawText(text, x, y, paint);
	}

	/** Paints the object using the specified coordinates, rotation, and scaling */
	public void paintCompass(ScreenObj obj, float x, float y, float rotation, float scale) {
		canvas.save();
		canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2);
		canvas.rotate(rotation);
		canvas.scale(scale, scale);
		canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
		obj.paint(this);
		canvas.restore();
	}
	
	/** Paints the object using the specified coordinates, rotation, and scaling */
	public void paintObj(ScreenObj obj, float x, float y, float rotation, float scale) {
		canvas.save();
		canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2);
		canvas.scale(scale, scale);
		canvas.rotate(rotation);
		canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
		obj.paint(this);
		canvas.restore();
	}
	
	/** Paints the object using the specified coordinates, rotation, and scaling */
	public void paintMarker(ScreenObj obj, float x, float y, float rotation,
			float scale) {
		canvas.save();
		canvas.translate(x+(obj.getWidth() / 2), y+(obj.getHeight() / 2));
		canvas.scale(scale, scale);
		canvas.rotate(rotation);
		canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
		obj.paint(this);
		canvas.restore();
	}
	
	/**
	 * Paints the Bitmap using the specified coordinates
	 * @param icon The Bitmap object to be drawn
	 * @param x The left side of the bitmap to be drawn
	 * @param y The top side of the bitmap to be drawn
	 */
	public void paintBitmap(Bitmap icon, float x, float y) {
		canvas.drawBitmap(icon, x, y, paint);
	}
	
	/** Paints the Bitmap using the specified coordinates and rotation **/
	public void paintBitmap(Bitmap icon, float x, float y, float rotation) {
		canvas.save();
		canvas.rotate(rotation, x+(icon.getWidth()/2), y+(icon.getHeight()/2));
		canvas.drawBitmap(icon, x, y, paint);
		canvas.restore();
	}
	
	/**
	 * Draws the distance box bitmap specified by type
	 * @param x The left side of the bitmap to be drawn
	 * @param y The top side of the bitmap to be drawn
	 * @param type 0 if target, 1 if default
	 */
	public void paintDistanceContainer(float x, float y, int type) {
		canvas.drawBitmap(MixView.poiBg[type], x, y, paint);
	}

	/**
	 * Measures the width of the text
	 * @param txt The text to be measured
	 * @return The width of the text
	 */
	public float getTextWidth(String txt) {
		return paint.measureText(txt);
	}

	/** Return the distance above (negative) the baseline (ascent) based on the current typeface and text size.**/
	public float getTextAsc() {
		return -paint.ascent();
	}

	/** Return the distance below (positive) the baseline (descent) based on the current typeface and text size.**/
	public float getTextDesc() {
		return paint.descent();
	}

	/** @return 0 */
	public float getTextLead() {
		return 0;
	}

	/**
	 * Sets the font size of the text to be used
	 * @param size the text's size
	 */
	public void setFontSize(float size) {
		paint.setTextSize(size);
	}
	
}