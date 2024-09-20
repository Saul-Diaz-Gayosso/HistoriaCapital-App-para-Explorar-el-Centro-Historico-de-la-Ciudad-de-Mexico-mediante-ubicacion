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


public class DeleteAccount extends AppCompatActivity {


    EditText EmailEditText;
    EditText PasswordEditText;
    Button deleteAccountButton;
    ImageView ivShowHidePassword; // Referencia al ImageView para el ojito
    ImageView btnBackarrow;
    boolean isPasswordVisible = false;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        EmailEditText = findViewById(R.id.email_edit_text);
        PasswordEditText = findViewById(R.id.password_edit_text);
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword); // Encuentra el ImageView
        deleteAccountButton = findViewById(R.id.delete_account_button);
        btnBackarrow = findViewById(R.id.btnBackarrow);

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

        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteAccount.this, Perfil.class);
                startActivity(intent);
                finish();
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

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
    }
    private void deleteAccount() {
        String email = EmailEditText.getText().toString().trim();
        String password = PasswordEditText.getText().toString().trim();

        // Validación de campos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(DeleteAccount.this, "Por favor, llene todos los campos", Toast.LENGTH_LONG).show();
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
                        // Mostrar confirmación de eliminación

                        final Context context = DeleteAccount.this; // Almacena el contexto de la actividad principal

                        new AlertDialog.Builder(DeleteAccount.this)
                                .setTitle("Confirmación")
                                .setMessage("¿Está seguro de que desea eliminar su cuenta de forma permanente? Esta acción no se puede deshacer.")
                                .setPositiveButton("Eliminar cuenta", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Eliminar cuenta
                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> deleteTask) {
                                                if (deleteTask.isSuccessful()) {
                                                    // Aplicar SharedPreferences después de eliminar la cuenta
                                                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putBoolean(KEY_IS_LOGGED_IN, false);
                                                    editor.apply();

                                                    // Mostrar Toast indicando que la cuenta ha sido eliminada
                                                    Toast.makeText(DeleteAccount.this, "Cuenta eliminada. Hasta pronto.", Toast.LENGTH_LONG).show();

                                                    // Agregar un retraso de 1 segundo antes de cerrar la aplicación y redirigir a la pantalla de inicio de sesión
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // Cerrar la aplicación
                                                            finishAffinity();

                                                            // Redirigir a la pantalla de inicio de sesión
                                                            Intent intent = new Intent(DeleteAccount.this, InicioSesion.class);
                                                            startActivity(intent);
                                                        }
                                                    }, 1000); // 1000 milisegundos = 1 segundo
                                                } else {
                                                    String errorMessage = Objects.requireNonNull(deleteTask.getException()).getMessage();
                                                    Toast.makeText(DeleteAccount.this, errorMessage, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } else {
                        String errorMessage = Objects.requireNonNull(reauthTask.getException()).getMessage();
                        Toast.makeText(DeleteAccount.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(DeleteAccount.this, "Usuario incorrecto", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DeleteAccount.this, Perfil.class);
        startActivity(intent);
        finish();
    }





}