package com.tofu.moto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;;
	LayoutInflater controlInflater = null;

	Button buttonTakePicture;
	
	static final int REQUEST_IMAGE_CAPTURE = 1;
	private Bitmap bitmap;
	private ImageView imageView;
    private String text;
    // add process bar for converting process
    ProgressDialog progress;
	Handler updateBarHandler;
	

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFormat(PixelFormat.UNKNOWN);
	    surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
	    surfaceHolder = surfaceView.getHolder();
	    surfaceHolder.addCallback(this);
	    //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //imageView = (ImageView) findViewById(R.id);
	    controlInflater = LayoutInflater.from(getBaseContext());
	    View viewControl = controlInflater.inflate(R.layout.control, null);
	    LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	    this.addContentView(viewControl, layoutParamsControl);
	    
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
		updateBarHandler = new Handler();
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
		  try {
			text = tess(bitmap);
			System.out.println(text);
			///test test test
		} catch (IOException e) {

			e.printStackTrace();
		}
		  
		 }
	};
	
	// add progressing bar
	public void showprocessbar(View view){
		progress = new ProgressDialog(this);
		progress.setMessage("Processing image:)");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setProgress(0);
		progress.setMax(100);
//		progress.setIndeterminate(true);
		progress.show();
		
		
		final Thread t = new Thread(){
			@Override
			public void run(){
				try{
					while(progress.getProgress() <= progress.getMax()){
						Thread.sleep(2000);
						updateBarHandler.post(new Runnable(){
							public void run(){
								progress.incrementProgressBy(5);
							}
						});
						if(progress.getProgress() == progress.getMax())
						{
							progress.dismiss();
						}
					}
				}catch(Exception e){
					
				}
			}
		};
		t.start();
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
    
    
}
