package edu.fsu.cs.argame.marker;

import java.util.ArrayList;

import org.mixare.MixView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.fsu.cs.argame.R;

/**
 * The class that overlays the:
 * <ul>
 * <li>users currentlocation (this should be based on the location returned by
 * the locationListener)</li>
 * <li>locationList - the list of markers set</li>
 * <li>searchedLocation - the marker tapped by the user or searched from the
 * Search Activity</li>
 * </ul>
 * 
 */
public class MapOverlay extends Overlay {

    /**
     * the current location of the user. this should be updated every
     * locationchange
     **/
    public GeoPoint currentlocation;

    /** the marker tapped by the user or searched from the Search Activity **/
    public Marker searchedLocation;

    /** the list of markers set **/
    public ArrayList<Marker> locationList;

    private Projection projection;
    public float direction;

    /**
     * @param context
     * @param currentlocation
     *            geopoint containing the current location of the user
     */
    public MapOverlay(Context context, GeoPoint currentlocation) {
	this.currentlocation = currentlocation;
	locationList = new ArrayList<Marker>();
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	super.draw(canvas, mapView, shadow);

	if (currentlocation != null) {

	    projection = mapView.getProjection();
	    Point currPoint = new Point();
	    projection.toPixels(currentlocation, currPoint);

	    /** draws the radar */
	    canvas.save();
	    canvas.translate(currPoint.x, currPoint.y);
	    canvas.rotate(direction - 180);
	    try {
		canvas.drawBitmap(MixView.angle, -70, -70, null);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    canvas.restore();

	    /** draws the list of locations */
	    for (int i = 0; i < locationList.size(); i++) {
		try {
		    Point listPoint = new Point();
		    projection.toPixels(new GeoPoint((int) (locationList.get(i).mGeoLoc.getLatitude() * 1E6), (int) (locationList.get(i).mGeoLoc.getLongitude() * 1E6)), listPoint);
		    canvas.drawBitmap(locationList.get(i).getMapIcon(), listPoint.x - 12, listPoint.y - 14, null);
		} catch (Exception e) {
		}
	    }

	    /** draws the searched location */
	    if (searchedLocation != null) {
		try {
		    Point searchedPoint = new Point();
		    projection.toPixels(new GeoPoint((int) (searchedLocation.mGeoLoc.getLatitude() * 1E6), (int) (searchedLocation.mGeoLoc.getLongitude() * 1E6)), searchedPoint);
		    canvas.drawBitmap(searchedLocation.getMapFocusIcon(), searchedPoint.x - 24, searchedPoint.y - 26, null);
		} catch (Exception e) {
		}
	    }

	}

	return true;
    }

    /**
     * Adds a new marker to the existing list of markers
     * 
     * @param newLocation
     *            marker to be added in the list of markers
     */
    public void setLocationList(Marker newLocation) {
	this.locationList.add(newLocation);
    }

    /**
     * Sets the searchedLocation marker to be drawn in the overlay, this should
     * be equal to the pressed marker in MixView
     */
    public void setSearchedLocation() {
	if (MixView.pressedMarker != null)
	    searchedLocation = MixView.pressedMarker;
	else
	    searchedLocation = null;
    }

    /**
     * Clears the locationList and sets searchedLocation to null
     */
    public void clearLocationList() {
	locationList.clear();
	searchedLocation = null;
    }

    /**
     * Checks if the tapped location is inside the bounds of a marker
     * 
     * @param x
     *            the horizontal position of the tap event
     * @param y
     *            the vertical position of the tap event
     * @return the marker with bounds inside the tap location, null if no marker
     *         is inside the tapped bounds
     */
    public Marker onTap(float x, float y) {
	GeoPoint tappedLocLeft = projection.fromPixels((int) x - 30, (int) y - 55);
	GeoPoint tappedLocRight = projection.fromPixels((int) x + 30, (int) y - 15);

	if (MixView.pressedMarker != null && MixView.pressedMarker.mGeoLoc.getLatitude() > (tappedLocRight.getLatitudeE6() / 1E6)
		&& MixView.pressedMarker.mGeoLoc.getLongitude() < (tappedLocRight.getLongitudeE6() / 1E6) && MixView.pressedMarker.mGeoLoc.getLatitude() < (tappedLocLeft.getLatitudeE6() / 1E6)
		&& MixView.pressedMarker.mGeoLoc.getLongitude() > (tappedLocLeft.getLongitudeE6() / 1E6)) {
	    searchedLocation = MixView.pressedMarker;
	    return MixView.pressedMarker;
	}

	for (int i = locationList.size() - 1; i >= 0; i--) {
	    if (locationList.get(i).mGeoLoc.getLatitude() > (tappedLocRight.getLatitudeE6() / 1E6) && locationList.get(i).mGeoLoc.getLongitude() < (tappedLocRight.getLongitudeE6() / 1E6)
		    && locationList.get(i).mGeoLoc.getLatitude() < (tappedLocLeft.getLatitudeE6() / 1E6) && locationList.get(i).mGeoLoc.getLongitude() > (tappedLocLeft.getLongitudeE6() / 1E6)) {
		searchedLocation = locationList.get(i);
		return locationList.get(i);
	    }
	}

	return null;
    }

}
