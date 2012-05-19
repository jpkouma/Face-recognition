package jp.kouma.face;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class SampleViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Sample::SurfaceView";

    private Camera              mCamera;
    private SurfaceHolder       mHolder;
    private int                 mFrameWidth;
    private int                 mFrameHeight;
    private byte[]              mFrame;
    private boolean             mThreadRun;
    private int[] faces = new int[3*4+1];
    public SampleViewBase(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
       
    	Log.i(TAG, "surfaceCreated");
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(sizes, width, height);
            params.setPreviewSize(optimalSize.width, optimalSize.height);
            mFrameWidth = optimalSize.width;
            mFrameHeight = optimalSize.height;
            
            
            
            mCamera.setParameters(params);
            
            _init();
        	
            try {
				mCamera.setPreviewDisplay(null);
			} catch (IOException e) {
				Log.e(TAG, "mCamera.setPreviewDisplay fails: " + e);
			}
            mCamera.startPreview();
        }
    }

    protected abstract void _init();
    
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mCamera = Camera.open(Camera.getNumberOfCameras()-1);
        mCamera.setPreviewCallback(new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (SampleViewBase.this) {
                    mFrame = data;
                    SampleViewBase.this.notify();
                }
            }
        });
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mThreadRun = false;
        if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
        	Log.i(TAG, "Application terminated");
            mThreadRun = false;
            if (mCamera != null) {
                synchronized (this) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    
    protected abstract Bitmap processFrame(byte[] data);
    protected abstract int[] getFaces();

    public void run() {
        mThreadRun = true;
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(20);
        float textWidth;
        //int[] face = new int[4*3+1];
        
        Log.i(TAG, "Starting processing thread");
        while (mThreadRun) {
            Bitmap bmp = null;

            synchronized (this) {
                try {
                    this.wait();
                    bmp = processFrame(mFrame);
                    faces = getFaces();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                	String s = null;
                	if(faces[0]>0){
                		s+= faces[1]+" "+faces[2]+" "+faces[3]+" "+faces[4];
                		textWidth = paint.measureText(s);
                    	canvas.drawText(s, (getWidth()-textWidth)/2, 20, paint);
	                    int cnt=1;
	                    int x, y, w, h;
	                    for (int i = 0; i < faces[0]; i++) {
	                    	x = faces[cnt+0];
	                    	y = faces[cnt+1];
	                    	w = faces[cnt+2];
	                    	h = faces[cnt+3];
	                    	paint.setStrokeWidth(2);
	                        paint.setStyle(Paint.Style.STROKE);
	                        canvas.drawRect(x, y, x+w, y+h, paint);
	                        cnt+=4;
	                    }
                	}
                	Matrix matrix = new Matrix();
                	matrix.reset();
                	int degree = 270;
                	matrix.setRotate(degree, bmp.getWidth()/2, bmp.getHeight()/2);
                	canvas.setMatrix(matrix);     
                	canvas.drawBitmap
                	(bmp, (-canvas.getHeight() + getFrameHeight()) / 2, 
                			(canvas.getWidth() - getFrameWidth()) / 2, null);
                	canvas.setMatrix(null);
                	s = "["+mFrameWidth+" "+mFrameHeight+"]."+" FD"+" "+faces[0]+" : ";
                	textWidth = paint.measureText(s);
                	canvas.drawText(s, (getWidth()-textWidth)/2, 20, paint);
                	int cnt=1;
                    int x, y, w, h;
                    for (int i = 0; i < faces[0]; i++) {
                    	x = faces[cnt+0];
                    	y = faces[cnt+1];
                    	w = faces[cnt+2];
                    	h = faces[cnt+3];
                    	paint.setStrokeWidth(2);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(x, y, x+w, y+h, paint);
                        cnt+=4;
                    }
                    mHolder.unlockCanvasAndPost(canvas); 
                }
                bmp.recycle();
            }
        }
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
    
//    public void onOrientationChanged(int orientation) {
//        if (orientation == ORIENTATION_UNKNOWN) return;
//        android.hardware.Camera.CameraInfo info =
//               new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//        orientation = (orientation + 45) / 90 * 90;
//        int rotation = 0;
//        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//            rotation = (info.orientation - orientation + 360) % 360;
//        } else {  // back-facing camera
//            rotation = (info.orientation + orientation) % 360;
//        }
//        params.setRotation(rotation);
//    }
    
}
