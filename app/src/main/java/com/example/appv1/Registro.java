package com.example.appv1;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class Registro extends AppCompatActivity {

    private CheckBox checkPoliticas;
    private EditText edtNombre;
    private EditText edtUser;
    private EditText edtPassword;
    private EditText edtCheckpassword;
    private EditText edtCorreo;
    private Button btnRegister;
    private ImageView btnArrowback;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private TextView txtTerminosCondiciones;
    private ImageView ivShowHidePassword;
    private ImageView ivShowHidePassword2;
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible2 = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        ivShowHidePassword2 = findViewById(R.id.ivShowHidePassword2);


        //validacion campo correo
        edtCorreo = findViewById(R.id.EdtCorreo);
        edtCorreo.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                // se ha perdido el foco
                if (!hasFocus) {
                    validacion("EdtCorreo");
                }
            }
        });

        //validacion campo nombre completo
        edtNombre = findViewById(R.id.EdtNombre);
        edtNombre.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                // se ha perdido el foco
                if (!hasFocus) {
                    validacion("Edtnombre");
                }
            }
        });

        //validacion campo nombre de usuario
        edtUser = findViewById(R.id.EdtNombreUsuario);
        edtUser.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                // se ha perdido el foco
                if (!hasFocus) {
                    validacion("EdtUsuario");
                }
            }
        });

        //validacion campo de "contraseña" & campo "confirmar contraseña"
        edtPassword = findViewById(R.id.EdtPassword);
        edtCheckpassword = findViewById(R.id.EdtPasswordConfirm);

        edtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                //se ha perdido el foco
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(edtPassword.getText().toString()) && !TextUtils.isEmpty(edtCheckpassword.getText().toString())) {
                        validacion("EdtPassword");
                    }
                }
            }
        });
        edtCheckpassword.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                //se ha perdido el foco
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(edtPassword.getText().toString()) && !TextUtils.isEmpty(edtCheckpassword.getText().toString())) {
                        validacion("EdtPassword");
                    }
                }
            }
        });

        //logica btn arrow back
        btnArrowback = findViewById(R.id.btnBackarrow);
        btnArrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registro.this, InicioSesion.class);
                startActivity(intent);
                finish();

            }
        });

        txtTerminosCondiciones = findViewById(R.id.txtTerminosCondiciones);
        txtTerminosCondiciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la URL de las condiciones y política de privacidad en el navegador
                String url = "https://www.historiacapital.site";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                edtPassword.setSelection(edtPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        ivShowHidePassword2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible2) {
                    edtCheckpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowHidePassword2.setImageResource(R.drawable.eye_close_icon); // Cambia esto a tu icono cerrado
                } else {
                    edtCheckpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowHidePassword2.setImageResource(R.drawable.eye_icon); // Cambia esto a tu icono abierto
                }
                edtCheckpassword.setSelection(edtCheckpassword.getText().length());
                isPasswordVisible2 = !isPasswordVisible2;
            }
        });

        //seccion logica del boton "Registrar"
        checkPoliticas = findViewById(R.id.checkCondicion);
        btnRegister = findViewById(R.id.BtnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checar que todos los campos esten llenos
                String correo = edtCorreo.getText().toString();
                String nombre = edtNombre.getText().toString();
                String usuario = edtUser.getText().toString();
                String contrasena = edtPassword.getText().toString();
                database = FirebaseDatabase.getInstance();
                DatabaseReference usuariosRef = database.getReference("usuarios");

                if (!correo.isEmpty() && !nombre.isEmpty() && !usuario.isEmpty() && !contrasena.isEmpty()) {
                    if (checkPoliticas.isChecked()) {
                        // aqui hacer el PUT al servidor de realtime database Firebase
                        addUserToDB(correo, nombre, usuario, contrasena);
                    }
                    else
                        Toast.makeText(Registro.this, "Porfavor acepta las politicas de privacidad y condiciones del servicio.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(Registro.this, "Porfavor llenar todos los campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }




    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Registro.this, InicioSesion.class);
        startActivity(intent);
        finish();
    }

    protected void addUserToDB(String correo, String nombre, String usuario, String cotrasena) {
        mAuth = FirebaseAuth.getInstance();
        // Creamos un nuevo usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(correo, cotrasena)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si la creación del usuario es exitosa, guarda los datos adicionales en Firebase Realtime Database
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios");
                            String userId = user.getUid();

                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("nombre", nombre);
                            userData.put("usuario", usuario);
                            userData.put("correo", correo);

                            // No guardes la contraseña en texto plano, Firebase Authentication la maneja de forma segura
                            userRef.child(userId).setValue(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Si se guardan los datos exitosamente, realizar acciones adicionales si es necesario
                                                Toast.makeText(Registro.this, "Se completó el registro del usuario.", Toast.LENGTH_SHORT).show();
                                                //Limpia los campos de entrada después del registro exitoso
                                                edtCorreo.getText().clear();
                                                edtNombre.getText().clear();
                                                edtUser.getText().clear();
                                                edtPassword.getText().clear();
                                                edtCheckpassword.getText().clear();

                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(usuario)
                                                        .build();

                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Perfil del usuario actualizado");
                                                                    Intent intent = new Intent(Registro.this, InicioSesion.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Registro.this, "Error al actualizar el perfil del usuario.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Registro.this, "Cuenta creada. Por favor, verifica tu correo.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(Registro.this, "Error al enviar correo de verificación.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                            } else {
                                                Toast.makeText(Registro.this, "Hubo un error al guardar los datos del usuario.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Si hay un error al crear el usuario en Firebase Authentication
                            Toast.makeText(Registro.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void validacion(String caso){
        // Obtenemos una referencia a la base de datos
        database = FirebaseDatabase.getInstance();
        DatabaseReference usuariosRef = database.getReference("usuarios");

        // Validacion campo correo
        if(caso.equals("EdtCorreo")) {
            String correo = edtCorreo.getText().toString();
            String REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(correo);

            if (matcher.matches()) {
                // hacer consulta de que no exista ya ese correo
                Query query = usuariosRef.orderByChild("usuarios").equalTo(correo);
                Task<DataSnapshot> task = query.get();
                task.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(Registro.this, "Ya existe un usuario con este correo", Toast.LENGTH_SHORT).show();
                            edtCorreo.setText("");
                        } else {
                            Toast.makeText(Registro.this, "Correo electrónico válido", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(Registro.this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
                edtCorreo.setText("");
            }
        }

        // Validacion campo nombre completo
        if (caso.equals("Edtnombre")) {
            String nombre = edtNombre.getText().toString();
            if (nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$")) {
                Toast.makeText(Registro.this, "Nombre válido", Toast.LENGTH_SHORT).show();
            } else if (!nombre.equals("")) {
                Toast.makeText(Registro.this, "Nombre inválido no se aceptan caracteres especiales", Toast.LENGTH_SHORT).show();
                edtNombre.setText("");
            }
        }

        // Validacion campo nombre de usuario
        // TODO Checar que sirva la VALIDACION de que no exista otro usuario con el mismo nombre
        if (caso.equals("EdtUsuario")) {
            String usuario = edtUser.getText().toString();
            if (usuario.matches("^[a-zA-Z0-9_]{3,20}$")) {
                // validar si ya existe el usuario
                Query query = usuariosRef.orderByChild("usuarios").equalTo(usuario);
                Task<DataSnapshot> task = query.get();
                task.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(Registro.this, "Ya existe un usuario con este nombre", Toast.LENGTH_SHORT).show();
                            edtUser.setText("");
                        } else {
                            Toast.makeText(Registro.this, "Nombre de usuario válido", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            } else if (!usuario.equals("")) {
                Toast.makeText(Registro.this, "Nombre de usuario inválido, revise la longitud y los caracteres", Toast.LENGTH_SHORT).show();
                edtUser.setText("");
            }
        }

        // Validación campo contraseña y confirmar contraseña
        if (caso.equals("EdtPassword")) {
            String contrasena = edtPassword.getText().toString();
            String valContrasena = edtCheckpassword.getText().toString();

            // Pautas para la validacion de contraseña
            int LONGITUD_MINIMA = 8;
            String REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=-])(?=\\S+$).*$";

            // Verificar que ambos casos coincidan
            if (!contrasena.equals(valContrasena)) {
                Toast.makeText(Registro.this, "Los campos contraseña y confirmar contraseña no coinciden", Toast.LENGTH_SHORT).show();
                edtPassword.setText("");
                edtCheckpassword.setText("");
            } else {
                // Verificar longitud mínima
                if (contrasena.length() < LONGITUD_MINIMA) {
                    Toast.makeText(Registro.this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
                    edtPassword.setText("");
                    edtCheckpassword.setText("");
                } else {
                    // Verificar complejidad
                    Pattern pattern = Pattern.compile(REGEX);
                    Matcher matcher = pattern.matcher(contrasena);
                    if (matcher.matches()) {
                        Toast.makeText(Registro.this, "Contraseña válida", Toast.LENGTH_SHORT).show();
                    } else {
                        edtPassword.setText("");
                        edtCheckpassword.setText("");
                        if (!contrasena.matches(".*[0-9].*"))
                            Toast.makeText(Registro.this, "La contraseña debe contener al menos un dígito.", Toast.LENGTH_SHORT).show();
                        if (!contrasena.matches(".*[a-z].*"))
                            Toast.makeText(Registro.this, "La contraseña debe contener al menos una letra minúscula.", Toast.LENGTH_SHORT).show();
                        if (!contrasena.matches(".*[A-Z].*"))
                            Toast.makeText(Registro.this, "La contraseña debe contener al menos una letra mayúscula.", Toast.LENGTH_SHORT).show();
                        if (!contrasena.matches(".*[@#$%^&+=-].*"))
                            Toast.makeText(Registro.this, "La contraseña debe contener al menos un carácter especial.", Toast.LENGTH_SHORT).show();
                        if (contrasena.matches(".*\\s.*"))
                            Toast.makeText(Registro.this, "La contraseña no debe contener espacios en blanco.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    protected void setCheckBoxText(){
        checkPoliticas = findViewById(R.id.checkCondicion);
        String checkBoxText = checkPoliticas.getText().toString();
        SpannableString spannableString = new SpannableString(checkBoxText);

        // Crear un ClickableSpan para "Condiciones del servicio"
        ClickableSpan condicionesDelServicioClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.historiacapital.site"));
                startActivity(intent);
            }
        };

        // Crear un ClickableSpan para "Política de privacidad"
        ClickableSpan politicaDePrivacidadClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.historiacapital.site"));
                startActivity(intent);
            }
        };

        // Definir las posiciones de inicio y fin para "Condiciones del servicio"
        int startCondicionesDelServicio = checkBoxText.indexOf("Condiciones del servicio");
        int endCondicionesDelServicio = startCondicionesDelServicio + "Condiciones del servicio".length();

        // Definir las posiciones de inicio y fin para "Política de privacidad"
        int startPoliticaDePrivacidad = checkBoxText.indexOf("Política de privacidad");
        int endPoliticaDePrivacidad = startPoliticaDePrivacidad + "Política de privacidad".length();

        // Agregar los ClickableSpan a las partes relevantes del SpannableString
        spannableString.setSpan(condicionesDelServicioClickableSpan, startCondicionesDelServicio, endCondicionesDelServicio, 0);
        spannableString.setSpan(politicaDePrivacidadClickableSpan, startPoliticaDePrivacidad, endPoliticaDePrivacidad, 0);

        // Establecer el SpannableString en el CheckBox
        checkPoliticas.setText(spannableString);
        checkPoliticas.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

