package com.example.appv1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Homepage extends AppCompatActivity {
    private static final String TAG = "Homepage";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";;
    private ImageView toggleImg;
    private ImageView btnUser;
    private ImageView btnSavePlaces;
    private ImageView btnCommunity;
    private NavigationView navView;
    private FusedLocationProviderClient mFusedLocationClient;
    private MapFragment mapFragment;
    private MapView mapView;
    private TextView tvCancelRoute; // Agregar referencia al TextView

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        toggleImg = findViewById(R.id.xiv);
        btnUser = findViewById(R.id.userIcon);
        btnSavePlaces = findViewById(R.id.saveIcon);
        btnCommunity = findViewById(R.id.plusIcon);
        navView = findViewById(R.id.navigation_view);
        mapView = findViewById(R.id.places_list_map);
        tvCancelRoute = findViewById(R.id.tv_cancel_route); // Asignar referencia al TextView


        mapFragment = new MapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map_container, mapFragment, "TagFragemntoMapa");
        fragmentTransaction.commit();

        // Aquí configuras la lógica para mostrar/ocultar el TextView "Cancelar Ruta"

        TextView tvCancelRoute = findViewById(R.id.tv_cancel_route);
        tvCancelRoute.setVisibility(View.VISIBLE);
        tvCancelRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.clearPolylines(); // Limpia las polilíneas trazadas en MapFragment
                }
                tvCancelRoute.setVisibility(View.GONE); // Oculta el TextView de cancelar ruta
            }
        });

        // Configurar el listener para el ImageView que despliega el menú lateral
        toggleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navView.getVisibility() == View.VISIBLE) {
                    navView.setVisibility(View.GONE);
                } else {
                    navView.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (navView.getVisibility() == View.VISIBLE) {
                                navView.setVisibility(View.GONE);
                            }
                        }
                    }, 3000);
                }
            }
        });

        // Configurar el listener para el NavigationView
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_item_salir) {
                // Cerrar sesión
                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, false);
                editor.apply();
                Intent intent = new Intent(Homepage.this, InicioSesion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_item_soporte) {
                Intent intent = new Intent(Homepage.this, Soporte.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_item_ultimo) {
                Intent intent = new Intent(Homepage.this, News.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Configurar clics a los botones
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, Perfil.class);
                startActivity(intent);
                finish();
            }
        });

        btnSavePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, SavePlaces.class);
                startActivity(intent);
                finish();
            }
        });

        btnCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, Community.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        checkLocationProviderAvailability();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapFragment != null) {
            mapFragment.hideMapView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapFragment != null) {
            mapFragment.showMapView();
        }
    }

    private void checkLocationProviderAvailability() {
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGpsEnabled)
            Log.d(TAG, "ProvedorUbi:GPS Provider is enabled");
        else
            Log.d(TAG, "ProvedorUbi:GPS Provider is not enabled");
        if (isNetworkEnabled)
            Log.d(TAG, "ProvedorUbi:Network Provider is enabled");
        else
            Log.d(TAG, "ProvedorUbi:Network Provider is not enabled");
    }
}
