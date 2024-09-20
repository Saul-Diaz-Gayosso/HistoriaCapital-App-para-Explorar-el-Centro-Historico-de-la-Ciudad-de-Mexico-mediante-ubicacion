package com.example.appv1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class PasswordReset extends AppCompatActivity{
    //private ImageView btnBackarrow;
    private Button btnSignUp;
    //private Button btnAcceso;
    //private EditText edtEmail;
    //private DatabaseReference mDatabase;
    Button btnAcceso;
    ImageView btnBackarrow;
    EditText edtEmail;
    FirebaseAuth mAuth;
    String strEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordreset);


        btnBackarrow = findViewById(R.id.btnBackarrow);
        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordReset.this, InicioSesion.class);
                startActivity(intent);
                finish();

            }
        });
/*
        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordReset.this, Homepage.class); // Reemplaza LoginActivity.class con la clase de tu actividad de inicio de sesión
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
*/
        btnSignUp = findViewById(R.id.BtnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordReset.this, Registro.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        //Inicio de recuperar contrasena
        btnAcceso = findViewById(R.id.btnAcceso);
        edtEmail = findViewById(R.id.EdtEmail);
        mAuth = FirebaseAuth.getInstance();

        //Reset de password button
        btnAcceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = edtEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail))
                {
                    ResetPassword();
                }
                else
                {
                    edtEmail.setError("Por favor, escriba su correo electronico");
                }
            }
        });

    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PasswordReset.this, InicioSesion.class);
        startActivity(intent);
        finish();
    }

    private void ResetPassword() {
        mAuth.sendPasswordResetEmail(strEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(PasswordReset.this, "Enlace para reestablecer contraseña ha sido enviado a su correo", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PasswordReset.this, InicioSesion.class);
                    startActivity(intent);
                    finish();
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        String exceptionMessage = exception.getMessage();
                        Log.e("PasswordReset", "Error: " + exceptionMessage);
                        if (exceptionMessage.contains("no user record")) {
                            edtEmail.setError("Correo no registrado, intente de nuevo");
                        } else {
                            Toast.makeText(PasswordReset.this, "Error al enviar el enlace de reestablecimiento. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PasswordReset.this, "Error desconocido. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}