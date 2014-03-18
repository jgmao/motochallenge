#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <getopt.h>
#include <unistd.h>
#include <iostream>
#include <string>
#include <android/log.h>
using namespace std;
using namespace cv;


#define uget(x,y)    at<unsigned char>(y,x)
#define uset(x,y,v)  at<unsigned char>(y,x)=v;
#define fget(x,y)    at<float>(y,x)
#define fset(x,y,v)  at<float>(y,x)=v;

double calcLocalStats (Mat &im, Mat &map_m, Mat &map_s, int winx, int winy) {

	double m,s,max_s, sum, sum_sq, foo;
	int wxh	= winx/2;
	int wyh	= winy/2;
	int x_firstth= wxh;
	int y_lastth = im.rows-wyh-1;
	int y_firstth= wyh;
	double winarea = winx*winy;

	max_s = 0;
	for	(int j = y_firstth ; j<=y_lastth; j++)
	{
		// Calculate the initial window at the beginning of the line
		sum = sum_sq = 0;
		for	(int wy=0 ; wy<winy; wy++)
			for	(int wx=0 ; wx<winx; wx++) {
				foo = im.uget(wx,j-wyh+wy);
				sum    += foo;
				sum_sq += foo*foo;
			}
		m  = sum / winarea;
		s  = sqrt ((sum_sq - (sum*sum)/winarea)/winarea);
		if (s > max_s)
			max_s = s;
		map_m.fset(x_firstth, j, m);
		map_s.fset(x_firstth, j, s);

		// Shift the window, add and remove	new/old values to the histogram
		for	(int i=1 ; i <= im.cols-winx; i++) {

			// Remove the left old column and add the right new column
			for (int wy=0; wy<winy; ++wy) {
				foo = im.uget(i-1,j-wyh+wy);
				sum    -= foo;
				sum_sq -= foo*foo;
				foo = im.uget(i+winx-1,j-wyh+wy);
				sum    += foo;
				sum_sq += foo*foo;
			}
			m  = sum / winarea;
			s  = sqrt ((sum_sq - (sum*sum)/winarea)/winarea);
			if (s > max_s)
				max_s = s;
			map_m.fset(i+wxh, j, m);
			map_s.fset(i+wxh, j, s);
		}
	}

	return max_s;
}
void Wolf (Mat im, Mat output, int winx, int winy, double k, double dR) {
	 // __android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "0.1------------------");

	double m, s, max_s;
	double th=0;
	double min_I, max_I;
	int wxh	= winx/2;
	int wyh	= winy/2;
	int x_firstth= wxh;
	int x_lastth = im.cols-wxh-1;
	int y_lastth = im.rows-wyh-1;
	int y_firstth= wyh;
	int mx, my;

	// Create local statistics and store them in a double matrices
	Mat map_m = Mat::zeros (im.rows, im.cols, CV_32F);
	Mat map_s = Mat::zeros (im.rows, im.cols, CV_32F);
	//  __android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "1------------------");
	max_s = calcLocalStats (im, map_m, map_s, winx, winy);
	//  __android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "2------------------");
	minMaxLoc(im, &min_I, &max_I);

	Mat thsurf (im.rows, im.cols, CV_32F);

	// Create the threshold surface, including border processing
	// ----------------------------------------------------
	//__android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "3------------------");
	for	(int j = y_firstth ; j<=y_lastth; j++) {

		// NORMAL, NON-BORDER AREA IN THE MIDDLE OF THE WINDOW:
		for	(int i=0 ; i <= im.cols-winx; i++) {

			m  = map_m.fget(i+wxh, j);
    		s  = map_s.fget(i+wxh, j);

     		th = m + k * (s/max_s-1) * (m-min_I);

    		thsurf.fset(i+wxh,j,th);

    		if (i==0) {
        		// LEFT BORDER
        		for (int i=0; i<=x_firstth; ++i)
                	thsurf.fset(i,j,th);

        		// LEFT-UPPER CORNER
        		if (j==y_firstth)
        			for (int u=0; u<y_firstth; ++u)
        			for (int i=0; i<=x_firstth; ++i)
        				thsurf.fset(i,u,th);

        		// LEFT-LOWER CORNER
        		if (j==y_lastth)
        			for (int u=y_lastth+1; u<im.rows; ++u)
        			for (int i=0; i<=x_firstth; ++i)
        				thsurf.fset(i,u,th);
    		}

			// UPPER BORDER
			if (j==y_firstth)
				for (int u=0; u<y_firstth; ++u)
					thsurf.fset(i+wxh,u,th);

			// LOWER BORDER
			if (j==y_lastth)
				for (int u=y_lastth+1; u<im.rows; ++u)
					thsurf.fset(i+wxh,u,th);
		}

		// RIGHT BORDER
		for (int i=x_lastth; i<im.cols; ++i)
        	thsurf.fset(i,j,th);

  		// RIGHT-UPPER CORNER
		if (j==y_firstth)
			for (int u=0; u<y_firstth; ++u)
			for (int i=x_lastth; i<im.cols; ++i)
				thsurf.fset(i,u,th);

		// RIGHT-LOWER CORNER
		if (j==y_lastth)
			for (int u=y_lastth+1; u<im.rows; ++u)
			for (int i=x_lastth; i<im.cols; ++i)
				thsurf.fset(i,u,th);
	}
	 //__android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "4------------------");

	for	(int y=0; y<im.rows; ++y)
	for	(int x=0; x<im.cols; ++x)
	{
    	if (im.uget(x,y) >= thsurf.fget(x,y))
    	{
    		output.uset(x,y,255);
    	}
    	else
    	{
    	    output.uset(x,y,0);
    	}
    }
	// __android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "5------------------");

}

extern "C" {

JNIEXPORT void JNICALL Java_com_tofu_moto_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_tofu_moto_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
	int winx=40, winy=40;
	float optK = 0.5;
    //make sure both are greyscale
	Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    //vector<KeyPoint> v;

    winy = (int) (2.0 * mGr.rows-1)/3;
    winx = (int) mGr.cols-1 < winy ? mGr.cols-1 : winy;
    if (winx > 100)
        winx = winy = 40;
    //Mat output=Mat::zeros(mGr.size(),mGr.type());
   // __android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "0------------------");
    Wolf(mGr, mRgb, winx, winy, optK, 128);
    //__android_log_write(ANDROID_LOG_INFO, "MOTOTAG", "6------------------");
   //__android_log_print(ANDROID_LOG_INFO,"MOTOTAG","type1: %d, type2:%d\n",mGr.type(),mRgb.type());
    //cv::cvtColor(output,mRgb,cv::COLOR_GRAY2BGRA,4);

}
}
