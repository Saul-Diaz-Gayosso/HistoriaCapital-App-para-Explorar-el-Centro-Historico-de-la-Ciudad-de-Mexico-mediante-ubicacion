package com.example.appv1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// SavePlaces.java
public class SavePlaces extends AppCompatActivity {

    ImageView btnHome;
    ImageView btnUser;
    ImageView btnCommunity;
    ImageView toggleImg;
    private NavigationView navView; // Declarar navView aquí

    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private LinearLayout mainLayout;
    private List<View> itemViewList; // Lista para mantener referencia a las vistas creadas dinámicamente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_places);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        itemViewList = new ArrayList<>();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("favorites");
            firestore = FirebaseFirestore.getInstance();

            getFavoritePlaces();
        } else {
            // Manejar el caso donde el usuario no está autenticado
        }

        // Inicializar botones
        toggleImg = findViewById(R.id.xiv);
        btnHome = findViewById(R.id.homeIcon);
        btnCommunity = findViewById(R.id.plusIcon);
        btnUser = findViewById(R.id.userIcon);
        mainLayout = findViewById(R.id.mainLayout);

        // Configurar botones de navegación
        toggleImg.setOnClickListener(v -> {
            NavigationView navView = findViewById(R.id.navigation_view);
            if (navView.getVisibility() == View.VISIBLE) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SavePlaces.this, Homepage.class);
            startActivity(intent);
            finish();
        });

        btnCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(SavePlaces.this, Community.class);
            startActivity(intent);
            finish();
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(SavePlaces.this, Perfil.class);
            startActivity(intent);
            finish();
        });

        navView = findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_item_salir) {
                FirebaseAuth.getInstance().signOut(); // Cerrar sesión en Firebase

                // Cerrar sesión y reiniciar la actividad
                SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Intent intent = new Intent(SavePlaces.this, InicioSesion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_item_soporte) {
                Intent intent = new Intent(SavePlaces.this, Soporte.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_item_ultimo) {
                Intent intent = new Intent(SavePlaces.this, News.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }

            return false;
        });

    }

    private void getFavoritePlaces() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favoriteIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Boolean isFavorite = snapshot.getValue(Boolean.class);
                    if (isFavorite != null && isFavorite) {
                        favoriteIds.add(snapshot.getKey());
                    }
                }
                fetchFavoriteItemsFromFirestore(favoriteIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar el error
            }
        });
    }

    private void fetchFavoriteItemsFromFirestore(List<String> favoriteIds) {
        if (firebaseUser == null) return; // Chequear si el usuario está autenticado

        // Limpiar la lista de vistas antes de volver a cargar
        mainLayout.removeAllViews();
        itemViewList.clear();

        for (String sitioInteresId : favoriteIds) {
            DocumentReference docRef = firestore.collection("Items").document(sitioInteresId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String title = document.getString("nombre");
                        String description = document.getString("descripcion");
                        String imgPath = document.getString("img");
                        String telefono = document.getString("telefono");
                        String link_compra = document.getString("link_compra");
                        String link_sitio = document.getString("link_sitio");
                        String horario_disp_L = document.getString("horario_disp_L");
                        String horario_disp_M = document.getString("horario_disp_M");
                        String horario_disp_Mi = document.getString("horario_disp_Mi");
                        String horario_disp_J = document.getString("horario_disp_J");
                        String horario_disp_V = document.getString("horario_disp_V");
                        String horario_disp_S = document.getString("horario_disp_S");
                        String horario_disp_D = document.getString("horario_disp_D");
                        fetchImageUrlAndAddToLayout(title, description, imgPath, telefono, link_compra, link_sitio,
                                horario_disp_L, horario_disp_M, horario_disp_Mi, horario_disp_J, horario_disp_V,
                                horario_disp_S,horario_disp_D, sitioInteresId);
                    }
                } else {
                    // Manejar el error
                }
            });
        }
    }

    private void fetchImageUrlAndAddToLayout(String title, String description, String imgPath, String telefono, String  link_compra, String  link_sitio,
                                             String horario_disp_L, String horario_disp_M, String horario_disp_Mi, String horario_disp_J,
                                             String horario_disp_V, String horario_disp_S, String horario_disp_D, String sitioInteresId) {
        if (isDestroyed() || isFinishing()) return; // Chequear si la actividad está destruida

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(imgPath);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String imgUrl = uri.toString();
            runOnUiThread(() -> addItemToLayout(title, description, imgUrl, telefono, link_compra,link_sitio,
                    horario_disp_L, horario_disp_M, horario_disp_Mi, horario_disp_J, horario_disp_V,
                    horario_disp_S, horario_disp_D, sitioInteresId)); // Ejecutar en hilo principal
        }).addOnFailureListener(exception -> {
            // Manejar el error de obtener la URL de descarga
        });
    }

    private void addItemToLayout(String title, String description, String imgUrl, String telefono, String  link_compra, String  link_sitio,
                                 String horario_disp_L, String horario_disp_M, String horario_disp_Mi, String horario_disp_J,
                                 String horario_disp_V, String horario_disp_S, String horario_disp_D, String sitioInteresId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_guardado, null);

        TextView titleTextView = itemView.findViewById(R.id.titleTextView);
        TextView descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        TextView telefonoTextView = itemView.findViewById(R.id.telefonoSitio);
        ImageView imageView = itemView.findViewById(R.id.imageView);
        ImageView bookmark = itemView.findViewById(R.id.bookmark);
        TextView  link_sitioTextView = itemView.findViewById(R.id.link_sitio);
        TextView  horarioTextView = itemView.findViewById(R.id.horario);
        TextView link_compraTextView = itemView.findViewById(R.id.link_compra);
        LinearLayout itemContainer = itemView.findViewById(R.id.itemContainer);


        // Text Horario */FALTA COMPROBAR SI ESTA ABIERO O CERRADO/*
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String horario = "";

        // Using a switch-case statement with break statements inside to prevent overwriting the value
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                horario = horario_disp_L;
                break;
            case Calendar.TUESDAY:
                horario = horario_disp_M;
                break;
            case Calendar.WEDNESDAY:
                horario = horario_disp_Mi;
                break;
            case Calendar.THURSDAY:
                horario = horario_disp_J;
                break;
            case Calendar.FRIDAY:
                horario = horario_disp_V;
                break;
            case Calendar.SATURDAY:
                horario = horario_disp_S;
                break;
            case Calendar.SUNDAY:
                horario = horario_disp_D;
                break;
            default:
                horario = "Horario no disponible";
        }
        // asignar el horario al EditText
        horarioTextView.setText(horario);

        // Configurar el identificador único para el contenedor de la vista
        itemContainer.setId(View.generateViewId());

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        telefonoTextView.setText(telefono);
        link_compraTextView.setText(link_compra);
        link_sitioTextView.setText(link_sitio);

        telefonoTextView.setOnClickListener(v -> {
            // Obtener el número de teléfono del campo de texto
            String phoneNumber = telefonoTextView.getText().toString();

            // Crear un Intent con la acción de marcar (ACTION_DIAL)
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));

            // Mostrar el cuadro de diálogo de llamada para que el usuario decida si llama o no
            startActivity(dialIntent);
        });

        link_compraTextView.setOnClickListener(v -> {
            // Abre el navegador web cuando el usuario haga clic explícitamente
            abrirEnlaceWeb((link_compra));
        });

        link_sitioTextView.setOnClickListener(v -> {
            // Abre el navegador web cuando el usuario haga clic explícitamente
            abrirEnlaceWeb2((link_sitio));
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            descriptionTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        // Cargar la imagen usando Glide
        Glide.with(this).load(imgUrl).into(imageView);

        // Configurar el listener del bookmark
        bookmark.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar sitio favorito")
                    .setMessage("¿Deseas eliminar el sitio favorito?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Eliminar el sitio favorito
                        removeFavorite(sitioInteresId);
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Añadir la vista al layout principal
        mainLayout.addView(itemView); // Ejecutar en hilo principal


        // Agregar la vista a la lista para mantener la referencia
        itemViewList.add(itemView);
    }


    private void abrirEnlaceWeb(String url) {
        // Verificar si la URL no es nula y tiene un valor válido
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
        }
    }

    private void abrirEnlaceWeb2(String url) {
        // Verificar si la URL no es nula y tiene un valor válido
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
        }
    }

    private void removeFavorite(String sitioInteresId) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(userId)
                    .child("favorites")
                    .child(sitioInteresId);

            favRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SavePlaces.this, "Sitio eliminado de favoritos", Toast.LENGTH_SHORT).show();
                    // Lanza el nuevo intent después de eliminar el favorito
                    Intent intent = new Intent(SavePlaces.this, SavePlaces.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finaliza la actividad actual
                } else {
                    Toast.makeText(SavePlaces.this, "Error al eliminar el sitio", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SavePlaces.this, Homepage.class);
        startActivity(intent);
        finish();
    }
}
