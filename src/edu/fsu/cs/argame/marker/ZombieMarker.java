package edu.fsu.cs.argame.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.mixare.reality.PhysicalPlace;

import net.peterd.zombierun.constants.Constants;
import net.peterd.zombierun.game.GameEvent;
import net.peterd.zombierun.service.GameEventBroadcaster;
import net.peterd.zombierun.service.GameEventListener;
import net.peterd.zombierun.util.GeoPointUtil;

import net.peterd.zombierun.util.Log;

public class ZombieMarker extends Marker implements GameEventListener {

  private final int id;
  private double lat;
  private double lon;
  private final List<PlayerMarker> players;
  private final double zombieSpeedMetersPerSecond;
  private final GameEventBroadcaster gameEventBroadcaster;
  private boolean isNoticingPlayer = false;
  private boolean isNearPlayer = false;
  private PlayerMarker playerZombieIsChasing;
  
  private PlayerMarker nearestPlayer = null;
  private double distanceToNearestPlayer = 0;
  
  public ZombieMarker(int id,
      PhysicalPlace startingLocation,
      List<PlayerMarker> players,
      PlayerMarker playerZombieIsChasing,
      double zombieSpeedMetersPerSecond,
      GameEventBroadcaster gameEventBroadcaster) {
	  this.id = id;
    lat = startingLocation.getLatitude();
    lon = startingLocation.getLongitude();
    this.players = players;
    this.playerZombieIsChasing = playerZombieIsChasing;
    this.zombieSpeedMetersPerSecond = zombieSpeedMetersPerSecond;
    this.gameEventBroadcaster = gameEventBroadcaster;
  }
  
  public ZombieMarker(int id,
      PhysicalPlace startingLocation,
      List<PlayerMarker> players,
      double zombieSpeedMetersPerSecond,
      GameEventBroadcaster gameEventBroadcaster) {
    this(id, startingLocation, players, null, zombieSpeedMetersPerSecond, gameEventBroadcaster);
  }

  public double getLatitude() {
    return lat;
  }
  
  public int getLatitudeE6() {
    return (int) (lat * 1e6);
  }
  
  public double getLongitude() {
    return lon;
  }
  
  public int getLongitudeE6() {
    return (int) (lon * 1e6);
  }

  public boolean isNoticingPlayer() {
    return isNoticingPlayer;
  }
  
  public void advance(long time, TimeUnit timeUnit) {
    computeNearestPlayer();
    
    // If we get an interval that's too long, it'll screw up the distance that the zombies move,
    // which can make them completely overshoot or something.
    long intervalMs = Math.min(timeUnit.toMillis(time), Constants.gameUpdateDelayMs);
    
    double movementDistanceMeters = zombieSpeedMetersPerSecond * (((float) intervalMs) / 1000);

    // hasNoticedPlayer sets playerZombieIsChasing and distanceToPlayerZombieIsChasing.
    if (hasNoticedPlayer()) {
      PlayerMarker playerZombieIsChasing = this.playerZombieIsChasing;
      movementDistanceMeters =
          Math.min(movementDistanceMeters, distanceToNearestPlayer);
      moveTowardPlayer(playerZombieIsChasing, movementDistanceMeters);

      // Setting isNoticingPlayer to true must happen after conditionallyVibrateOnPlayerNoticed, as
      // that method checks to see if it's being called while isNoticingPlayer is false to determine
      // whether or not this is the point at which the zombie switches from 'not noticed player' to
      // 'noticed player.'
      conditionallyVibrateOnPlayerNoticed();
      isNoticingPlayer = true;
    } else {
      meander(movementDistanceMeters);
      isNoticingPlayer = false;
    }
    
    conditionallyWarnZombieNearPlayer();
    
    PlayerMarker playerZombieIsChasing = this.playerZombieIsChasing;
    if (playerZombieIsChasing != null && hasCaughtPlayer(playerZombieIsChasing)) {
      gameEventBroadcaster.broadcastEvent(GameEvent.ZOMBIE_CATCH_PLAYER);
    }
  }

  private void computeNearestPlayer() {
    distanceToNearestPlayer = Double.MAX_VALUE;
    // Don't allocate a list iterator.
    for (int i = 0; i < players.size(); ++i) {
      PlayerMarker player = players.get(i);
      double distance = GeoPointUtil.distanceMeters(lat, 
          lon, 
          player.getLatitude(), 
          player.getLongitude());
      if (distance < distanceToNearestPlayer) {
        nearestPlayer = player;
        distanceToNearestPlayer = distance;
      }
    }
  }

  private void conditionallyWarnZombieNearPlayer() {
    PlayerMarker playerZombieIsChasing = this.playerZombieIsChasing;
    if (playerZombieIsChasing != null && isNearPlayer(playerZombieIsChasing)) {
      if (!isNearPlayer) {
        gameEventBroadcaster.broadcastEvent(GameEvent.ZOMBIE_NEAR_PLAYER);
      }
      isNearPlayer = true;
    } else {
      isNearPlayer = false;
    }
  }
  
  private void conditionallyVibrateOnPlayerNoticed() {
    if (!isNoticingPlayer) {
      gameEventBroadcaster.broadcastEvent(GameEvent.ZOMBIE_NOTICE_PLAYER);
    }
  }
  
  /**
   * Precondition: {@link #playerZombieIsChasing} must not be null.
   * @return
   */
  private boolean hasCaughtPlayer(PlayerMarker playerZombieIsChasing) {
    return distanceToNearestPlayer < Constants.zombieCatchPlayerDistanceMeters;
  }
  
