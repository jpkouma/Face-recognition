package jp.kouma.face;
import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

class Sample0View extends SampleViewBase {
	
	/* Own library functions */
	static {
		System.loadLibrary("opencv");
	    System.loadLibrary("FaceRecog");
	  }
	public native void init(int width, int height);
	public native  int[] DetectFaces(byte[] yuv, int max_num_faces);
	
	int frameSize = getFrameWidth() * getFrameHeight();
    int max_num_faces = 3;
    public int[] face = new int[max_num_faces*4+1];
	Bitmap bmp;
	
    public Sample0View(Context context) {
        super(context);
    }
    
    protected void _init(){
    	init(getFrameWidth(), getFrameHeight());
    	Log.i("FD", "_init()" );
    }
    
    protected int[] getFaces(){
    	return(face);
    }

    @Override
    protected Bitmap processFrame(byte[] data) {
    	Log.i("FD", "Sart processFrame()");
        face = DetectFaces(data, max_num_faces);
//        Log.i("FD", "faces" + face);
     // Convert to JPG
        //Size previewSize = camera.getParameters().getPreviewSize(); 
        YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, getFrameWidth(), getFrameHeight(), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, getFrameWidth(), getFrameHeight()), 100, baos);
        byte[] jdata = baos.toByteArray();

        // Convert to Bitmap
        bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
        
        return bmp;
    }
      
}
