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

import java.util.Objects;


public class UpdateNewEmail extends AppCompatActivity {


    EditText currentEmailEditText;
    EditText currentPasswordEditText;
    EditText newEmailEditText;
    Button updateEmailButton;

    ImageView btnBackarrow;


    private ImageView ivShowHidePassword;
    private boolean isPasswordVisible = false;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatenewemail);

        currentEmailEditText = findViewById(R.id.current_email_edit_text);
        currentPasswordEditText = findViewById(R.id.current_password_edit_text);
        newEmailEditText = findViewById(R.id.new_email_edit_text);
        updateEmailButton = findViewById(R.id.update_email_button);
        btnBackarrow = findViewById(R.id.btnBackarrow);

        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);

        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateNewEmail.this, Perfil.class);
                startActivity(intent);
                finish();
            }
        });

        updateEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmail();
            }
        });

        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    currentPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    currentPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                currentPasswordEditText.setSelection(currentPasswordEditText.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

    }


    private void changeEmail() {
        String email = currentEmailEditText.getText().toString().trim();
        String password = currentPasswordEditText.getText().toString().trim();
        String newEmail = newEmailEditText.getText().toString().trim();

        // Validación de campos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(newEmail)) {
            //Toast.makeText(UpdateNewEmail.this, errorMessage, Toast.LENGTH_LONG).show();
            Toast.makeText(UpdateNewEmail.this, "Por favor, llene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Reautenticación del usuario
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> reauthTask) {
                    if (reauthTask.isSuccessful()) {
                        // Cambio de correo electrónico
                        user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> updateEmailTask) {
                                if (updateEmailTask.isSuccessful()) {
                                    //Toast.makeText(UpdateNewEmail.this, "Cambio de correo electrónico realizado con éxito. Ingrese nuevamente.", Toast.LENGTH_LONG).show();

                                    // Mostrar ventana informativa durante 1.5 segundos
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(UpdateNewEmail.this);
                                    builder.setMessage("Se ha enviado un correo de verificación al nuevo correo electrónico. Por favor, verifica tu bandeja de entrada y sigue las instrucciones para completar el cambio.")
                                            .setCancelable(false);
                                    final AlertDialog alert = builder.create();
                                    alert.show();

                                    final Context context = UpdateNewEmail.this;
                                    // Cerrar la ventana después de 10 segundos
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            alert.dismiss();
                                            // Cerrar sesión en Firebase
                                            // Cerrar sesión en Firebase

                                            FirebaseAuth.getInstance().signOut();
                                            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putBoolean(KEY_IS_LOGGED_IN, false);
                                            editor.apply();

                                            Intent intent = new Intent(UpdateNewEmail.this, InicioSesion.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }, 1500);
                                } else {
                                    String errorMessage = Objects.requireNonNull(updateEmailTask.getException()).getMessage();
                                    Toast.makeText(UpdateNewEmail.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        String errorMessage = Objects.requireNonNull(reauthTask.getException()).getMessage();
                        Toast.makeText(UpdateNewEmail.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(UpdateNewEmail.this, "Usuario incorrecto", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UpdateNewEmail.this, Perfil.class);
        startActivity(intent);
        finish();
    }



}