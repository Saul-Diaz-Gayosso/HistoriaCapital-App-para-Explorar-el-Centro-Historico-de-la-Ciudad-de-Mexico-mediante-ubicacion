package com.example.appv1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class Perfil extends AppCompatActivity {

    ImageView btnArrowback;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    ImageView btnHome;
    ImageView btnSavePlaces;

    ImageView btnCommunity;

    Button btnChangeEmail;

    Button btnChangePassword;

    Button btnDeleteAccount;

    Button btnChangeName;
    ImageView toggleImg;
    private NavigationView navView; // Declarar navView aquí

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        toggleImg = findViewById(R.id.xiv);
        navView = findViewById(R.id.navigation_view); // Inicializar navView


        //BOTON QUE TE ENVIA A HOME PAGE
        btnHome = findViewById(R.id.homeIcon);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, Homepage.class);
                startActivity(intent);
                finish();
            }
        });

        btnCommunity = findViewById(R.id.plusIcon);
        btnCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, Community.class);
                startActivity(intent);
                finish();
            }
        });

        btnSavePlaces = findViewById(R.id.saveIcon);
        btnSavePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, SavePlaces.class);
                startActivity(intent);
                finish();
            }
        });

        btnChangeEmail = findViewById(R.id.BtnChangeEmail);
        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, UpdateNewEmail.class);
                startActivity(intent);
                finish();
            }
        });

        btnDeleteAccount = findViewById(R.id.BtnDeleteAccount);
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, DeleteAccount.class);
                startActivity(intent);
                finish();
            }
        });

        btnChangePassword = findViewById(R.id.BtnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, ChangePassword.class);
                startActivity(intent);
                finish();
            }
        });

        btnChangeName = findViewById(R.id.BtnSaveAnotherName);
        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perfil.this, ChangeName.class);
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

        // Otro código ...

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

                Intent intent = new Intent(Perfil.this, InicioSesion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_item_soporte){
                Intent intent = new Intent(Perfil.this, Soporte.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_item_ultimo){
                Intent intent = new Intent(Perfil.this, News.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Obtener instancia de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Obtener el nombre de usuario
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuarios").child(currentUser.getUid()).child("usuario");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String displayName = currentUser.getDisplayName();
                    String userName = dataSnapshot.getValue(String.class);

                    if (userName == null || userName.trim().isEmpty()) {
                        // Mostrar mensaje predeterminado si el nombre de usuario no se encuentra
                        mostrarMensajePredeterminado(displayName);
                    } else {
                        mostrarBienvenidaYNombreUsuario(displayName, userName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Manejar errores de base de datos si es necesario
                }
            });
        } else {
            mostrarMensajePredeterminado(null);
        }
    }

    private void mostrarBienvenidaYNombreUsuario(String displayName, String userName) {
        TextView textViewBienvenida = findViewById(R.id.textViewBienvenida);
        textViewBienvenida.setText("Bienvenido " + displayName);

        TextView textViewUsuario = findViewById(R.id.textViewUsuario);
        textViewUsuario.setText("Usuario: @" + userName);
    }

    private void mostrarMensajePredeterminado(String displayName) {
        TextView textViewBienvenida = findViewById(R.id.textViewBienvenida);
        if (displayName == null) {
            textViewBienvenida.setText("Bienvenido");
        } else {
            textViewBienvenida.setText("Bienvenido " + displayName);
        }

        TextView textViewUsuario = findViewById(R.id.textViewUsuario);
        textViewUsuario.setText("");
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Perfil.this, Homepage.class);
        startActivity(intent);
        finish();
    }
}
