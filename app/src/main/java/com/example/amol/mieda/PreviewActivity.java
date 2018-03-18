package com.example.amol.mieda;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final String TAG = "PreviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Log.d(TAG, "onCreate: ");

        imageView = findViewById(R.id.preview_image_view);

        /*byte[] bytes = getIntent().getByteArrayExtra(CameraActivity.KEY);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);*/

        /* @TODO:
         * Image should be scaled according to aspect ratio */

        Bundle extras = getIntent().getExtras();
        String path = extras.getString("PATH_NAME");

        Log.d(TAG, "onCreate: " + path);

        Bitmap bmp = BitmapFactory.decodeFile(path);

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        int screenWidth = display.widthPixels;
        int screenHeight = display.heightPixels;

        Bitmap scaledImage = Bitmap.createScaledBitmap(bmp, screenWidth, screenHeight, true);

        imageView.setImageBitmap(scaledImage);
    }
}
