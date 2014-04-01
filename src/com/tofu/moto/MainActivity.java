package com.tofu.moto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import net.java.frej.*;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;;
	LayoutInflater controlInflater = null;
    private Thread thread;
	Button buttonTakePicture;
	Calendar beginTime;
	static final int REQUEST_IMAGE_CAPTURE = 1;
	private Bitmap bitmap;
	private ImageView imageView;
    private String text;
    // add process bar for converting process, modified on 4/1
    ProgressDialog progress;
	Handler updateBarHandler;
	Button StartProgressBtn;
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
	private static final String    TAG = "Moto::MainActivity";
	
	
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("binarize");

                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    
    
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
		 
		//   text = "Date: Friday, May,.March 28, 2014 at 10:303m\n"+
		//		  "Location: 2145 Sheridan Road\n" +
	//			  "Tech Room B211\n"+
	//			  "Seminar Host: Prof. Dongning Guo\n"+
//				  "Contact/or more information: Lana Kl'perman, 8474670028, IanaQeecs.narthwestern.edu\n";
//		   fuzzyMatch(text);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFormat(PixelFormat.UNKNOWN);
	    surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
	    surfaceHolder = surfaceView.getHolder();
	    surfaceHolder.addCallback(this);
	    //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       // imageView = (ImageView) findViewById(R.id.imageView1);
	    controlInflater = LayoutInflater.from(getBaseContext());
	    View viewControl = controlInflater.inflate(R.layout.control, null);
	    LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	    this.addContentView(viewControl, layoutParamsControl);
	    
	    //thread = new Thread(this);
	    //thread.start();
	    buttonTakePicture = (Button)findViewById(R.id.takepicture);
	    buttonTakePicture.setOnClickListener(new Button.OnClickListener(){
	    @Override
	    public void onClick(View arg0) {	    
	     camera.takePicture(myShutterCallback,myPictureCallback_RAW, myPictureCallback_JPG);
	    }});
	    
		// set the autofocus function for the camera
	    LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.background);
		layoutBackground.setOnClickListener(new LinearLayout.OnClickListener(){
		@Override
		public void onClick(View arg0) {
		  buttonTakePicture.setEnabled(false);
		  camera.autoFocus(myAutoFocusCallback);
		}});
		
		
		// progress bar
		// progress bar, modified on 4/1, added asynctask

		
		StartProgressBtn = (Button)findViewById(R.id.startprogress);
		progress = new ProgressDialog(this);
		progress.setMessage("Processing");
		progress.setCancelable(false);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		StartProgressBtn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v){
		
				new BackgroundAsyncTask().execute();
				StartProgressBtn.setClickable(false);
			}
		});
		
		
	}

	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
		  @Override
		  public void onAutoFocus(boolean arg0, Camera arg1) {
		  
		   buttonTakePicture.setEnabled(true);
		  }
    };
    
    ShutterCallback myShutterCallback = new ShutterCallback(){

		 @Override
		 public void onShutter() {
		 

		 }
	};
	PictureCallback myPictureCallback_RAW = new PictureCallback(){
		 @Override
		 public void onPictureTaken(byte[] arg0, Camera arg1) {		  
		 }
	};
	
	PictureCallback myPictureCallback_JPG = new PictureCallback(){

		 @Override
		 public void onPictureTaken(byte[] arg0, Camera arg1) {
			 

	       bitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
	       int dstWidth = 800;
		   int dstHeight = (int)( 800.0/(double)bitmap.getWidth()*(double)bitmap.getHeight());
		  
	       Mat myMat = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8U,new Scalar(4));
		   Utils.bitmapToMat(bitmap, myMat);
		   Mat inMat = new Mat(dstHeight,dstWidth,CvType.CV_8U, new Scalar(4));
		   Imgproc.resize(myMat, inMat, inMat.size(),0,0,Imgproc.INTER_CUBIC);
		      
		   Mat gryMat1 = new Mat(dstHeight,dstWidth,CvType.CV_8U);
		   Mat gryMat2 = new Mat(dstHeight,dstWidth,CvType.CV_8U);
		   Imgproc.cvtColor(inMat, gryMat1, Imgproc.COLOR_RGBA2GRAY);
		  //binarization
		  
		   FindFeatures(gryMat1.getNativeObjAddr(), gryMat2.getNativeObjAddr());
		   bitmap.recycle();
		   myMat.release();
		   inMat.release();
		   gryMat1.release();
		   bitmap = Bitmap.createBitmap(dstWidth,dstHeight,Config.ARGB_8888);
		   Utils.matToBitmap(gryMat2, bitmap);
		 
		  //Bitmap mutableBitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false).copy(Bitmap.Config.ARGB_8888, true);
		  //Canvas canvas = new Canvas(mutableBitmap);
		  //surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
		  //surfaceView.draw(canvas);
		  try {
			//showprocessbar(surfaceView);
			text = tess(bitmap);
			Log.i(TAG,text);
			//match infor
			fuzzyMatch(text);
			//System.out.println(text);
			///test test test
		} catch (IOException e) {

			e.printStackTrace();
		}
		  
		 }
	};
	public void fuzzyMatch(String text)
	{
		//preprocessing of text
		//beginTime = Calendar.getInstance();
		String line =text;
		Regex regex;
//		String pattern = "[^\n" +
//				"@loc\n"+
//				"]\n"+ //regex
//				"::loc\n"+//def sub str
//				"(^add,addr*,address,location)\n";
		//match location/address
		String addr1="";
		String addr2="";
		String tm="";
		String pattern = "{(^location,addr*)~A}|$A";
		Log.i(TAG,pattern);
		String punctuators = ",";
		double threshold = -1;
		regex = new Regex(pattern, threshold, punctuators);
		boolean b = regex.match(line);
		int i = regex.presentInSequence(line);
		if (i>=0)
		{
			String suffix = regex.suffix();
			int j = suffix.length();
			if (j>40)
				j=40;
			line = suffix.substring(0,j);
		}
		//match street line
		pattern = "((#)~A, (!\\[A-Za-z\\]+)~B, (^Street,St,Road, Rd, Building, Avenue, Ave*, Lane, Place, Pl)~C)|$A_$B_$C";
		//((#)~A, (!\[A-Za-z\]+)~B, (^Street,St,Room,Road, Rd, Building, Avenue, Ave*, Lane, Place, Pl)~C, (^(Apt*, Room, Rm, Fl*),(!\[A-Za-z\]+))~D,(^(Apt*, Room, Rm, Fl*),(!\[A-Za-z\]+))~E,(=(#),(!\[A-Za-z\]*))~F)|$A_$B_$C_$D_$E_$F
		//pattern = "{(^(?\b+)~A,(^street,st,room,building, avenue, ave*, lane, place, pl)~B,(#)~C)}|Location_$A_$B_$C";
		Log.i(TAG,line);
		regex = new Regex(pattern);
		i = regex.presentInSequence(line);
		//regex.matchFromStart(seq)
		if (i>=0)
		{
			int j = regex.getMatchEnd();
			 addr1= regex.getReplacement();
			 String left = line.substring(0, i);
			 String right = line.substring(j+1, line.length());
			 line = left+right;
		}
		//match second level address
		pattern = "((^(^Apt*, Room, Rm, Fl*),(!\\[A-Za-z\\]+))~D,(?(^Apt*, Room, Rm, Fl*))~E,(=(#),(!\\[A-Za-z\\]*))~F)|$D_$E_$F";
		
		Log.i(TAG,line);
		regex = new Regex(pattern);
		i = regex.presentInSequence(line);
		if (i>=0)
		{
			addr2 = regex.getReplacement();
		}
		
		//match date/time
		line = text;
		pattern = "(^date,time)";
		regex = new Regex(pattern);
		i = regex.presentInSequence(line);
		if (i>=0)
		{
			String suffix = regex.suffix();
			int j = suffix.length();
			if (j>40)
				j=40;
			line = suffix.substring(0,j);
		}
		//intent.putExtra("eventLocation", myAddress)
		//match format hh/mm? Friday, month, day, year, hh/mm?
//		pattern = "((?^Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday, Mon, Tue, Wed, Thu, Fri, Sat, Sun)~A,"+
//				  "(?^January, February, March, April, May, June, July, August, September, October, November, December, Jan, Feb, Mar, Apr, May, Jun,  Jul,  Aug, Sep, Oct, Nov, Dec)~B,"+
//				  "(#31)~C, (#2000:2500)~D, (?^on, at, in), ((#),(?:),(?#), (?!\\[a-zA-Z\\]+))~E)|$A_$B_$C_$D_$E";
//		
		pattern="((?^Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday, Mon, Tue, Wed, Thu, Fri, Sat, Sun)~W,"+
				"(^January, February, March, April, May, June, July, August, September, October, November, December, Jan, Feb, Mar, Apr, May, Jun,  Jul,  Aug, Sep, Oct, Nov, Dec)~B,"+
				"(#31)~D, (#2000:2500)~Y, (?^on, at, in), (?(#)~N,(?:),(?#60)~K, (?^pm,am,3m)~L)"+
				")|$Y/$B/$D/$N/$K/$L";
		regex = new Regex(pattern);
		i = regex.presentInSequence(line);
		if (i>=0)
		{
			String temp = regex.getReplacement();
			SimpleDateFormat ft;
			String pm = temp.substring(temp.length()-2,temp.length());
			if (0==pm.compareTo("3m"))
			{
				temp = temp.substring(0,temp.length()-2);
				temp = temp + "am";
			}
			int count=0;
			int j = temp.lastIndexOf("/");
			while (j==temp.length()-1)
			{
				count++;
				temp = temp.substring(0, j);
				j=temp.lastIndexOf("/");
			}
			
			if (count==0)
				ft = new SimpleDateFormat("yyyy/MMMM/dd/hh/mm/a");
			else if (count==1)
				ft = new SimpleDateFormat("yyyy/MMMM/dd/hh/mm");
			else if (count==2)
				ft = new SimpleDateFormat("yyyy/MMMM/dd/hh");
			else
				ft = new SimpleDateFormat("yyyy/MMMM/dd");
			try {
				ft.parse(temp);
				beginTime = ft.getCalendar();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else
		{
			pattern="((#12)~A,(#31)~B,(#2000:2500)~C)|$A/B/$C";
			regex = new Regex(pattern);
			if (i>=0)
			{
				
				String temp = regex.getReplacement();
				SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd");
				try {
					ft.parse(temp);
					beginTime = ft.getCalendar();
				} catch (ParseException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
	}
	
	// add progressing bar, add asynctask to solve multi-thread problem
		public class BackgroundAsyncTask extends AsyncTask<String, Integer, Void>{
	        int myProgressCount;

	        @Override
	        protected void onPreExecute() {
	        	// show the processing bar
	        	super.onPreExecute();
	        	progress.show();
	        }
	        protected Void doInBackground(String... params) {
	        	// do OCR Prcessing here, and update
	        	// comment out the following while loop if not needed
	            while (myProgressCount < 80) {
	                  myProgressCount++;

	                  publishProgress(myProgressCount);
	                  SystemClock.sleep(100);
	            }
	            return null;
	        }
	        @Override
	        protected void onProgressUpdate(Integer... values) {
	        	// update the processing bar, no need to be modified
	        	super.onProgressUpdate(values);
	        }

	        @Override
	        protected void onPostExecute(Void result) {
	        	// after processing, display result or do the calendar event
	        	super.onPostExecute(result);
	        	progress.dismiss();
	            StartProgressBtn.setClickable(true);
	        }
	 
		}
		
	
		  
//	//@Override
//    public void onClick(View view) {
//    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
//    	if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//	        // Create the File where the photo should go
//	        //File photoFile = null;
//	        //try {
//	        //    photoFile = createImageFile();
//	        //} catch (IOException ex) {
//	            // Error occurred while creating the File
//	        //}
//	        // Continue only if the File was successfully created
//	        //if (photoFile != null) {
//	        //    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//	        //            Uri.fromFile(photoFile));
//	            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//	        //}
//	    }
//        //startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE); 
//    }
     
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
//	        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {  
//	            Bitmap photo = (Bitmap) data.getExtras().get("data"); 
//	            imageView.setImageBitmap(photo);
//	            String text;
//	            try {
//					text = tess(photo);
//				} catch (IOException e) {
//					
//					text = "exception";
//					e.printStackTrace();
//				}
//	            System.out.println(text);
//	        }  
//	} 
	
	
	protected String tess(Bitmap bitmap) throws IOException
	{
		String DATA_PATH = getApplicationContext().getFilesDir().toString()+//Environment.getDataDirectory().toString() +
	            "/motochallenge/";
		File dir = new File(DATA_PATH + "tessdata");
	    dir.mkdirs();
	    if (!(new File(DATA_PATH + "tessdata/eng.traineddata")).exists()) {
	    	try {
	    		AssetManager assetManager = getAssets();
	            InputStream in = assetManager.open("tessdata/eng.traineddata");
	            OutputStream out = new FileOutputStream(DATA_PATH
	                    + "tessdata/eng.traineddata");
	            byte[] buf = new byte[1024];
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	            in.close();
	            out.close();
	    	}catch (IOException e) {}
	    }
		TessBaseAPI baseApi = new TessBaseAPI();
		
		baseApi.init(DATA_PATH,"eng");
		baseApi.setImage(bitmap);
		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		return recognizedText;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	
	  if(previewing){
	    camera.stopPreview();
	    previewing = false;
	  }
     
      
	  if (camera != null){
	    try {
	      camera.setPreviewDisplay(surfaceHolder);
	      camera.startPreview();
	      previewing = true;
	    } catch (IOException e) {
	  
	      e.printStackTrace();
	    }
	  }
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	
	  camera = Camera.open();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	  camera.stopPreview();
	  camera.release();
	  camera = null;
	  previewing = false;
	}
    //	
//    String mCurrentPhotoPath;

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        // do public first for debug
//        File storageDir = getCacheDir();
//        /*File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);*/
//        //private uses getExternalFilesDir()
//        
//        File image = File.createTempFile(
//            imageFileName,  /* prefix */
//            ".jpg",         /* suffix */
//            storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        //debug only add to gallery
//        //galleryAddPic();
//        return image;
//    }
    
/////////////////add to calendar event//////////////
	
	public void addCalendarEvent(){
		 
	  	 Calendar testDate = Calendar.getInstance();
		 testDate.set(Calendar.YEAR,2015);
		 testDate.set(Calendar.MONTH, 2);
		 testDate.set(Calendar.DAY_OF_MONTH, 20);
		 testDate.set(Calendar.HOUR_OF_DAY, 12);
		 testDate.set(Calendar.MINUTE, 35);
		 String address = "Tech Room G221";

	        

	     Intent intent = new Intent(Intent.ACTION_INSERT);
	     intent.setData(Events.CONTENT_URI);
	     intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, testDate.getTime().getTime());
	     intent.putExtra("eventLocation", address);
	     startActivity(intent); 	        
 
	}
}
