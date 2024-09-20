package com.example.appv1;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class ChangeName extends AppCompatActivity {


    EditText EmailEditText;
    EditText PasswordEditText;
    EditText newNameEditText;
    Button updateNameButton;

    ImageView ivShowHidePassword; // Referencia al ImageView para el ojito

    boolean isPasswordVisible = false;

    ImageView btnBackarrow;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        EmailEditText = findViewById(R.id.email_edit_text);
        PasswordEditText = findViewById(R.id.password_edit_text);
        newNameEditText = findViewById(R.id.new_name_edit_text);
        updateNameButton = findViewById(R.id.update_name_button);

        ivShowHidePassword = findViewById(R.id.ivShowHidePassword); // Encuentra el ImageView

        btnBackarrow = findViewById(R.id.btnBackarrow);
        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeName.this, Perfil.class);
                startActivity(intent);
                finish();
            }
        });

        // Configurar el clic del ImageView para alternar la visibilidad de la contraseña
        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    PasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    PasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                PasswordEditText.setSelection(PasswordEditText.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });



        updateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });
    }
    private void changeName() {
        String email = EmailEditText.getText().toString().trim();
        String password = PasswordEditText.getText().toString().trim();
        String newName = newNameEditText.getText().toString().trim();

        // Validación de campos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(newName)) {
            Toast.makeText(ChangeName.this, "Por favor, llene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Reautenticación del usuario
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Obtener las credenciales del usuario actual
            //AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            // Reautenticar al usuario con las credenciales
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> reauthTask) {
                    if (reauthTask.isSuccessful()) {
                        // Actualizar el nombre del usuario
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName) // Aquí establecemos el nuevo nombre
                                .build();

                        // Aplicar los cambios en el perfil del usuario
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> updateProfileTask) {
                                if (updateProfileTask.isSuccessful()) {
                                    // El nombre se actualizó correctamente
                                    Toast.makeText(ChangeName.this, "Nombre actualizado correctamente. Inicie sesión nuevamente.", Toast.LENGTH_SHORT).show();

                                    // Retrasar la acción de cerrar sesión y redirigir
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            FirebaseAuth.getInstance().signOut();
                                            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putBoolean(KEY_IS_LOGGED_IN, false);
                                            editor.apply();

                                            Intent intent = new Intent(ChangeName.this, InicioSesion.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }, 1500);
                                } else {
                                    // Error al actualizar el nombre del usuario
                                    String errorMessage = Objects.requireNonNull(updateProfileTask.getException()).getMessage();
                                    Toast.makeText(ChangeName.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        // Error al reautenticar al usuario
                        String errorMessage = Objects.requireNonNull(reauthTask.getException()).getMessage();
                        Toast.makeText(ChangeName.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            // El usuario no está autenticado
            Toast.makeText(ChangeName.this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangeName.this, Perfil.class);
        startActivity(intent);
        finish();
    }



}