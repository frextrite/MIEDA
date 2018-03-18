package com.example.amol.mieda;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* @TODO:
 * Remove CameraActivity class
 * Write Camera Code from scratch
 * Use Intent(MediaStore.ACTION_IMAGE_CAPTURE */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionMenu fam;
    private FloatingActionButton fab_camera, fab_audio;
    public static final int TAKE_PICTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final String TAG = "MainActivity";
    private static String FILE_NAME = null;
    private static String PATH = Environment.DIRECTORY_DCIM;
    private static String ENCODED_IMAGE = null;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fam = findViewById(R.id.fam);
        fab_camera = findViewById(R.id.fab_camera);

        fam.setIconAnimated(false);

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fam.isOpened()) {
                    fam.close(true);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.CAMERA)) {

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                } else {
                    generateFileName();
                    // Fixes ClipData.Item.getUri() error
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());

                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                    File image = new File(dir, FILE_NAME);
                    camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                    camera_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    camera_intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(camera_intent, TAKE_PICTURE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera_intent, TAKE_PICTURE);
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: DENIED!");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + FILE_NAME;
            PATH = path;
            Intent preview = new Intent(MainActivity.this, PreviewActivity.class);
            preview.putExtra("PATH_NAME", path);
            Log.d(TAG, "onActivityResult: " + path);
            encodeBitmapToBase64String();
            new UploadImage().execute();
            startActivity(preview);
        }

    }

    protected void generateFileName() {
        String prefix = "MIEDA_";
        String extension = ".jpg";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        FILE_NAME = prefix + timeStamp + extension;
    }

    protected void encodeBitmapToBase64String() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(PATH);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] b = byteStream.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        ENCODED_IMAGE = encodedImage;
    }



    private class UploadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("imageBase64", ENCODED_IMAGE);
                String dataToSend = jsonObject.toString();
                String whereToSend = "http://192.168.1.xxx:XXXX";
                URL url = new URL(whereToSend);
//                JSONObject jsoni = new JSONObject();
//                jsoni.put("hey", "string long");
//                dataToSend = jsoni.toString();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(dataToSend.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.connect();

                Log.d(TAG, "doInBackground: " + dataToSend);

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(dataToSend);
                wr.flush();
                wr.close();


                /* Read Data from Server */
                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                String result = sb.toString();
                Log.d(TAG, "Response: " + result);
                //Response = new JSONObject(result);
                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
