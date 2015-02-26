/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zeus.example.com.mapsexamplegoogle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This shows how to create a simple activity with a map and a marker on the map. R.id.map
 */
public class BasicMapDemoActivity extends FragmentActivity implements LocationListener, LocationSource {

    private static final String TAG = BasicMapDemoActivity.class.getSimpleName();
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    private OnLocationChangedListener mListener;
    private LocationManager locationManager;

    LatLng cameraLatLng;

    List<LatLng> routePoints;


    PolylineOptions route;

    Polyline linea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_demo);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null) {
            boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gpsIsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
                Toast.makeText(this, "GPS ACTIVO", Toast.LENGTH_SHORT).show();
            } else if (networkIsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
                Toast.makeText(this, "PROVEEDOR DE RED ACTIVO", Toast.LENGTH_SHORT).show();
            } else {
                //Show an error dialog that GPS is disabled...
            }
        } else {
            //Show some generic error dialog because something must have gone wrong with location manager.
        }

        setUpMapIfNeeded();


    }

    @Override
    public void onPause() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();

        if (locationManager != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p/>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.

            if (mMap != null) {
                setUpMap();
            }

            //This is how you register the LocationSource
            mMap.setLocationSource(this);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        routePoints = new ArrayList<LatLng>();
        initTraceRoute();

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Entrando a la funcion onLocationChange");
        if (mListener != null) {
            mListener.onLocationChanged(location);

            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;


            if (!bounds.contains(new LatLng(location.getLatitude(), location.getLongitude()))) {
                cameraLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                //Move the camera to the user's location once it's available!
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng, 16));

            }
            drawLineOnMap(cameraLatLng);
        }
    }
/*
            @Override
    public void onLocationChanged(Location location) {
        if (mListener != null) {
            mListener.onLocationChanged(location);

            cameraLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng, 16));
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

            //mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
        }
    }
*/

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "provider disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "provider enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "status changed", Toast.LENGTH_SHORT).show();
    }


    public void drawLineOnMap(LatLng latLng){

        Log.d(TAG, "Entrando a la función drawLineOnMap...");


        if( latLng != null ){
            routePoints.add(new LatLng( mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude() ));

            route = new PolylineOptions().width(10).color(Color.BLUE).addAll(routePoints);

            //linea = mMap.addPolyline(route);
            linea.setPoints( routePoints );


            Log.d(TAG, "La longitud de la linea: "+linea.getPoints().size() );

            if( linea.isVisible() ){
                Log.d(TAG, "la linea es visible");
            }else{
                Log.d(TAG, "la linea NOOOOOOO es visible");
            }

            Log.d(TAG, "Longitud Posiciones: " + route.getPoints().size() );
            Toast.makeText(this, "Longitud Posiciones: " + route.getPoints().size(), Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(this, "Latlang es nulo ", Toast.LENGTH_LONG).show();
        }


        for( LatLng point: routePoints ){
            Log.d(TAG, point.toString());
        }
/*

        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(19.3360093, -99.1929361))
                .add(new LatLng(19.3360093, -99.1929361))  // North of the previous point, but at the same longitude
                .add(new LatLng(19.3360093, -99.1929361));  // Same latitude, and 30km to the west
        rectOptions.color(Color.BLUE);
        rectOptions.width(7);
        rectOptions.geodesic(true);
        rectOptions.zIndex(21);
*/





/*
        mMap.addPolyline(new PolylineOptions()
                .add(c1, c2).width(7).color(Color.BLUE));

        mMap.addPolyline(new PolylineOptions()
                .add(c2, c3).width(7).color(Color.RED));

        mMap.addPolyline(new PolylineOptions()
                .add(c3, c1).width(7).color(Color.GREEN));
*/
        // Get back the mutable Polyline
        //Polyline polyline = mMap.addPolyline(rectOptions);
/*
        Polyline route = mMap.addPolyline(new PolylineOptions()
                .width(7)
                .color(Color.BLUE)
                .geodesic(true)
                .zIndex(21));
        route.setPoints(routePoints);

*/
    }

    private void initTraceRoute(){

        LatLng c1 = new LatLng(19.3360093, -99.1929361);
        LatLng c2 = new LatLng(19.3460093, -99.1929361);

       route = new PolylineOptions().add(c1,c2).width(7).color(Color.BLUE);
       linea = mMap.addPolyline(route);

    }

}
