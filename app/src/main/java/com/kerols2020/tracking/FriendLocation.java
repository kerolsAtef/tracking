package com.kerols2020.tracking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class FriendLocation extends FragmentActivity implements OnMapReadyCallback {


    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
/*
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionsGranted = false;


    private FusedLocationProviderClient mFusedLocationProviderClient;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_location);

        initMap();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

     auth=FirebaseAuth.getInstance();
     reference=database.getInstance().getReference().child("user").child(auth.getCurrentUser().getUid());

       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               double lat=(double)dataSnapshot.child("latitude").getValue();
               double lon=(double)dataSnapshot.child("longitude").getValue();
               LatLng l=new LatLng(lat,lon);
               moveCamera(lat,lon,15f,googleMap);
               googleMap.setMyLocationEnabled(true);
               googleMap.getUiSettings().setMyLocationButtonEnabled(true);
               googleMap.addMarker(new MarkerOptions().position(l).title("your last friend's location updated"));
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

        }



    private void moveCamera(double LAT,double LON, float zoom,GoogleMap mMap){
       LatLng latLng= new LatLng(LAT,LON);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,
                zoom);
        mMap.animateCamera(update);
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(FriendLocation.this);
    }


}
