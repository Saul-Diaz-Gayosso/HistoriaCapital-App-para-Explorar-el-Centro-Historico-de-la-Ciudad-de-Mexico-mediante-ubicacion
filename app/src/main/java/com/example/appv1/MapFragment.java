package com.example.appv1;

import static com.example.appv1.Constants.MAPVIEW_BUNDLE_KEY;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.PatternItem;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback,  GoogleMap.OnPolylineClickListener
{
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private ArrayList<MyItem> mMyItems = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    private float zoomLevel = 15.5f;
    private boolean isFollowingUser = true;
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolylineData = new ArrayList<>();
    private Marker selectedMarker;
    private boolean isRouteActive = false;


    public MapFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_homepage, container, false);


        mMapView = view.findViewById(R.id.places_list_map);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key))
                    .build();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        adjustMapViewHeight();
        startLocationUpdates();
        return view;
    }


    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "ejecuccion: rutas resultado: " + result.routes.length);
                if (mPolylineData.size()>0){
                    for (PolylineData polylineData: mPolylineData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }
                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "ejecuccion: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }

                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.blue));
                    polyline.setClickable(true);
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));
                }
            }
        });
    }

    protected void calculateDirections() {
        if (isRouteActive) {
            clearPolylines(); // Método para borrar la ruta en tu MapFragment
            isRouteActive = false;
            return;
        }

        // Código para calcular las direcciones y pintar las polilíneas
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                selectedMarker.getPosition().latitude,
                selectedMarker.getPosition().longitude
        );
        if (selectedMarker != null) {
            selectedMarker.remove();
        }
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(true);
        startLocationUpdates();
        directions.origin(new com.google.maps.model.LatLng(latitude, longitude));
        Log.d(TAG, "calculateDirections: origin latitude: " + latitude + " longitude: " + longitude);
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
                isRouteActive = true; // Establecer ruta activa después de pintar las polilíneas
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Fallo obteniendo la direccion: " + e.getMessage());

            }
        });
    }

    private void adjustMapViewHeight() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        float scale = displayMetrics.density;
        int paddingInPx = (int) (183 * scale + 0.5f);
        int newHeight = screenHeight - paddingInPx;
        ViewGroup.LayoutParams params = mMapView.getLayoutParams();
        params.height = newHeight;
        mMapView.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        if (mGoogleMap != null) {
            CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("latitude", (float) cameraPosition.target.latitude);
            editor.putFloat("longitude", (float) cameraPosition.target.longitude);
            editor.putFloat("zoom", cameraPosition.zoom);
            editor.apply();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        mGoogleMap.setOnPolylineClickListener(this);
        defineCentroHistorico();
        agregarMarcadores();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        float savedLatitude = sharedPreferences.getFloat("latitude", (float) 19.432608);
        float savedLongitude = sharedPreferences.getFloat("longitude", (float) -99.133209);
        float savedZoom = sharedPreferences.getFloat("zoom", 15.5f);
        LatLng savedLatLng = new LatLng(savedLatitude, savedLongitude);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(savedLatLng)
                .zoom(savedZoom)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isFollowingUser = false;
                }
            }
        });

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                isFollowingUser = true;
                centrarMapa(false);
                return true;
            }
        });

        startLocationUpdates();
    }


    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d("MapFragment", "Latitud: " + latitude + ", Longitud: " + longitude);
                        LatLng ubicacionActual = new LatLng(latitude, longitude);

                        if (isFollowingUser && mGoogleMap.getCameraPosition().zoom == zoomLevel) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(ubicacionActual)
                                    .zoom(zoomLevel)
                                    .build();
                            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
            }
        }
    };

    protected void agregarMarcadores() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null) {
                        Log.e(TAG, "Context is null, cannot initialize ClusterManager");
                        return;
                    }
                    ClusterManager<ClusterItem> clusterManager = new ClusterManager<>(getContext(), mGoogleMap);
                    mGoogleMap.setOnCameraIdleListener(clusterManager);
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long sitioInteresId = document.getLong("sitioInteresId");
                        String nombre = document.getString("nombre");
                        String latitudS = document.getString("latitud");
                        double latitud = Double.parseDouble(latitudS);
                        String longitudS = document.getString("longitud");
                        double longitud = Double.parseDouble(longitudS);
                        double score = document.getDouble("score");
                        int total_opinions = document.getLong("total_opinions").intValue();
                        String imgUri = document.getString("img");
                        String imgUrl = convertToAuthenticatedUrl(imgUri);

                        String telefono = document.getString("telefono");
                        String precio = document.getString("precio");
                        String link_sitio = document.getString("link_sitio");
                        String link_compra = document.getString("link_compra");
                        String horario_disp_L = document.getString("horario_disp_L");
                        String horario_disp_M = document.getString("horario_disp_M");
                        String horario_disp_Mi = document.getString("horario_disp_Mi");
                        String horario_disp_J = document.getString("horario_disp_J");
                        String horario_disp_V = document.getString("horario_disp_V");
                        String horario_disp_S = document.getString("horario_disp_S");
                        String horario_disp_D = document.getString("horario_disp_D");
                        String desc_ubi = document.getString("desc_ubi");
                        String descripcion = document.getString("descripcion");
                        String audio = document.getString("audio");


                        LatLng ubicacion = new LatLng(latitud, longitud);
                        MyClusterItem clusterItem = new MyClusterItem(ubicacion, nombre, score,
                                total_opinions, imgUrl, telefono, precio, link_sitio, link_compra,
                                horario_disp_L, horario_disp_M, horario_disp_Mi, horario_disp_J,
                                horario_disp_V, horario_disp_S, horario_disp_D, desc_ubi, descripcion, audio, sitioInteresId);
                        clusterManager.addItem(clusterItem);
                    }

                    clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterItem>() {
                        @Override
                        public boolean onClusterItemClick(ClusterItem item) {
                            showBottomSheet((MyClusterItem) item);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(item.getPosition())
                                    .title(item.getTitle());

                            // Añadir el marker temporal al mapa
                            selectedMarker = mGoogleMap.addMarker(markerOptions);
                            return false;
                        }
                    });
                    clusterManager.cluster();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "MapFrag: error in fetching data");
                    showRetryDialog();
                });
    }

    private String convertToAuthenticatedUrl(String gsutilUri) {
        if (gsutilUri.startsWith("gs://")) {
            return gsutilUri.replace("gs://", "https://storage.googleapis.com/");
        } else {
            return null;
        }
    }

    private void showRetryDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage("No se pudo obtener la información. ¿Deseas intentar nuevamente?")
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        agregarMarcadores();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    protected void defineCentroHistorico() {
        LatLng latLng1 = new LatLng(19.44088465601894, -99.14240599140417);
        LatLng latLng2 = new LatLng(19.43683632580631, -99.14720492379946);
        LatLng latLng3 = new LatLng(19.427008669210558, -99.14907802684539);
        LatLng latLng4 = new LatLng(19.425381332443592, -99.1257447768006);
        LatLng latLng5 = new LatLng(19.440262280065888, -99.12344000648814);
        PolygonOptions polygonOptions = new PolygonOptions()
                .add(latLng1)
                .add(latLng2)
                .add(latLng3)
                .add(latLng4)
                .add(latLng5)
                .strokeColor(ContextCompat.getColor(getContext(), R.color.deepgray))
                .fillColor(Color.TRANSPARENT);
        Polygon polygon = mGoogleMap.addPolygon(polygonOptions);
    }

    private void showBottomSheet(MyClusterItem item) {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(item);
        bottomSheetFragment.show(getParentFragmentManager(), "ModalBottonSheet");
    }

    private void centrarMapa(boolean animate) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("MapFrag", "Latitud: " + latitude + ", Longitud: " + longitude);
                            LatLng ubicacionActual = new LatLng(latitude, longitude);

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(ubicacionActual)
                                    .zoom(15.5f)
                                    .build();

                            if (animate) {
                                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            } else {
                                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        }
                    }
                });
    }

    protected void centrarMapa2() {
        if (checkPermission()) {
            return;
        }
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            float zoomLevel = 19.0f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel);
            mGoogleMap.animateCamera(cameraUpdate);
        } else
            Toast.makeText(getContext(), "No se pudo obtener la ubicación actual del usuario", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public void clearPolylines() {
        if (mPolylineData.size() > 0) {
            for (PolylineData polylineData : mPolylineData) {
                polylineData.getPolyline().remove();
            }
            mPolylineData.clear();
        }

        // Reinicia cualquier otra variable que sea necesaria para trazar nuevas rutas
        selectedMarker = null;
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void showMapView() {
        if (mMapView != null) {
            mMapView.setVisibility(View.VISIBLE);
        }
    }

    public void hideMapView() {
        if (mMapView != null) {
            mMapView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    //todos los click a una polyline van a ser interceptados aquí
    @Override
    public void onPolylineClick(Polyline polyline) {
        int index = 0;
        for(PolylineData polylineData: mPolylineData){
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.deepblue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lat
                );
                 selectedMarker = mGoogleMap.addMarker((new MarkerOptions()
                        .position(endLocation)
                        .title("Trip: #" + index)
                         .snippet("Duration: " + polylineData.getLeg().duration)
                ));
                selectedMarker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}
