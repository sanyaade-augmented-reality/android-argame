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
package org.mixare.render;

/** 
 * This matrix corresponds to the radar point location of a marker
 * @author daniele */
public class MixVector {
	public float x;
	public float y;
	public float z;

	public MixVector() {
		this(0, 0, 0);
	}

	public MixVector(MixVector v) {
		this(v.x, v.y, v.z);
	}

	public MixVector(float v[]) {
		this(v[0], v[1], v[2]);
	}

	public MixVector(float x, float y, float z) {
		set(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		MixVector v = (MixVector) obj;
		return (v.x == x && v.y == y && v.z == z);
	}

	public boolean equals(float x, float y, float z) {
		return (this.x == x && this.y == y && this.z == z);
	}

	@Override
	public String toString() {
		return "<" + x + ", " + y + ", " + z + ">";
	}

	public void set(MixVector v) {
		set(v.x, v.y, v.z);
	}

	/** Sets the value of x, y, and z **/
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	/** Adds up to the current value of x, y, and z **/
	public void add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void add(MixVector v) {
		add(v.x, v.y, v.z);
	}

	public void sub(float x, float y, float z) {
		add(-x, -y, -z);
	}
	
	/** Subtract from the current value of x, y, and z **/
	public void sub(MixVector v) {
		add(-v.x, -v.y, -v.z);
	}

	/** Multiplies s to x, y, and z **/
	public void mult(float s) {
		x *= s;
		y *= s;
		z *= s;
	}

	/** Multiplies s to x, y, and z **/
	public void divide(float s) {
		x /= s;
		y /= s;
		z /= s;
	}

	/** @return the length */
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public void norm() {
		divide(length());
	}

	/** @return the dot product */
	public float dot(MixVector v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/** Computes the cross product of vector u and v **/
	public void cross(MixVector u, MixVector v) {
		float x = u.y * v.z - u.z * v.y;
		float y = u.z * v.x - u.x * v.z;
		float z = u.x * v.y - u.y * v.x;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/** Computes the product of matrix m and the MixVecto(x,y,z) **/
	public void prod(Matrix m) {
		float xTemp = m.a1 * x + m.a2 * y + m.a3 * z;
		float yTemp = m.b1 * x + m.b2 * y + m.b3 * z;
		float zTemp = m.c1 * x + m.c2 * y + m.c3 * z;

		x = xTemp;
		y = yTemp;
		z = zTemp;
	}
}
