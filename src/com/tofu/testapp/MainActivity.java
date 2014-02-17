package com.tofu.testapp;

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

public class MainActivity extends Activity {

	static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //dispatchTakePictureIntent();
        return true;
    }
    //
    //capture photo
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
    public void captureImage(View view){
    	Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
    	if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
    	        // Create the File where the photo should go
    	        File photoFile = null;
    	        try {
    	            photoFile = createImageFile();
    	        } catch (IOException ex) {
    	            // Error occurred while creating the File
    	        }
    	        // Continue only if the File was successfully created
    	        if (photoFile != null) {
    	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
    	                    Uri.fromFile(photoFile));
    	            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    	        }
    	    }
    }
    
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
//        }
//    }
    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // do public first for debug
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        //private uses getExternalFilesDir()
        
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        //debug only add to gallery
        galleryAddPic();
        return image;
    }
    //debug only add to add to gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    
    
}
