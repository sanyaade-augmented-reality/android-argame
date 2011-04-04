package net.peterd.zombierun.util;

import org.mixare.reality.PhysicalPlace;

import net.peterd.zombierun.constants.Constants;

import com.google.android.maps.GeoPoint;

import android.location.Location;

public class GeoPointUtil {

  public static GeoPoint fromLocation(Location location) {
    return new GeoPoint((int) Math.round(location.getLatitude() * 1E6),
        (int) Math.round(location.getLongitude() * 1E6));
  }
  
  public static PhysicalPlace getGeoPointNear(double lat, double lon,
      double distanceMeters) {
    double targetLat = Math.random() - 0.5 + lat;
    double targetLon = Math.random() - 0.5 + lon;
    return geoPointTowardsTarget(lat, lon, targetLat, targetLon, distanceMeters);
  }
  
  public static PhysicalPlace getRandomDest(double lat, double lon) {
	    double targetLat = Math.random() - 0.5 + lat;
	    double targetLon = Math.random() - 0.5 + lon;
	    double distanceMeters = Math.random() * 20.0;
	    return geoPointTowardsTarget(lat, lon, targetLat, targetLon, distanceMeters);
  }
  

  public static PhysicalPlace geoPointTowardsTarget(
      double oLat,
      double oLon,
      double dLat,
      double dLon,
      double distanceMeters) {
    double diffLat = dLat - oLat;
    double diffLon = dLon - oLon;
    
    double diffMagnitudeMeters = distanceMeters(oLat, oLon, dLat, dLon);
    double deltaLat = diffLat * (distanceMeters / diffMagnitudeMeters);
    double deltaLon = diffLon * (distanceMeters / diffMagnitudeMeters);
    
    return new PhysicalPlace(oLat + deltaLat, oLon + deltaLon);
  }
  
  /**
   * A version of distanceMeters that can be used without allocating additional
   * PhysicalPlace objects.
   */
  public static double distanceMeters(double aLat, double aLon, double bLat, double bLon) {
    // Haversine formula, from http://mathforum.org/library/drmath/view/51879.html
    double dlon = bLon - aLon;
    double dlat = bLat - aLat;
    double a = Math.pow(Math.sin(Math.toRadians(dlat/2)), 2) +
        Math.cos(Math.toRadians(aLat)) *
        Math.cos(Math.toRadians(bLat)) *
        Math.pow(Math.sin(Math.toRadians(dlon / 2)), 2);
    double greatCircleDistance = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    return Constants.radiusOfEarthMeters * greatCircleDistance;
  }
}
