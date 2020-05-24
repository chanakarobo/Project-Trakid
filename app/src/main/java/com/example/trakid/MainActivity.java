package com.example.trakid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_UID = "uid";
    TextView userNameText;
    TextView userMailText;
    NavigationView navigationView;
    ImageView imageView;
    SignInActivity logoutObject;
    private GoogleMap mMap;
    View  mMapView;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    LatLng currentLocation;
    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location mLastKnownLocation;
    double lat,lon,myLat,myLon;
    List<ChildObject> childList;
    ArrayList<String>items=new ArrayList<>();
    SpinnerDialog spinnerChild;
    String paircode,childname;
    String uid;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=101;
    boolean mLocationPermissionGranted;
    ConnectivityManager connectivityManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        TextView getchildlist=findViewById(R.id.get_children);
        getchildlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectivityManager.getActiveNetworkInfo()!=null){
                    if(!items.isEmpty()) {
                        spinnerChild.showSpinerDialog();
                    }else{
                        showWebView();
                    }

                }else{
                    showAlert();
                }

            }
        });

//        showWebView();
        logoutObject = new SignInActivity();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final LinearLayout holder = findViewById(R.id.holder);



        FloatingActionButton mylocation = findViewById(R.id.floatingActionButton);
        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)throws NullPointerException {
                try{
                    if(connectivityManager.getActiveNetworkInfo()!=null) {
                        onLocationChange();
                    }else{
                        showAlert();
                    }
                }catch (NullPointerException ex){
                    Toast.makeText(getApplicationContext(), "please select your child first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        String username = (String) getIntent().getExtras().get(EXTRA_USERNAME);
        String email = (String) getIntent().getExtras().get(EXTRA_EMAIL);
        String url = (String) getIntent().getExtras().get(EXTRA_URL);
        uid = (String) getIntent().getExtras().get(EXTRA_UID);
//        Log.e("user name is : ", url);

        navigationView = findViewById(R.id.nav_view);

        View hview = navigationView.inflateHeaderView(R.layout.nav_header_main);

        userNameText = hview.findViewById(R.id.txt_username);
        userMailText = hview.findViewById(R.id.txt_email);
        imageView = hview.findViewById(R.id.profile_image);


        userNameText.setText(username);
        userMailText.setText(email);

        Glide.with(this).load(url).into(imageView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                float scaleFactor = 7f;
                float slideX = drawerView.getWidth() * slideOffset;

                holder.setTranslationX(slideX);
                holder.setScaleX(1 - (slideOffset / scaleFactor));
                holder.setScaleY(1 - (slideOffset / scaleFactor));

                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);// will remove all possible our aactivity's window bounds
        }

        drawer.addDrawerListener(toggle);

        drawer.setScrimColor(Color.TRANSPARENT);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        getChildren();

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            mLastKnownLocation = (Location) task.getResult();
                            myLat=mLastKnownLocation.getLatitude();
                            myLon=mLastKnownLocation.getLongitude();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 300));
                        } else {
//                            Log.e("Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.notification) {
            // Handle the camera action
        } else if (id == R.id.help) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.logout) {
            logoutObject.signOut();
            startActivity(new Intent(this, SignInActivity.class));

        } else if (id == R.id.nav_share) {
            if(lat!=0  && lon!=0){
                sharemap();
            }else{
                Toast.makeText(getApplicationContext(), "please select your child first", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMaxZoomPreference(15);
//        onLocationChange();

        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(30, 0, 0, 200);

        getDeviceLocation();
        updateLocationUI();


    }

    private void onLocationChange(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("location").child(paircode);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mMap.clear();
                setmMarkers(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setmMarkers(DataSnapshot dataSnapshot){

        String key=dataSnapshot.getKey();
        HashMap<String,Object>value=(HashMap<String, Object>)dataSnapshot.getValue();

        String latitude= (String) value.get("lat");
        String [] spliterLat=latitude.split("=",2);

        String longtiude= (String) value.get("lon");
        String [] spliterlong=longtiude.split("=",2);

        String speeds= (String) value.get("speed");
        String [] spliterspeed=speeds.split("=",2);

        String device= (String) value.get("devid");
        String [] spliterdevice=device.split("=",2);



     lat=Double.parseDouble(spliterLat[1]);
     lon=Double.parseDouble(spliterlong[1]);
        String speed=spliterspeed[1];
        String devis=spliterdevice[1];

        currentLocation=new LatLng(lat,lon);

        if(!mMarkers.containsKey(key)){
            mMarkers.put(key,mMap.addMarker(new MarkerOptions().title(devis).icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_emoji)).position(currentLocation)));

        }else{
            mMarkers.get(key).setPosition(currentLocation);
            mMarkers.put(key,mMap.addMarker(new MarkerOptions().title(devis).icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_emoji)).position(currentLocation)));
        }

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        for(Marker m: mMarkers.values()){
            builder.include(m.getPosition());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),300));

    }


    public void getDistance(View view){

        Location locationA = new Location("point A");
        locationA.setLatitude(lat);
        locationA.setLongitude(lon);

        Location locationB = new Location("point B");

        locationB.setLatitude(myLat);
        locationB.setLongitude(myLon);

        float distance = locationA.distanceTo(locationB)/100000;

        TextView textDistance=findViewById(R.id.get_distance);
        textDistance.setText("Air Distance : "+String.format("%.2f",distance)+" km");

        Log.e("distance from a to b", String.valueOf(distance));

    }

    public void getChildren(){


        childList=new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UserDevices");
        DatabaseReference myRef1 = myRef.child(uid);



           myRef1.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                   for (DataSnapshot chlidSnapshot : dataSnapshot.getChildren()) {
                       HashMap<String, Object> value = (HashMap<String, Object>) chlidSnapshot.getValue();
//                       ChildObject childObject = chlidSnapshot.getValue(ChildObject.class);

                       Log.e("datasnapshot of : ", chlidSnapshot.getKey());
                       Log.e("datasnapshot of : ", (String) value.get("label"));

                       items.add((String) value.get("label") + "\n" + (String) value.get("paircode"));


                   }

               }


               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });



         spinnerChild = new SpinnerDialog(this, items, "Select Employee", "Close");
         spinnerChild = new SpinnerDialog(this, items, "Select Employee", R.style.DialogAnimations_SmileWindow, "Close");

         spinnerChild.setCancellable(true);
         spinnerChild.setShowKeyboard(false);

        spinnerChild.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                String[]parts=item.split("\n",2);
                paircode=parts[1];
                childname=parts[0];

                TextView getchildlist=findViewById(R.id.get_children);
                getchildlist.setText(childname);


                if(paircode!=null){
                    onLocationChange();
                }

                Log.e("pair code is : ",paircode);
            }
        });

    }


    //show alert when internet connection is not avilable
    public void showAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Internet connection...");
        alertDialog.setMessage("Your network connection is lost..!");
        alertDialog.setIcon(R.drawable.wifi_signal);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });

        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    //show our web page when without any childs
    public void showWebView(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        WebView wv=new WebView(this);
        wv.loadUrl("https://fourssh.net/pixel");
        wv.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                view.requestFocus();
                view.getSettings().setLightTouchEnabled(true);
                return true;
            }
        });

        alertDialog.setTitle("There is currently not available any chlid");
        alertDialog.setMessage(Html.fromHtml("their no any child,<a href=\"https://fourssh.net/pixel\">Clink hear to..</a>"));
        alertDialog.setIcon(R.drawable.wifi_signal);
        alertDialog.setView(wv);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        alertDialog.setCanceledOnTouchOutside(false);
    }
//pass latitude and longtitude to other travelling apps
    public void sharemap(){

        String uri = "geo:" + lat + ","
                +lon + "?q=" + lat
                + "," + lon;
        startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(uri)));
    }




}



