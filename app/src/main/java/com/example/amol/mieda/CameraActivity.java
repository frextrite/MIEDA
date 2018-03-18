package com.example.amol.mieda;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends AppCompatActivity {

    private CameraView cameraView;
    private FloatingActionButton fabCaptureImage;
    public static final String KEY = "preview_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initializing
        cameraView = findViewById(R.id.camera);
        fabCaptureImage = findViewById(R.id.fab_capture_image);

        // Camera Properties
        cameraView.setFocus(CameraKit.Constants.FOCUS_TAP);


        fabCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                    @Override
                    public void callback(CameraKitImage cameraKitImage) {
                        Bitmap image = cameraKitImage.getBitmap();

                        /* @TODO:
                         * not use a global class instead save the image on storage */
                        //GlobalImage.finalImage = image;

                        /*// Compressing Image
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] bytes = stream.toByteArray();*/

                        Intent preview_intent = new Intent(CameraActivity.this, PreviewActivity.class);
                        startActivity(preview_intent);
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onStop() {
        cameraView.stop();
        super.onStop();
    }
}
