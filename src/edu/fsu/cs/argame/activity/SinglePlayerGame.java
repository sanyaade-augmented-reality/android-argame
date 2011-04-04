package edu.fsu.cs.argame.activity;

import edu.fsu.cs.argame.marker.DestinationMarker;
import net.peterd.zombierun.service.GameService;

public class SinglePlayerGame extends Game {

  @Override
  protected void createGame(GameService service, DestinationMarker destination) {
    service.createSinglePlayerGame(service.getHardwareManager().getLastKnownLocation(),
        destination,
        gameSettings);
  }
}
