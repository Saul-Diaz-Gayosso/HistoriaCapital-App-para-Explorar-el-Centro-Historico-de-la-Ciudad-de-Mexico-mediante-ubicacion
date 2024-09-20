package com.example.appv1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.text.LineBreaker;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ImageView imageView;
    private TextView title;
    private TextView score;
    private MyClusterItem item;
    private View view;
    private ImageView[] stars;
    private TextView textPrecio;
    private TextView textHorario;
    private TextView textLinkSitio;

    private TextView textLinkCompra;
    private TextView textTelefono;
    private TextView textHistoriaSitio;
    private TextView textLocation;
    private ImageView imageSpeaker;
    private ImageView imageBookmark;
    private FirebaseStorage storage;
    private MediaPlayer mediaPlayer;
    private ImageView btnRoute;
    private boolean isPlaying = false;
    private boolean isRouteActive = false;

    private boolean isBookmarked;
    private DatabaseReference databaseReference;
    public BottomSheetFragment(MyClusterItem item) {
        this.item = item;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tarjeta_bottom, container, false);

        // Configurar la vista con los datos del marcador
        imageView = view.findViewById(R.id.imv_url);
        title = view.findViewById(R.id.title);
        score = view.findViewById(R.id.score);
        imageSpeaker = view.findViewById(R.id.speaker);
        imageBookmark = view.findViewById(R.id.bookmark);
        textPrecio = view.findViewById(R.id.txtViewPrecio);
        textHorario = view.findViewById(R.id.horario);
        textLinkSitio = view.findViewById(R.id.linkSitio);
        textLinkCompra = view.findViewById(R.id.idNombre);
        textTelefono = view.findViewById(R.id.telefonoSitio);
        textHistoriaSitio = view.findViewById(R.id.historiaSitio);
        textLocation = view.findViewById(R.id.location);
        btnRoute = view.findViewById(R.id.route);

        stars = new ImageView[5];
        stars[0] = view.findViewById(R.id.estrella1);
        stars[1] = view.findViewById(R.id.estrella2);
        stars[2] = view.findViewById(R.id.estrella3);
        stars[3] = view.findViewById(R.id.estrella4);
        stars[4] = view.findViewById(R.id.estrella5);


        title.setText(item.getTitle());
        storage = FirebaseStorage.getInstance();


        Long sitioInteresId = item.getSitioInteresId(); // Obtén el Long directamente desde tu objeto MyClusterItem
        String sitioInteresIdStr = sitioInteresId.toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(userId)
                    .child("favorites")
                    .child(sitioInteresIdStr);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    isBookmarked = dataSnapshot.exists() && (Boolean) dataSnapshot.getValue();
                    updateBookmarkIcon();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Manejar errores si ocurre algún problema al leer los datos
                }
            });
        }

        imageBookmark.setOnClickListener(v -> toggleBookmark());

        imageSpeaker.setOnClickListener(v -> {
            String audioUrl = item.getAudio();
            if (isPlaying) {
                pauseAudio();
            } else {
                playAudioFromUrl(audioUrl);
            }
        });


        String StringScore = String.valueOf(item.getScore());
        score.setText(StringScore+" de 5 ("+item.getTotalOpinions()+" opiniones)");

        // Configurar contenido multimedia si lo tienes
        Glide.with(this)
                .load(item.getImgURL())
                .into(imageView);

        double ScoreValue = Double.parseDouble(StringScore);
        updateStars(ScoreValue);

        // Text Precio
        textPrecio.setText(item.getPrecio());

        // Text Location
        textLocation.setText(item.getDescUbi());

        // Text Horario */FALTA COMPROBAR SI ESTA ABIERO O CERRADO/*
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String horario = "";

        // Using a switch-case statement with break statements inside to prevent overwriting the value
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                horario = item.getHorarioL();
                break;
            case Calendar.TUESDAY:
                horario = item.getHorarioM();
                break;
            case Calendar.WEDNESDAY:
                horario = item.getHorarioMi();
                break;
            case Calendar.THURSDAY:
                horario = item.getHorarioJ();
                break;
            case Calendar.FRIDAY:
                horario = item.getHorarioV();
                break;
            case Calendar.SATURDAY:
                horario = item.getHorarioS();
                break;
            case Calendar.SUNDAY:
                horario = item.getHorarioD();
                break;
        }
        // asignar el horario al EditText
        textHorario.setText(horario);

        // asignar el link del Sitio al EditText
        textLinkSitio.setText(item.getLink_sitio());
        textLinkCompra.setText(item.getLink_compra());
        textTelefono.setText(item.getTelefono());

        textLinkSitio.setOnClickListener(v -> {
            abrirEnlaceWeb(item.getLink_sitio());
        });

        textLinkCompra.setOnClickListener(v -> {
            abrirEnlaceWeb2(item.getLink_compra());
        });

        textTelefono.setOnClickListener(v -> {
            // Obtener el número de teléfono del campo de texto
            String phoneNumber = textTelefono.getText().toString();

            // Crear un Intent con la acción de marcar (ACTION_DIAL)
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));

            // Mostrar el cuadro de diálogo de llamada para que el usuario decida si llama o no
            startActivity(dialIntent);
        });

        btnRoute.setOnClickListener(v -> {
            // Obtener una referencia al MapFragment
            FragmentManager fragmentManager = getParentFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag("TagFragemntoMapa");
            if (mapFragment != null) {
                // Llamar a la función calculateDirections() de MapFragment
                mapFragment.calculateDirections();
            } else {
                // Manejar caso cuando MapFragment no se encuentra
                Toast.makeText(getContext(), "No se encontró el fragmento del mapa", Toast.LENGTH_SHORT).show();
            }
            // Cerrar el BottomSheet
            dismiss();
        });



        textHistoriaSitio.setText(HtmlCompat.fromHtml(item.getDescripcion(), HtmlCompat.FROM_HTML_MODE_COMPACT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textHistoriaSitio.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        return view;
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
    private void toggleBookmark() {
        isBookmarked = !isBookmarked;
        updateBookmarkIcon();
        databaseReference.setValue(isBookmarked);
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            imageBookmark.setImageResource(R.drawable.bookmark_full);
        } else {
            imageBookmark.setImageResource(R.drawable.bookmark);
        }
    }

    private void playAudioFromUrl(String audioUrl) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            StorageReference storageRef = storage.getReferenceFromUrl(audioUrl);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                try {
                    mediaPlayer.setDataSource(uri.toString());
                    mediaPlayer.prepareAsync(); // Preparar de forma asíncrona
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        isPlaying = true;
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(e -> {
                // Manejo de errores
                e.printStackTrace();
            });
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
    }
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateStars(double score) {
        int fullStars = (int) score;
        boolean hasHalfStar = (score-fullStars) >= 0.5;
        for (int i = 0; i < stars.length; i++) {
            if (i < fullStars) {
                stars[i].setImageResource(R.drawable.star_full);
            } else if (i == fullStars && hasHalfStar) {
                stars[i].setImageResource(R.drawable.star_half);
            } else {
                stars[i].setImageResource(R.drawable.star_empty);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Asegúrate de que esté expandido completamente

                // Añadir un nuevo BottomSheetCallback personalizado
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        // Evitar el cierre automático del BottomSheet
                        // Dejar el método vacío para que no ejecute ninguna acción en el cambio de estado
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // Permitir la navegación continua sin cierre automático
                        // No realiza ninguna acción específica durante el deslizamiento
                    }
                });
            }
        }
    }
}