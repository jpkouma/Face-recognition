package jp.kouma.face;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector.Face;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
    SurfaceHolder mHolder;

    Camera mCamera;

    //This variable is responsible for getting and setting the camera settings
    private Parameters parameters;
    //this variable stores the camera preview size
    private Size previewSize;
    //this array stores the pixels as hexadecimal pairs
    private int[] pixels;
    

    Preview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        CameraPreviewView surfaceView = (CameraPreviewView) findViewById(R.id.LinearLayout01);
        mHolder = surfaceView.getHolder();

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        
        mCamera = Camera.open(Camera.getNumberOfCameras()-1);
        try {
           mCamera.setPreviewDisplay(holder);

           //sets the camera callback to be the one defined in this class
           mCamera.setPreviewCallback(this);

           ///initialize the variables
           parameters = mCamera.getParameters();
//           List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//           Size optimalSize = getOptimalPreviewSize(sizes, w, h);
           //parameters.setPreviewFpsRange(10, 15);
           
           previewSize = parameters.getPreviewSize();
           pixels = new int[3*previewSize.width * previewSize.height];
          

        } catch (IOException exception) {
//        	mCamera.unlock();
//            mCamera.release();
//            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	 
        parameters.setPreviewSize(w, h);
        //set the camera's settings
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		
	}
	
	
	 private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	    final double ASPECT_TOLERANCE = 0.05;
	    double targetRatio = (double) w / h;
	    if (sizes == null) return null;
	
	    Size optimalSize = null;
	    double minDiff = Double.MAX_VALUE;
	
	    int targetHeight = h;
	
	    // Try to find an size match aspect ratio and size
	    for (Size size : sizes) {
	        double ratio = (double) size.width / size.height;
	        if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	        if (Math.abs(size.height - targetHeight) < minDiff) {
	            optimalSize = size;
	            minDiff = Math.abs(size.height - targetHeight);
	        }
	    }
	
	    // Cannot find the one match the aspect ratio, ignore the requirement
	    if (optimalSize == null) {
	        minDiff = Double.MAX_VALUE;
	        for (Size size : sizes) {
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }
	    }
	    return optimalSize;
	}
}