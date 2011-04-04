package net.peterd.zombierun.constants;

import android.graphics.Bitmap;
import edu.fsu.cs.argame.R;

/**
 * The class container of all the bitmap used for a marker.
 */
public class ImageBundle {
	
	/** The default icon used in ARMode **/
	private Bitmap icon;
	
	/** The icon used when a marker is clicked in ARMode**/
	private Bitmap focusIcon;
	
	/** The icon used in Tab mode (the listings of marker) **/
	private Bitmap bigIcon;

	/** The icon used in Map Mode. It is basically a resized smaller version of the icon used in ARMode **/
	private Bitmap mapIcon;
	
	/** The icon used Map Mode when a marker is tapped **/
	private Bitmap mapFocusIcon;
	
	/**  @return the icon */
	public Bitmap getIcon() {
		return icon;
	}
	
	/** @param icon the icon to set */
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	/** @return the focusIcon */
	public Bitmap getFocusIcon() {
		return focusIcon;
	}
	
	/** @param focusIcon the focusIcon to set */
	public void setFocusIcon(Bitmap focusIcon) {
		this.focusIcon = focusIcon;
	}
	
	/** @return the bigIcon */
	public Bitmap getBigIcon() {
		return bigIcon;
	}
	
	/** @param bigIcon the bigIcon to set */
	public void setBigIcon(Bitmap bigIcon) {
		this.bigIcon = bigIcon;
	}
	
	/**  @return the mapFocusIcon  */
	public Bitmap getMapFocusIcon() {
		return mapFocusIcon;
	}
	
	/** @param mapFocusIcon the mapFocusIcon to set */
	public void setMapFocusIcon(Bitmap mapFocusIcon) {
		this.mapFocusIcon = mapFocusIcon;
	}
	
	/** @return the mapIcon */
	public Bitmap getMapIcon() {
		return mapIcon;
	}
	
	/**  @param mapIcon the mapIcon to set */
	public void setMapIcon(Bitmap mapIcon) {
		this.mapIcon = mapIcon;
	}

}
