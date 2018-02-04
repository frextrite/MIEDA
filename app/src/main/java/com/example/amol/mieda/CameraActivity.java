package com.example.amol.mieda;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

public class CameraActivity extends AppCompatActivity {

    private CameraView cameraView;
    private FloatingActionButton fabCaptureImage;

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
                        cameraView.setFocus(CameraKit.Constants.FOCUS_OFF);
                        Bitmap image = cameraKitImage.getBitmap();
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int nh = (int) ( image.getHeight() * (2048.0 / image.getWidth()) );
                                Bitmap scaled = Bitmap.createScaledBitmap(image, 2048, nh, true);
                                i.setImageBitmap(scaled);
                            }
                        });*/
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
