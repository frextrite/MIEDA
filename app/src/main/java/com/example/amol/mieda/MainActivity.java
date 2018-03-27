package com.example.amol.mieda;

import android.Manifest;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* Variable Declarations */
    private FloatingActionMenu fam;
    private FloatingActionButton fab_camera, fab_audio;

    private static final int TAKE_PICTURE = 1;
    private static final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 100;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final String TAG = "MainActivity";

    private static String FILE_NAME = null;
    private static String PATH = null; //Environment.DIRECTORY_DCIM;
    private static String ENCODED_IMAGE = null;
    private static String SERVER_URL = "http://192.168.1.xxx:XXXX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Ask for permissions */
        askPermission();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fam = findViewById(R.id.fam);
        fab_camera = findViewById(R.id.fab_camera);

        fam.setIconAnimated(false); // Remove FAM animation

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Close FAM if it is opened */
                if(fam.isOpened()) {
                    fam.close(true);
                }

                /* Check for Camera Permissions */
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.CAMERA)) {

                    } else {
                        /* Ask for permission */
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                } else {
                    /* Generate a new File Name */
                    generateFileName();

                    /* Fixes ClipData.Item.getUri() error */
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());

                    /* File is stored at the location: /storage/emulated/0/DCIM/FILE_NAME */
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                    File image = new File(dir, FILE_NAME);
                    camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));

                    /* Grant URI Permissions */
                    camera_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    camera_intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(camera_intent, TAKE_PICTURE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Image has been captured and stored on the SD Card */
        if(requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + FILE_NAME;
            PATH = path;

            /* Start the Preview Activity */
            Intent preview = new Intent(MainActivity.this, PreviewActivity.class);
            preview.putExtra("PATH_NAME", path);

            Log.d(TAG, "onActivityResult: " + path);

            /* Encode the current Image into a Base64 String */
            encodeBitmapToBase64String();

            /* Send the image to the server */
            new UploadImage().execute();

            /* Show the preview to the user */
            startActivity(preview);
        }

    }

    /* Generates file name using current time stamp
     * Format: MIEDA_yyyyMMdd_HHmmss.jpg */
    protected void generateFileName() {
        String prefix = "MIEDA_";
        String extension = ".jpg";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        FILE_NAME = prefix + timeStamp + extension;
    }

    /* Convert the captures image into a Base64 String */
    protected void encodeBitmapToBase64String() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(PATH);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] b = byteStream.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        ENCODED_IMAGE = encodedImage;
    }


    /* Upload image to server and get back the analysed data */
    private class UploadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /* Create a new JSON Object */
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imageBase64", ENCODED_IMAGE);

                /* Convert JSON data to String */
                String dataToSend = jsonObject.toString();

                /* Server URL */
                URL url = new URL(SERVER_URL);

                /* Create an Http connection */
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                /* Set parameters for Http connection */
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST"); // We'll be using POST
                connection.setFixedLengthStreamingMode(dataToSend.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                // Connect to the server
                connection.connect();

                Log.d(TAG, "doInBackground: " + dataToSend);

                /* Send Data to Server */
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(dataToSend); // Send data to the server
                wr.flush();
                wr.close();


                /* Read Data from Server */
                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                String result = sb.toString();
                Log.d(TAG, "Response: " + result);

                /* Disconnect from server */
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_MULTIPLE_PERMISSIONS: {
                boolean isOk = true;
                if(grantResults.length > 0) {
                    for(int i = 0; i < permissions.length; i++) {
                        int permissionGrantResults = grantResults[i];
                        if(!(permissionGrantResults == PackageManager.PERMISSION_GRANTED)) {
                            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                            if(!showRationale) {
                                // User denied permission and checked "Never Ask Again"
                                /* @TODO:
                                 * explain the user why this permission is important and
                                 * redirect the user to the App settings page */
                            } else if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
                                // Explain the user why the permission is important and re ask
                            } else if(Manifest.permission.CAMERA.equals(permissions[i])) {
                                // Explain the user why the permission is important and re ask
                            } else if(Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                                // Explain the user why the permission is important and re ask
                            }
                            /* @TODO: create a global variable to handle all permissions */

                            /* Meanwhile use this */
                            isOk = false;
                        }
                    }
                    if(!isOk) {
                        askPermission();
                    }
                }
            }
        }
    }

    protected void askPermission() {
        final ArrayList<String> permissionsList = new ArrayList<String>();
        addPermissions(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermissions(permissionsList, Manifest.permission.CAMERA);
        addPermissions(permissionsList, Manifest.permission.RECORD_AUDIO);

        if(permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_MULTIPLE_PERMISSIONS);
        }
    }

    private void addPermissions(ArrayList<String> permissionsList, String permission) {
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }
}
