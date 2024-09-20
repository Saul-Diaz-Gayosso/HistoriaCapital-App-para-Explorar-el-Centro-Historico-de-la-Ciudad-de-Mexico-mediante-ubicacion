package com.example.appv1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InicioSesion extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_STORAGE = 126;
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private int currentPage = 0;
    private Handler handler;
    private Button SignUp;
    private Button SignIn;
    private FirebaseAuth mAuth;
    private EditText edtMail;
    private EditText edtPassword;
    private ImageView ivShowHidePassword;
    private boolean isPasswordVisible = false;

    private int failedLoginAttempts = 0; // Variable para contar intentos fallidos
    private TextView forgotPassword;
    private final int delay = 3000; // 3 segundos
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 125; // Código de solicitud de permisos
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private boolean mReadStoragePermissionGranted = false; // Estado del permiso para leer almacenamiento
    private boolean mWriteStoragePermissionGranted = false;


    private SharedPreferences sharedPreferences;
    public String saludo = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        checkAndRequestStoragePermissions();
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            Intent intent = new Intent(InicioSesion.this, Homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        SignUp = findViewById(R.id.BtnRegister);
        SignIn = findViewById(R.id.BtnLogin);
        forgotPassword = findViewById(R.id.textForgotPass);

        edtMail = findViewById(R.id.EdtemailUser);
        edtPassword = findViewById(R.id.Edtpassword);
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);


/*
        // Verificar si el usuario ya está logueado
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(InicioSesion.this, Homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

*/

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesion.this, PasswordReset.class);
            startActivity(intent);
            finish();
        });

        SignUp.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesion.this, Registro.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        SignIn.setOnClickListener(v -> {
            String correo = edtMail.getText().toString();
            String cotrasena = edtPassword.getText().toString();

            if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(cotrasena)) {
                Toast.makeText(InicioSesion.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                mAuth.signInWithEmailAndPassword(correo, cotrasena)
                        .addOnCompleteListener(InicioSesion.this, task -> {
                            if (task.isSuccessful()) {
                                //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                    editor.apply();

                                    Intent intent = new Intent(InicioSesion.this, Homepage.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else if (user != null) {
                                    if (user.isEmailVerified()) {
                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("usuarios");
                                        String uid = user.getUid();
                                        usersRef.child(uid).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                String nombreUsuario = snapshot.getValue(String.class);
                                                if (nombreUsuario != null) {
                                                    Calendar calendar = Calendar.getInstance();
                                                    int hora = calendar.get(Calendar.HOUR_OF_DAY);


                                                    if (hora >= 6 && hora < 12) {
                                                        saludo = "¡Buenos días, " + "@" + nombreUsuario + "!";
                                                    } else if (hora >= 12 && hora < 18) {
                                                        saludo = "¡Buenas tardes, "+ "@" + nombreUsuario + "!";
                                                    } else {
                                                        saludo = "¡Buenas noches, "+ "@" + nombreUsuario + "!";
                                                    }

                                                    Toast toast = Toast.makeText(InicioSesion.this, saludo, Toast.LENGTH_SHORT);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // Manejar error de base de datos
                                                Toast.makeText(InicioSesion.this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        Toast.makeText(InicioSesion.this, "Por favor verifica tu correo electrónico antes de iniciar sesión.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                failedLoginAttempts++; // Incrementar el contador de intentos fallidos
                                if (failedLoginAttempts == 5) {
                                    Toast.makeText(InicioSesion.this, "Recuerde que puede cambiar su contraseña.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(InicioSesion.this, "Hubo un error con el correo o la contraseña.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } catch (Exception e) {
                // Capturar cualquier excepción y mostrar un mensaje de error
                Toast.makeText(InicioSesion.this, "Ocurrió un error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ivShowHidePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivShowHidePassword.setImageResource(R.drawable.eye_close_icon);
            } else {
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivShowHidePassword.setImageResource(R.drawable.eye_icon);
            }
            edtPassword.setSelection(edtPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        BannerAdapter adapter = new BannerAdapter(this);
        viewPager.setAdapter(adapter);
        addDotsIndicator();
        startBannerAutoScroll();
    }

    private void getStoragePermissions() {
        // Verificar permiso para leer almacenamiento
        boolean readPermissionGranted = ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        // Verificar permiso para escribir almacenamiento
        boolean writePermissionGranted = ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (readPermissionGranted && writePermissionGranted) {
            mReadStoragePermissionGranted = true;
            mWriteStoragePermissionGranted = true;
        } else {
            List<String> permissionsToRequest = new ArrayList<>();
            if (!readPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!writePermissionGranted) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSIONS_REQUEST_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                mReadStoragePermissionGranted = true;
                mWriteStoragePermissionGranted = true;
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Acceso denegado al almacenamiento, algunas funciones pueden no estar disponibles.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        }
    }


    private void checkAndRequestStoragePermissions() {
        if (!mReadStoragePermissionGranted || !mWriteStoragePermissionGranted) {
            getStoragePermissions();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void addDotsIndicator() {
        // Implementa tu lógica para agregar puntos indicadores aquí
    }

    private void startBannerAutoScroll() {
        handler = new Handler();
        final Runnable update = new Runnable() {
            public void run() {
                if (currentPage == 6) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, delay);
            }
        };
        handler.postDelayed(update, delay);
    }
}
