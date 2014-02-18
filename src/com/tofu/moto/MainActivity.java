package com.tofu.moto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	private Bitmap bitmap;
	private ImageView imageView;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //imageView = (ImageView) findViewById(R.id);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        
	}
    
	//@Override
    public void onClick(View view) {
    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
    	if (cameraIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        //File photoFile = null;
	        //try {
	        //    photoFile = createImageFile();
	        //} catch (IOException ex) {
	            // Error occurred while creating the File
	        //}
	        // Continue only if the File was successfully created
	        //if (photoFile != null) {
	        //    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	        //            Uri.fromFile(photoFile));
	            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
	        //}
	    }
        //startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE); 
    }
     
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {  
	            Bitmap photo = (Bitmap) data.getExtras().get("data"); 
	            imageView.setImageBitmap(photo);
	        }  
	} 
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //dispatchTakePictureIntent();
        return true;
    }
    //	
    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // do public first for debug
        File storageDir = getCacheDir();
        /*File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);*/
        //private uses getExternalFilesDir()
        
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        //debug only add to gallery
        //galleryAddPic();
        return image;
    }
    
    
}
