package net.peterd.zombierun.entity;

import org.mixare.reality.PhysicalPlace;

import android.os.Bundle;
import net.peterd.zombierun.util.NotImplementedException;

public class Destination {

  private final PhysicalPlace location;
  
  public static final String DESTINATION_BUNDLE_KEY = "net.peterd.zombierun.entity.Destination";
  
  public Destination(PhysicalPlace location) {
    this.location = location;
  }
  
  public PhysicalPlace getLocation() {
    return location;
  }
  
  public String toString() {
    // TODO: remove this method once we're sure it's not being used.
    throw new NotImplementedException();
  }
  
  public static Destination fromString(String string) {
    PhysicalPlace fpgp =
        PhysicalPlace.fromString(string);
    return fpgp == null ? null : new Destination(fpgp);
  }
  
  public static Destination fromBundle(Bundle bundle) {
    if (!bundle.containsKey(DESTINATION_BUNDLE_KEY)) {
      return null;
    }
    PhysicalPlace fpgp =
        PhysicalPlace.fromString(bundle.getString(DESTINATION_BUNDLE_KEY));
    return fpgp == null ? null : new Destination(fpgp);
  }

  public void toBundle(Bundle bundle) {
    bundle.putString(DESTINATION_BUNDLE_KEY, location.toString());
  }
}
