package com.example.appv1;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Community extends AppCompatActivity {

    private ImageView btnHome;
    private ImageView btnUser;
    private ImageView btnSavePlaces;
    private ImageView toggleImg;
    private ImageView btnPlus;
    private ImageView btnClose;
    private RelativeLayout mapContainer;
    private LinearLayout contentCentral;
    private FrameLayout fragmentContainer;
    private ImageView bannerArriba;
    private ImageView bannerAbajo;
    private LinearLayout linearLayout;
    private LinearLayout linearLayout2;
    private RecyclerView recyclerView;
    private OpinionAdapter adapter;
    private  List<Opinion> opinionList;
    private FirebaseFirestore db;
    private DatabaseReference dbRef;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private NavigationView navView;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community); //Recordar cambiar al layout correspondiente
        mapContainer = findViewById(R.id.map_container);
        contentCentral = findViewById(R.id.contentCentral);
        fragmentContainer = findViewById(R.id.fragment_container);
        bannerArriba = findViewById(R.id.banner_arriba);
        bannerAbajo = findViewById(R.id.banner_abajo);
        linearLayout = findViewById(R.id.linearLayout);
        linearLayout2 = findViewById(R.id.linearLayout2);



        //COMIENZA CONFIGURACION DEL RECYCLEVIEW PARA LAS OPINIONES

        //encuentra la vista RecyclerView en el diseño inflado
        recyclerView = findViewById(R.id.recyclerView);
        opinionList = new ArrayList<>();
        //nueva instancia del adapter, el adapter es el responsable de tomar los datos y vincularlos a las vistas en el RecyclerView
        adapter = new OpinionAdapter(this, opinionList);

        //establece el layout manager del recyclerView en un linearLayoutManager, es decir los elementos se disponen en una lista vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //asigna el adaptador al recyclerView, esto concecta el recyclerView con el adaptador para que pueda mostrar los datos
        recyclerView.setAdapter(adapter);
        //obtener una instancia de FirebaseFirestore
        db = FirebaseFirestore.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("usuarios");
        //metodo loadOpinions para cargar opiniones desde Firestore y mostrarlas
        loadOpinions();
        //Añade un listener al RecyclerView para detectar cuando el usuario llega al final de la lista
        //si no se están cargando datos y no es la última página, carga más datos desde Firestore.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadOpinions(); //cargar opiniones
                    }
                }
            }
        });

        //TERMINA CONFIGURACION DEL RECYCLEVIEW

        toggleImg = findViewById(R.id.xiv);
        navView = findViewById(R.id.navigation_view); // Inicializar navView

        //BOTONES
        btnPlus = findViewById(R.id.btnPlus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOpinionFragmet();
            }
        });

        btnHome = findViewById(R.id.homeIcon);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Community.this, Homepage.class);
                startActivity(intent);
                finish();
            }
        });

        btnUser = findViewById(R.id.userIcon);
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Community.this, Perfil.class); // Reemplaza "Perfil.class" con el nombre de tu segunda actividad
                startActivity(intent);
                finish();
            }
        });

        btnSavePlaces = findViewById(R.id.saveIcon);
        btnSavePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Community.this, SavePlaces.class); // Reemplaza "Perfil.class" con el nombre de tu segunda actividad
                startActivity(intent);
                finish();
            }
        });


        RelativeLayout mainLayout = findViewById(R.id.map_container); // Reemplaza R.id.map_container con el ID de tu RelativeLayout principal
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (navView.getVisibility() == View.VISIBLE) {
                        Rect outRect = new Rect();
                        navView.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            // Si el evento táctil ocurrió fuera del área del NavigationView, cierra el menú
                            navView.setVisibility(View.GONE);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        toggleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navView.getVisibility() == View.VISIBLE)
                    navView.setVisibility(View.GONE);
                else
                    navView.setVisibility(View.VISIBLE);
            }
        });

        // Configurar el listener para el NavigationView
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_item_salir) {
                // Cerrar sesión en Firebase
                // Cerrar sesión en Firebase

                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, false);
                editor.apply();

                Intent intent = new Intent(Community.this, InicioSesion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_item_soporte){
                Intent intent = new Intent(Community.this, Soporte.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_item_ultimo){
                Intent intent = new Intent(Community.this, News.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadOpinions() {
        isLoading = true;
        Log.d(TAG, "Community:Dentro de la funcion loadOpinions de Community");
        //Consulta a la base de datos
        Query query = db.collection("Opinions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        //Ejecucuion de la consulta
        query.get().addOnCompleteListener(task -> {
            Log.d(TAG, "Community: Consulta completada");
            // Procesamiento de los Resultados
            if (task.isSuccessful()) {
                List<Opinion> newOpinions = new ArrayList<>();
                Log.d(TAG, "Community: antes de entrar al for");
                for (DocumentSnapshot document : task.getResult()) {
                    String ItemID = document.getString("ItemID");
                    String imageUri = document.getString("imagen");
                    String imgUrl = "";
                    if (imageUri != null && !imageUri.isEmpty()) {
                        imgUrl = convertToAuthenticatedUrl(imageUri);
                    }
                    Opinion opinion = document.toObject(Opinion.class);
                    if (opinion != null) {
                        opinion.setImagen(imgUrl);
                        getItemByID(ItemID, opinion);
                        newOpinions.add(opinion);
                        Log.d(TAG, "Opinion cargada: " + opinion.toString());
                    }
                }
                // se determina si se ha alcanzado la última página
                if (newOpinions.size() < 10) {
                    isLastPage = true;
                }
                // se actualiza la última página
                if (!newOpinions.isEmpty()) {
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
                // se añaden las nuevas opiniones al adaptador
                adapter.addOpinions(newOpinions);
                Log.d(TAG, "Nuevas opiniones añadidas: " + newOpinions.size());
            } else {
                // Manejo del error
                Log.d(TAG, "Community: Error en la consulta de las opiniones: ", task.getException());
            }
            isLoading = false;
        });
    }


    private String convertToAuthenticatedUrl(String gsutilUri) {
        if (gsutilUri == null || gsutilUri.isEmpty()) {
            return ""; // Devuelve una cadena vacía o un valor predeterminado según tu necesidad
        }

        // Convert gs://bucket-name/object-name to https://storage.cloud.google.com/bucket-name/object-name
        if (gsutilUri.startsWith("gs://")) {
            return gsutilUri.replace("gs://", "https://storage.googleapis.com/");
        } else {
            return gsutilUri; // Devuelve la misma cadena si no comienza con "gs://"
        }
    }


    private void showAddOpinionFragmet() {
        AddOpinionFragment addOpinionFragment = new AddOpinionFragment();
        // Ocultar el contenido central
        contentCentral.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        bannerAbajo.setVisibility(View.GONE);

        // Mostrar el contenedor del fragmento
        fragmentContainer.setVisibility(View.VISIBLE);

        // Transacción del fragmento con animación
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.fragment_container, addOpinionFragment)
                .addToBackStack(null)
                .commit();
    }

    private void getItemByID(String sitioInteresId, Opinion opinion) {
        Log.d(TAG, "getItemByID sitioInteresId: " + sitioInteresId);

        // Asumiendo que el ID del documento es el mismo que `sitioInteresId`
        DocumentReference docRef = db.collection("Items").document(sitioInteresId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Obtener los datos del documento
                        String nombre = document.getString("nombre");
                        // Actualizar la opinión con los datos obtenidos
                        if (nombre != null) opinion.setTitle(nombre);
                        // Notificar al adaptador sobre el cambio de datos
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void showCentralContent() {
        contentCentral.setVisibility(View.VISIBLE);
        mapContainer.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);
        bannerAbajo.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AddOpinionFragment) {
            ((AddOpinionFragment) currentFragment).handleOnBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
