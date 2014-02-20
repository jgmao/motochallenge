package com.tofu.moto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
	    LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.background);
		layoutBackground.setOnClickListener(new LinearLayout.OnClickListener(){

		@Override
		public void onClick(View arg0) {
		   

		  buttonTakePicture.setEnabled(false);
		  camera.autoFocus(myAutoFocusCallback);
		}});

        //this.imageView = (ImageView)this.findViewById(R.id.imageView1);
  
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
		  //text = tess(bitmap);
		  text = "test";
		 }
	};
		  
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
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.init("file://android_assets/eng.traineddata", "eng");
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
