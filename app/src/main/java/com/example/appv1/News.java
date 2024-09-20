package com.example.appv1;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class News extends AppCompatActivity {

    private static final String TAG = "NewsActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView btnBackarrow;
    private LinearLayout noticiasContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        btnBackarrow = findViewById(R.id.btnBackarrow);
        noticiasContainer = findViewById(R.id.noticiasContainer);

        // Cargar las noticias desde Firestore
        cargarNoticias();

        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(News.this, Homepage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cargarNoticias() {
        CollectionReference noticiasRef = db.collection("News");
        noticiasRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String titulo = document.getString("titulo");
                                String textoNoticia = document.getString("texto_noticia");
                                String imageUrl = document.getString("img");
                                String enlace = document.getString("enlace");

                                View noticiaView = LayoutInflater.from(News.this).inflate(R.layout.item_noticia, noticiasContainer, false);
                                noticiasContainer.addView(noticiaView);

                                TextView tituloView = noticiaView.findViewById(R.id.textViewTituloNoticia);
                                LinearLayout textoNoticiaContainer = noticiaView.findViewById(R.id.textViewNoticiaContainer);
                                ImageView imgView = noticiaView.findViewById(R.id.imageViewNoticia);
                                TextView enlaceView = noticiaView.findViewById(R.id.textViewEnlace);

                                tituloView.setText(titulo);

                                // Dividir el texto en párrafos utilizando "<br><br>" como separador
                                String[] parrafos = textoNoticia.split("<br><br>");

                                // Limpiar el contenedor de texto de noticias
                                textoNoticiaContainer.removeAllViews();

                                // Añadir cada párrafo como un nuevo TextView dentro del contenedor
                                for (String parrafo : parrafos) {
                                    TextView parrafoView = new TextView(News.this);
                                    parrafoView.setText(Html.fromHtml(parrafo));
                                    parrafoView.setPadding(0, 0, 0, 16); // Añadir algo de padding inferior entre párrafos
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        parrafoView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                                    }
                                    textoNoticiaContainer.addView(parrafoView);
                                }

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Glide.with(News.this)
                                            .load(imageUrl)
                                            .centerCrop()
                                            .into(imgView);
                                }

                                if (enlace != null && !enlace.isEmpty()) {
                                    enlaceView.setText("Para leer más, haga clic aquí...");
                                    enlaceView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(enlace));
                                            startActivity(browserIntent);
                                        }
                                    });
                                } else {
                                    enlaceView.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Log.w(TAG, "Error al obtener documentos", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(News.this, Homepage.class);
        startActivity(intent);
        finish();
    }
}
