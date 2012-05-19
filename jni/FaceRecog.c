#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
#include <assert.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#include "cv.h"
#include "highgui.h"

// Create memory for calculations
static CvMemStorage* storage = 0;
// Create a new Haar classifier
static CvHaarClassifierCascade* cascade = 0;

IplImage *img, *small_img, *small_tr_img;
int width, height, nt;
double scale;
// Initialization of the face detector
JNIEXPORT void JNICALL Java_jp_kouma_face_Sample0View_init
(JNIEnv* env, jobject thiz, jint jwidth, jint jheight)
{
	// Load the HaarClassifierCascade
	    cascade = (CvHaarClassifierCascade*)
	    		cvLoad( "/sdcard/haarcascade_frontalface_alt.xml", 0, 0, 0 );

	    // Check whether the cascade has loaded successfully.
	    // Else report and error and quit
	    if( !cascade )
	    {
	    	__android_log_print(ANDROID_LOG_ERROR,
	    			"Init", "ERROR: Could not load classifier cascade");
	        exit(-1);
	    }

	    width = jwidth;
	    height = jheight;
	    double scale_flag=(double)width;
	    if(width<height) scale_flag = (double)height;

	    scale = scale_flag/160.0;
	    nt=0;
	    img = cvCreateImage(cvSize(width, height), 8, 1);
	    small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
	    	                         cvRound (img->height/scale)), 8, 1 );
	    small_tr_img = cvCreateImage( cvSize(cvRound (img->height/scale) ,
	    	    	                         cvRound (img->width/scale)), 8, 1 );
	    storage = cvCreateMemStorage(0);
	    __android_log_print(ANDROID_LOG_INFO,
	    	    			"JNI:Init", "cascade loaded. W=%d, H=%d", width, height);
}

void WritePGM(char *filename, IplImage *im){
	FILE *inp = fopen(filename, "w");
	fprintf(inp, "P5 %d %d\n 255\n", im->width, im->height);
	int i, j;
	char x;
	for(i=0; i<im->height; i++){
		for(j=0; j<im->width; j++){
			x = cvGetReal2D(im, i, j);
			fprintf(inp, "%c", x);
		}
	}
	fclose(inp);
}

JNIEXPORT jintArray JNICALL Java_jp_kouma_face_Sample0View_DetectFaces
(JNIEnv* env, jobject thiz, jbyteArray arr, jint max_num_faces)
{

	__android_log_print(ANDROID_LOG_INFO, "JNI:Detect", "DetectFaces()");

	 jintArray result;
	 int size = max_num_faces*4+1;
	 result = (*env)->NewIntArray(env, size);
	 if (result == NULL) {
	     return NULL; /* out of memory error thrown */
	 }

	jbyte *carr = (*env)->GetByteArrayElements(env, arr, NULL);
	if(carr==NULL){
		 __android_log_print(ANDROID_LOG_ERROR, "JNI:Detect", "No byte received");
	 }
	memmove((unsigned char *)img->imageData, (unsigned char *)carr,
			sizeof(char)*width*height);
	__android_log_print(ANDROID_LOG_INFO, "JNI:Detect", "memmove()");
	(*env)->ReleaseByteArrayElements(env, arr, carr, 0);

	int i;
	// Clear the memory storage which was used before
	cvClearMemStorage( storage );
	cvResize( img, small_img, CV_INTER_LINEAR );
	cvTranspose(small_img, small_tr_img);
	cvFlip(small_tr_img, small_tr_img, 0);
	cvEqualizeHist(small_tr_img, small_tr_img );
	__android_log_print(ANDROID_LOG_INFO,
									"JNI:Detect", "cvResize()");
	char ff[256];
	sprintf(ff, "sdcard/frame_%d.txt", nt++);
//	WritePGM(ff, small_tr_img);
	// There can be more than one face in an image. So create a growable sequence of faces.
	// Detect the objects and store them in the sequence
	CvSeq* faces = cvHaarDetectObjects( small_tr_img, cascade, storage,
										1.1, 2, CV_HAAR_DO_CANNY_PRUNING,
										cvSize(32, 32) );
	__android_log_print(ANDROID_LOG_INFO,
								"JNI:Detect", "cvHaarDetectObjects().(%g)", scale);

	jint fill[size];
	fill[0]=faces->total;
	// Loop the number of faces found.
	int cnt=1;

	int tot=max_num_faces;
	if(max_num_faces>faces->total) tot = faces->total;


	for( i = 0; i < tot; i++ )
	{
	   // Create a new rectangle for drawing the face
		CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
		fill[cnt]= cvRound(r->x*scale);
		fill[cnt+1]= cvRound(r->y*scale);
		fill[cnt+2]= cvRound(r->width*scale);
		fill[cnt+3]= cvRound(r->height*scale);
//		FILE *inp = fopen(ff, "w");
//		fprintf(inp, "%d %d %d %d\n", fill[cnt], fill[cnt+1],fill[cnt+2],fill[cnt+3]);
//		fclose(inp);
		cnt+=4;
	}

	__android_log_print(ANDROID_LOG_INFO,
			"JNI:Detect", "Detected faces %d", faces->total);
	(*env)->SetIntArrayRegion(env, result, 0, size, fill);
	 return result;

}


/* Function prototype */
//JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial0_Preview_MYFUNCTION
//(JNIEnv* env, jobject thiz/*, jlong addrGray, jlong addrRgba*/)
//{
//}
