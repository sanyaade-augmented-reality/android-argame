package net.peterd.zombierun.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.fsu.cs.argame.marker.DestinationMarker;
import edu.fsu.cs.argame.marker.PlayerMarker;
import edu.fsu.cs.argame.marker.ZombieMarker;

import android.os.Bundle;
import net.peterd.zombierun.util.Log;

import net.peterd.zombierun.service.GameEventBroadcaster;

public class GameState {

	private final List<ZombieMarker> zombies = new ArrayList<ZombieMarker>();
	private final List<PlayerMarker> players = new ArrayList<PlayerMarker>();
	private int indexOfThisDevicePlayer;
	private DestinationMarker destination;

	private static final String zombieHordeBundleKey = "net.peterd.zombierun.service.GameState.ZombieHorde";

	public GameState() {
		// Nothing to see here.
	}

	public GameState(DestinationMarker destination) {
		setDestination(destination);
	}

	public DestinationMarker getDestination() {
		return destination;
	}

	public void setDestination(DestinationMarker destination) {
		this.destination = destination;
	}

	public List<ZombieMarker> getZombies() {
		return zombies;
	}

	public List<PlayerMarker> getPlayers() {
		return players;
	}

	public void setThisDevicePlayer(PlayerMarker player) {
		indexOfThisDevicePlayer = players.indexOf(player);
		assert indexOfThisDevicePlayer > 0;
	}

	public PlayerMarker getThisDevicePlayer() {
		return players.get(indexOfThisDevicePlayer);
	}

	public void toBundle(Bundle state) {
		destination.toBundle(state);
		state.putString(zombieHordeBundleKey,
				ZombieMarker.ZombieListSerializer.toString(zombies));
	}

	public void fromBundle(Bundle state,
			GameEventBroadcaster gameEventBroadcaster) {
		destination = DestinationMarker.fromBundle(state);

		List<ZombieMarker> zombies = this.zombies;
		zombies.clear();
		zombies.addAll(ZombieMarker.ZombieListSerializer.fromString(
				state.getString(zombieHordeBundleKey), players,
				gameEventBroadcaster));
	}

	public void AdvanceZombies(long deltaTimeMs) {
		Log.d("ZombieRun.GameState", "Advancing Zombies.");
		List<ZombieMarker> zombies = getZombies();
		// don't allocate a list iterator object.
		for (int i = 0; i < zombies.size(); ++i) {
			zombies.get(i).advance(deltaTimeMs, TimeUnit.MILLISECONDS);
		}
	}
}
