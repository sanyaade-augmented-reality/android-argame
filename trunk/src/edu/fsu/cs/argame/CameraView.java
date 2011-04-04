package edu.fsu.cs.argame;

import org.mixare.MixView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import edu.fsu.cs.argame.R;

/**
 * The class that handles camera preview
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
	
	/**  The camera from which to get images. */
	Camera camera;
	
	/** SurfaceHolder to put images on. */
	SurfaceHolder previewHolder;

	/** 
	 * Constructor. 
	 * @param ctx The current application Context.
	 */
	public CameraView(Context ctx) {
		super(ctx);
		initPreviewHolder();
	}
	
	/**
	 * Initializes previewHolder 
	 */
	private void initPreviewHolder(){
		previewHolder = this.getHolder();
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder.addCallback(this);
		previewHolder.getSurface();
		setBackgroundColor(Color.TRANSPARENT);
	}
	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Stops camera preview and releases the camera object
	 */
	public void closeCamera() {
		if (camera != null) {
			 camera.stopPreview();
			 camera.release();
			 camera = null;
		}
	}
	
	/**
	 * Checks if the camera is null
	 * @return true if the camera is null, false otherwise
	 */
	public boolean isCameraNull() {
		if(camera == null) return true;
		return false;
	}
	
	public void dispatchDraw(Canvas c) {
		super.dispatchDraw(c);
	}

	/**
	 * Opens the camera.<br/>
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
		} catch (RuntimeException e) {}
		try {
			camera.setPreviewDisplay(previewHolder);
		} catch (Throwable t) {
		}
	}
	
	/**
	 * Changes camera orientation parameter when surfaceChanged.
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,int w, int h) {
		Camera.Parameters parameters = camera.getParameters();
		
		/** Sets the camera orientation depending on the phone's orientation **/
		switch (MixView.windowOrientation) {
			case MixView.PORTRAIT:
				parameters.set("orientation", "portrait");
				break;
			case MixView.LANDSCAPE:
				parameters.set("orientation", "landscape");
				break;
			default:
				parameters.set("orientation", "landscape");
				parameters.set("rotation", 180);
				break;
		}
		
		camera.setParameters(parameters);
		
		try {
			camera.startPreview();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		closeCamera();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		WindowManager w =  (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display d = w.getDefaultDisplay();
		
		int dwidth = d.getWidth();
		int dheight = d.getHeight();
		
		if (dwidth < dheight){
			/** when the orientation is in portrait **/
			setMeasuredDimension(dwidth+210, dheight); // stretches the portrait view so that the camera preview fills the screen
			ViewGroup.LayoutParams lp = getLayoutParams();
			if (lp.getClass() == FrameLayout.LayoutParams.class){
				((FrameLayout.LayoutParams)lp).gravity = Gravity.CENTER;
				((FrameLayout.LayoutParams)lp).height = LayoutParams.FILL_PARENT;
				((FrameLayout.LayoutParams)lp).width = LayoutParams.FILL_PARENT;
			}
			setLayoutParams(lp);
		}
		else
			setMeasuredDimension(dwidth, dheight);

		getHolder().setFixedSize(dwidth, dheight);
		
	}
}
