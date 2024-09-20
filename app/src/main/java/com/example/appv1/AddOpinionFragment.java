package com.example.appv1;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddOpinionFragment extends Fragment {
        private static final int PICK_IMAGE_REQUEST = 1;
        private ImageView userIcon;
        private EditText editTextDescription;
        private ImageView buttonSelectImage;
        private ImageView buttonSelectEmoji;
        public ImageView btnClose;
        private Button buttonSubmit;
        private Uri imageUri;
        private TextView userName;
        private Spinner placeSpinner;
        private ArrayAdapter<String> placeAdapter;
        private List<String> placeNames;
        private Map<String, String> placeMap;
        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private TextView score;
        private ImageView estrella1, estrella2, estrella3, estrella4, estrella5;
        private double currentScore = 0;
        private String itemId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_opinion_card, container, false);

        placeSpinner = view.findViewById(R.id.placeSpinner);
        userIcon = view.findViewById(R.id.userIcon);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        buttonSelectEmoji = view.findViewById(R.id.buttonSelectEmoji);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonSelectImage.setOnClickListener(v -> {
            if (imageUri != null) {
                showImageOptionsDialog();

            } else {
                openFileChooser();
            }
        });
        buttonSubmit.setOnClickListener(v -> showConfirmationDialog());
        btnClose = view.findViewById(R.id.closeButton);
        userName = view.findViewById(R.id.userName);
        score = view.findViewById(R.id.score);
        mAuth = FirebaseAuth.getInstance();
        placeNames = new ArrayList<>();
        placeMap = new HashMap<>();

        btnClose.setOnClickListener(v -> handleOnBackPressed());

        placeNames = new ArrayList<>();
        placeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, placeNames);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeSpinner.setAdapter(placeAdapter);

        //actualiza el score
        score.setText("Score: 0/5");
        estrella1 = view.findViewById(R.id.estrella1);
        estrella2 = view.findViewById(R.id.estrella2);
        estrella3 = view.findViewById(R.id.estrella3);
        estrella4 = view.findViewById(R.id.estrella4);
        estrella5 = view.findViewById(R.id.estrella5);
        estrella1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRating(1);
            }
        });
        estrella2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRating(2);
            }
        });
        estrella3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRating(3);
            }
        });
        estrella4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRating(4);
            }
        });
        estrella5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRating(5);
            }
        });

        db = FirebaseFirestore.getInstance();
        loadPlaces();
        displayUserName();

        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = placeNames.get(position);
                itemId = placeMap.get(selectedPlace);
                Toast.makeText(getContext(), "Seleccionó: " + selectedPlace, Toast.LENGTH_SHORT).show();
                // Manejar la selección del lugar aquí
                Log.d(TAG, "Lugar seleccionado: " + selectedPlace);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
        return view;
    }

    public void handleOnBackPressed() {
        String description = editTextDescription.getText().toString().trim();
        boolean isStarSelected = currentScore > 0;
        boolean isImageLoaded = imageUri != null;

        if (!description.isEmpty() || isStarSelected || isImageLoaded) {
            showExitConfirmationDialog();
        } else {
            closeFragment();
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("¿Quieres seguir editando tu reseña?")
                .setMessage("Tiene cambios sin guardar. ¿Desea seguir editando o salir?")
                .setPositiveButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Salir", (dialog, which) -> closeFragment())
                .create()
                .show();
    }

    private void setRating(int rating) {
        currentScore = rating;
        // Actualizar las imágenes de las estrellas
        estrella1.setImageResource(rating >= 1 ? R.drawable.star_full : R.drawable.star_empty);
        estrella2.setImageResource(rating >= 2 ? R.drawable.star_full : R.drawable.star_empty);
        estrella3.setImageResource(rating >= 3 ? R.drawable.star_full : R.drawable.star_empty);
        estrella4.setImageResource(rating >= 4 ? R.drawable.star_full : R.drawable.star_empty);
        estrella5.setImageResource(rating >= 5 ? R.drawable.star_full : R.drawable.star_empty);

        // Actualizar el texto del puntaje
        score.setText("Score: " + rating + "/5");
    }

    private void loadPlaces() {
        db.collection("Items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentSnapshot document : querySnapshot) {
                        String placeName = document.getString("nombre");
                        String id = document.getId(); // O usa document.getString("id") si el ID no es el documento ID
                        if (placeName != null) {
                            placeNames.add(placeName);
                            placeMap.put(placeName, id); // Almacenar el ID con el nombre
                        }
                    }
                    placeAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "QuerySnapshot es nulo");
                }
            } else {
                Log.d(TAG, "Error al obtener los documentos", task.getException());
            }
        });
    }

    private void displayUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String usuario = currentUser.getDisplayName();
            Log.d(TAG, "displayUserName, usuario: " + usuario); //ojo me esta dando nulo
            if (usuario != null) {
                userName.setText(usuario);
            } else {
                Toast.makeText(getContext(), "No se encontró el usuario", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Ningún usuario está autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void showImageOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Opciones de imagen")
                .setMessage("Ya tiene una imagen cargada. ¿Qué quiere hacer?")
                .setPositiveButton("Cambiar imagen", (dialog, which) -> openFileChooser())
                .setNegativeButton("Borrar imagen", (dialog, which) -> {
                    imageUri = null;
                    buttonSelectImage.setImageResource(R.drawable.ic_media);
                    Toast.makeText(getContext(), "Imagen borrada", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            // Obtener la URI de la imagen seleccionada
            imageUri = data.getData();

            // Verificar el tamaño del archivo seleccionado
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                int size = inputStream.available(); // Tamaño en bytes

                // Verificar si el tamaño excede 4MB (4MB = 4 * 1024 * 1024 bytes)
                if (size > 4 * 1024 * 1024) {
                    // Mostrar un mensaje de error o realizar alguna acción
                    Toast.makeText(getContext(), "La imagen seleccionada excede el tamaño máximo permitido (4MB)", Toast.LENGTH_SHORT).show();
                    // Limpiar la imagen seleccionada
                    imageUri = null;
                    buttonSelectImage.setImageDrawable(null); // Limpiar la imagen del ImageView
                    return;
                }

                // Mostrar la imagen seleccionada en el ImageView
                buttonSelectImage.setImageURI(imageUri);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al abrir la imagen", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Si el usuario no selecciona ninguna imagen, imageUri debería ser null
            imageUri = null;
            //buttonSelectImage.setImageDrawable(null); // Limpiar la imagen del ImageView
            buttonSelectImage.setImageResource(R.drawable.ic_media);
        }
    }


    private void closeFragment() {
        //obten el fragmentManager
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
        ((Community) getActivity()).showCentralContent();

        //agregar una bandera que si se manda a llamar cuando se agregó una opinion, se carge todas las opiniones de nuevo o solo la nueva y que se ponga hasta arriba
    }


    private void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar Reseña")
                .setMessage("¿Es correcta la información de su reseña? No podrá editarla después.")
                .setPositiveButton("Continuar", (dialog, which) -> submitOpinion())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    private void submitOpinion() {
        String description = editTextDescription.getText().toString().trim();
        String selectedPlace = (String) placeSpinner.getSelectedItem();

        // Obtener el ID del lugar seleccionado
        String selectedPlaceId = placeMap.get(selectedPlace);

        Log.d(TAG, "score de la opinion (currentScore): " + currentScore);

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Por favor llena la descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String author = currentUser.getDisplayName();
        long opinionID = System.currentTimeMillis();

        // Verificar si hay una imagen seleccionada
        if (imageUri == null) {
            saveOpinionToFirestore(itemId, userId, author, null, description, opinionID, currentScore);
        } else {
            // Caso con imagen: subir la imagen y luego guardar la opinión con la URL de la imagen
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Opinions/" + opinionID + ".jpg");
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveOpinionToFirestore(itemId, userId, author, imageUrl, description, opinionID, currentScore);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Falló al subir la imagen", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveOpinionToFirestore(String itemId, String userId, String author, String imageUrl, String description, long opinionID, double currentScore) {
        Map<String, Object> opinion = new HashMap<>();
        opinion.put("ItemID", itemId); // Guardar el ID del lugar
        opinion.put("UID", userId);
        opinion.put("autor", author);
        opinion.put("denunciar_pub", false);
        if (imageUrl != null) {
            opinion.put("imagen", imageUrl);
        }
        opinion.put("num_likes", 0);
        opinion.put("ocultar_pub", false);
        opinion.put("opinion", description);
        opinion.put("opinionId", opinionID);
        opinion.put("reportar_pub", false);
        opinion.put("timestamp", com.google.firebase.Timestamp.now());
        opinion.put("score", currentScore);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Opinions").document(String.valueOf(opinionID))
                .set(opinion)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "La opinión se subió correctamente", Toast.LENGTH_SHORT).show();
                    // Limpiar campos de entrada y restablecer imagen (si se desea)
                    editTextDescription.getText().clear();
                    imageUri = null;
                    // Actualizamos score
                    actualizarOpinionYScore(itemId, currentScore);
                    closeFragment(); //todo que se indique lo que esta dentro de la funcion
                    Intent intent = new Intent(getContext(), Community.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish(); // Finaliza la actividad actual
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Algo falló, intentelo nuevamente", Toast.LENGTH_SHORT).show());
    }

    private void actualizarOpinionYScore(String itemIdString, double currentScore){
        Log.d(TAG, "actualizarOpinionYScore, itemId: "+itemIdString+" currentScore: "+currentScore);
        // Inicializa Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clase contenedora para las variables que necesitamos modificar
        class Holder {
            Double score;
            Long totalOpinions;
        }

        // Instancia de la clase contenedora
        Holder holder = new Holder();

        // Convierte itemIdString a long
        long itemId = Long.parseLong(itemIdString);

        // Consulta el documento en la colección "Items" con el campo sitioInteresId igual a itemId
        db.collection("Items").whereEqualTo("sitioInteresId", itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d(TAG, "actualizarOpinionYScore, success " + querySnapshot.size() + " documents found:");
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Imprimir información del documento
                                Log.d(TAG, "Document ID: " + document.getId());
                                Log.d(TAG, "Document Data: " + document.getData());

                                // Recupera el score y total_opinions
                                holder.score = document.getDouble("score");
                                holder.totalOpinions = document.getLong("total_opinions");
                                Log.d(TAG, "actualizarOpinionYScore, holder.score = " + holder.score + " holder.totalOpinions = " + holder.totalOpinions);

                                // Asegúrate de que los valores no sean nulos
                                if (holder.score != null && holder.totalOpinions != null) {
                                    // Actualizamos score y total_opinions
                                    holder.score = ((holder.score * holder.totalOpinions) + currentScore) / (holder.totalOpinions + 1);
                                    holder.totalOpinions = holder.totalOpinions + 1;

                                    // Los guardamos en la base de datos
                                    Log.d(TAG, "Score: " + holder.score);
                                    Log.d(TAG, "Total Opinions: " + holder.totalOpinions);

                                    // Actualiza los valores en Firestore
                                    document.getReference().update("score", holder.score, "total_opinions", holder.totalOpinions)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                                            .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                                } else {
                                    Log.d(TAG, "Los valores de score o total_opinions son nulos");
                                }
                            }
                        } else {
                            Log.d(TAG, "No documents found");
                        }
                    } else {
                        Log.d(TAG, "Error al obtener documentos: ", task.getException());
                    }
                });
    }

}
