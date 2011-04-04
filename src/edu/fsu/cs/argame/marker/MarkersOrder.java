package edu.fsu.cs.argame.marker;
/**
 * Compares the markers. The closer they are the higher in the stack.
 * 
 * @author daniele
 * 
 */
public class MarkersOrder implements java.util.Comparator<Object> {
	public int compare(Object left, Object right) {
		Marker leftPm = (Marker) left;
		Marker rightPm = (Marker) right;

		return Double.compare(leftPm.mGeoLoc.getDistance(), rightPm.mGeoLoc
				.getDistance());
	}
}