  /**
   * Determines whether or not the zombie has noticed a player, and sets
   * {@link #playerZombieIsChasing} and {@link #distanceToPlayerZombieIsChasing} appropriately (or
   * null if the zombie is not noticing a player.
   * 
   * @return True if the zombie has noticed a player.  The player the zombie has noticed and the
   *    distance to that player will be stored in {@link #playerZombieIsChasing} and
   *    {@link #distanceToPlayerZombieIsChasing}.
   */
  private boolean hasNoticedPlayer() {
    if (distanceToNearestPlayer < Constants.zombieNoticePlayerDistanceMeters) {
      playerZombieIsChasing = nearestPlayer;
      return true;
    } else {
      playerZombieIsChasing = null;
      return false;
    }
  }
  
  private boolean isNearPlayer(PlayerMarker player) {
    return distanceToNearestPlayer < Constants.zombieNearPlayerDistanceMeters;
  }
  
  private void meander(double movementDistanceMeters) {
    // TODO: Give them a primary direction, not just random movements.
    // TODO: Make zombies cluster a little bit
    // TODO: Try to optimize out allocating this PhysicalPlace
    PhysicalPlace location = 
      GeoPointUtil.getGeoPointNear(lat, lon, movementDistanceMeters);
    lat = location.getLatitude();
    lon = location.getLongitude();
  }
  
  private void moveTowardPlayer(PlayerMarker player, double movementDistanceMeters) {
    if (Log.loggingEnabled()) {
      Log.d("ZombieRun.Zombie", "Moving towards player " + player.toString());
    }
    PhysicalPlace location = GeoPointUtil.geoPointTowardsTarget(lat, 
        lon, 
        player.getLatitude(), 
        player.getLongitude(),
        movementDistanceMeters);
    lat = location.getLatitude();
    lon = location.getLongitude();
  }
  
  public String toString() {
    int indexOfPlayerZombieIsChasing = -1;
    if (playerZombieIsChasing != null) {
      indexOfPlayerZombieIsChasing = players.indexOf(playerZombieIsChasing);
    }
    StringBuilder builder = new StringBuilder();
    builder.append(id);
    builder.append(":");
    builder.append(indexOfPlayerZombieIsChasing);
    builder.append(":");
    builder.append(PhysicalPlace.toString(lat, lon));
    builder.append(":");
    builder.append(zombieSpeedMetersPerSecond);
    return builder.toString();
  }
  
  public static ZombieMarker fromString(String stringEncodedZombie,
      List<PlayerMarker> players,
      GameEventBroadcaster gameEventBroadcaster) {
    String[] parts = stringEncodedZombie.split(":", 4);
    if (parts.length != 4) {
      Log.e("ZombieRun.Zombie", "Did not find 3 parts, which should have been the zombie id, " +
      		"index of the player the zombie is chasing (-1 for none), and a string-encoded " +
          "PhysicalPlace, in '" + stringEncodedZombie + "'.");
      return null;
    }
    
    String zombieIdStr = parts[0];
    int zombieId;
    try {
      zombieId = Integer.parseInt(zombieIdStr);
    } catch (NumberFormatException e) {
      Log.e("Zombie", "Could not parse integer zombie id from '" + zombieIdStr + "'.", e);
      return null;
    }
    
    String indexOfPlayerZombieIsChasingStr = parts[1];
    PlayerMarker playerZombieIsChasing = null;
    try {
      int indexOfPlayerZombieIsChasing = Integer.parseInt(indexOfPlayerZombieIsChasingStr);
      if (indexOfPlayerZombieIsChasing >= 0 && indexOfPlayerZombieIsChasing < players.size()) {
        playerZombieIsChasing = players.get(indexOfPlayerZombieIsChasing);
      }
    } catch (NumberFormatException e) {
      Log.e("Zombie", "Could not parse integer player index from '" + 
          indexOfPlayerZombieIsChasingStr + "' to determine which player this zombie is " +
          "chasing.", e);
      return null;
    }
    
    String fpgpString = parts[2];
    PhysicalPlace fpgp = PhysicalPlace.fromString(fpgpString);
    if (fpgp == null) {
      Log.e("Zombie", "Could not parse zombie position PhysicalPlace from encoded " +
          "string '" + fpgpString + "'.");
      return null;
    }
    
    String zombieSpeedMetersPerSecondString = parts[3];
    double zombieSpeedMetersPerSecond = 0;
    try {
      zombieSpeedMetersPerSecond = Double.parseDouble(zombieSpeedMetersPerSecondString);
    } catch (NumberFormatException e) {
      Log.e("ZombieRun.Zombie", "Could not parse zombie speed from string '" +
          zombieSpeedMetersPerSecondString + "'.");
      return null;
    }
    
    ZombieMarker zombie =
        new ZombieMarker(zombieId,
            fpgp,
            players,
            playerZombieIsChasing,
            zombieSpeedMetersPerSecond,
            gameEventBroadcaster);
    return zombie;
  }
  
  public static class ZombieListSerializer {
    
    public static String toString(List<ZombieMarker> zombies) {
      StringBuilder builder = new StringBuilder();
      for (ZombieMarker zombie : zombies) {
        builder.append(zombie.toString());
        builder.append("\n");
      }
      return builder.toString();
    }

    public static List<ZombieMarker> fromString(
        String encodedString,
        List<PlayerMarker> players,
        GameEventBroadcaster gameEventBroadcaster) {
      List<ZombieMarker> zombies = new ArrayList<ZombieMarker>();
      String[] lines = encodedString.split("\n");
      for (int i = 0; i < lines.length; ++i) {
        ZombieMarker zombie =
            ZombieMarker.fromString(lines[i],
                players,
                gameEventBroadcaster);
        if (zombie != null) {
          zombies.add(zombie);
        }
      }
      return zombies;
    }
  }

  public void receiveEvent(GameEvent event) {
    
  }

public int getMaxObjects() {
	// TODO Auto-generated method stub
	return 0;
}
}
