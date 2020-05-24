package com.example.trakid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, Marker>mMarkers=new HashMap<>();
    LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        onLocationChange();
    }

    private void onLocationChange(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("track");

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



        double lat=Double.parseDouble(spliterLat[1]);
        double lon=Double.parseDouble(spliterlong[1]);
        String speed=spliterspeed[1];
        String devis=spliterdevice[1];

        currentLocation=new LatLng(lat,lon);

        if(!mMarkers.containsKey(key)){
            mMarkers.put(key,mMap.addMarker(new MarkerOptions().title(devis).position(currentLocation)));

        }else{
            mMarkers.get(key).setPosition(currentLocation);
        }

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        for(Marker m: mMarkers.values()){
            builder.include(m.getPosition());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),300));




    }

}
