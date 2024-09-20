package com.example.appv1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.core.view.Change;

public class ChangePassword extends AppCompatActivity {

    // Declaración de variables
     EditText editTextEmail;
     EditText editTextOldPassword;
     EditText editTextNewPassword;
     Button buttonChangePassword;

    ImageView btnBackarrow;


    ImageView ivShowHidePassword;
    ImageView ivShowHidePassword2;
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible2 = false;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // En el método onCreate o en algún otro lugar adecuado
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepass);

        // Vinculación de vistas
        editTextEmail = findViewById(R.id.EdtEmail);
        editTextOldPassword = findViewById(R.id.EdtOldPassword);
        editTextNewPassword = findViewById(R.id.EdtNewPassword);
        buttonChangePassword = findViewById(R.id.btnChange);
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        ivShowHidePassword2 = findViewById(R.id.ivShowHidePassword2);

        // Configurar el clic del ImageView para alternar la visibilidad de la contraseña
        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    editTextOldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    editTextOldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                editTextOldPassword.setSelection(editTextOldPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        ivShowHidePassword2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible2) {
                    editTextNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword2.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    editTextNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword2.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                editTextNewPassword.setSelection(editTextNewPassword.getText().length());
                isPasswordVisible2 = !isPasswordVisible2;
            }
        });


        btnBackarrow = findViewById(R.id.btnBackarrow);
        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePassword.this, Perfil.class);
                startActivity(intent);
                finish();

            }
        });

        // Configuración del OnClickListener para el botón
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para cambiar la contraseña
                changePassword();
                //finish();
            }
        });
    }


    // Método para cambiar la contraseña
    private void changePassword() {
        String email = editTextEmail.getText().toString().trim();
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // Validación de campos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(ChangePassword.this, "Por favor, llene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Validación de la nueva contraseña
        if (!isValidPassword(newPassword)) {
            Toast.makeText(ChangePassword.this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial", Toast.LENGTH_LONG).show();
            return;
        }

        // Reautenticación del usuario
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Cambio de contraseña
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangePassword.this, "Cambio de contraseña realizado con éxito. Ingrese nuevamente.", Toast.LENGTH_LONG).show();
                                    // Cerrar sesión y redirigir a la pantalla de inicio de sesión
                                    final Context context = ChangePassword.this;

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            // Cerrar sesión en Firebase
                                            // Cerrar sesión en Firebase

                                            FirebaseAuth.getInstance().signOut();
                                            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putBoolean(KEY_IS_LOGGED_IN, false);
                                            editor.apply();

                                            Intent intent = new Intent(ChangePassword.this, InicioSesion.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            //return true;
                                        }
                                    }, 1000);
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(ChangePassword.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(ChangePassword.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(ChangePassword.this, "Usuario incorrecto", Toast.LENGTH_LONG).show();
        }
    }

    // Método para validar la contraseña
    private boolean isValidPassword(String password) {
        // Verificar longitud mínima
        if (password.length() < 8) {
            return false;
        }

        // Verificar al menos una mayúscula
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Verificar al menos un número
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Verificar al menos un carácter especial
        if (!password.matches(".*[!@#$%^&*()\\-+].*")) {
            return false;
        }

        // La contraseña cumple con todos los requisitos
        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangePassword.this, Perfil.class);
        startActivity(intent);
        finish();
    }



}
