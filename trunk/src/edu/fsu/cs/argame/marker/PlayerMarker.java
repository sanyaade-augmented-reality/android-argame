package edu.fsu.cs.argame.marker;

import java.util.ArrayList;
import java.util.List;

import org.mixare.reality.PhysicalPlace;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import net.peterd.zombierun.util.Log;

import net.peterd.zombierun.constants.Constants;
import net.peterd.zombierun.game.GameEvent;
import net.peterd.zombierun.service.GameEventBroadcaster;
import net.peterd.zombierun.service.GameEventListener;
import net.peterd.zombierun.util.GeoPointUtil;

public class PlayerMarker extends Marker implements LocationListener, GameEventListener {

  private double lat;
  private double lon;
  private final DestinationMarker destination;
  private final int playerId;
  private final GameEventBroadcaster gameEventBroadcaster;
  
  /**
   * Construct a player.
   * 
   * @param destination The game's destination.
   * @param playerId The player's id, 0-indexed.
   * @param location The player's location, possibly null.
   * @param onReachDestinationRunnable The {@link Runnable} that should be run when the player
   *    reaches the destination.
   */
  public PlayerMarker(DestinationMarker destination,
      int playerId,
      PhysicalPlace location,
      GameEventBroadcaster gameEventBroadcaster) {
    this.destination = destination;
    this.playerId = playerId;
    if (location != null) {
      this.lat = location.getLatitude();
      this.lon = location.getLongitude();
    }
    this.gameEventBroadcaster = gameEventBroadcaster;
  }
  
  /**
   * Get the player's current location.
   * 
   * Deprecated, please use getLatitude and getLongitude directly to reduce the
   * number of PhysicalPlace allocations.
   * 
   * @return The player's current location, possibly null.
   */
  @Deprecated
  public PhysicalPlace getLocation() {
    return new PhysicalPlace(lat, lon);
  }
  
  public double getLatitude() {
    return lat;
  }
  
  public double getLongitude() {
    return lon;
  }
  
  /**
   * Serialize the player to a string.
   */
  @Override
  public String toString() {
    if (lat != 0 && lon != 0) {
      return playerId + ":" + PhysicalPlace.toString(lat, lon);
    } else {
      return Integer.toString(playerId);
    }
  }

  /**
   * @throws IllegalArgumentException if there are any parsing errors. 
   */
  public static PlayerMarker fromString(String serializedPlayer,
      DestinationMarker destination,
      GameEventBroadcaster gameEventBroadcaster) {
    String[] splits = serializedPlayer.split(":", 2);
    String playerIdString;
    String locationString;
    if (splits.length == 2) {
      playerIdString = splits[0];
      locationString = splits[1];
    } else if (splits.length == 1) {
      playerIdString = splits[0];
      locationString = null;
    } else {
      throw new IllegalArgumentException(
          "Could not split serialized player into one or two parts.");
    }
    
    int playerId;
    try {
      playerId = Integer.parseInt(playerIdString);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Could not parse player id from string '" + serializedPlayer + "'.", e);
    }
    
    PhysicalPlace location = null;
    if (locationString != null) {
      // May return null.  That's okay, by the semantics of the Player constructor.
      location = PhysicalPlace.fromString(locationString);
    }
    
    return new PlayerMarker(destination, playerId, location, gameEventBroadcaster);
  }

  /**
   * Deprecated in favor of {@link #setLocation(double, double)}.
   * @param location
   */
  @Deprecated
  public void setLocation(PhysicalPlace location) {
    if (location != null) {
      setLocation(location.getLatitude(), location.getLongitude());
    } else {
      Log.w("ZombieRun.Player", "Player location attempted to be set to null.");
    }
  }
  
  public void setLocation(double latitude, double longitude) {
    lat = latitude;
    lon = longitude;
    if (Log.loggingEnabled()) {
      Log.d("ZombieRun.Player", "Player location updated to " +
          PhysicalPlace.toString(lat, lon));
    }
    if (GeoPointUtil.distanceMeters(lat,
            lon,
            destination.getLocation().getLatitude(), 
            destination.getLocation().getLongitude()) <
                Constants.reachDestinationTestDistanceMeters) {
      gameEventBroadcaster.broadcastEvent(GameEvent.PLAYER_REACHES_DESTINATION);
    }
  }
  
  public void onLocationChanged(Location location) {
    PhysicalPlace point =
        new PhysicalPlace(GeoPointUtil.fromLocation(location));
    setLocation(point.getLatitude(), point.getLongitude());
  }

  public void onProviderDisabled(String provider) { }
  public void onProviderEnabled(String provider) { }
  public void onStatusChanged(String provider, int status, Bundle extras) { }

  public void receiveEvent(GameEvent event) {
    // Currently a no-op.
  }
  
  public static class PlayerListSerializer {
    
    public static String toString(List<PlayerMarker> players) {
      StringBuilder builder = new StringBuilder();
      for (PlayerMarker player : players) {
        builder.append(player.toString());
        builder.append("\n");
      }
      return builder.toString();
    }

    public static List<PlayerMarker> fromString(
        String encodedString,
        DestinationMarker destinationReference,
        GameEventBroadcaster gameEventBroadcaster) {
      List<PlayerMarker> players = new ArrayList<PlayerMarker>();
      String[] lines = encodedString.split("\n");
      for (int i = 0; i < lines.length; ++i) {
        PlayerMarker player = PlayerMarker.fromString(lines[i], destinationReference, gameEventBroadcaster);
        if (player != null) {
          players.add(player);
        }
      }
      return players;
    }
  }

public int getMaxObjects() {
	// TODO Auto-generated method stub
	return 0;
}
}
