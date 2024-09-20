package com.example.appv1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.appv1.MyEmailSender;
public class OpinionAdapter extends RecyclerView.Adapter<OpinionAdapter.OpinionViewHolder> {
    private static final String TAG = "OpinionAdapter";
    private List<Opinion> opinions;
    private Context context;
    private FirebaseFunctions mFunctions;

    public OpinionAdapter(Context context, List<Opinion> opinions) {
        this.context = context;
        this.opinions = opinions;
        mFunctions = FirebaseFunctions.getInstance();
    }

    @NonNull
    @Override
    public OpinionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_opinion, parent, false);
        return new OpinionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpinionViewHolder holder, int position) {
        Opinion opinion = opinions.get(position);
        Log.d(TAG, "OpinionAdapter Opinion: " + opinion.toString());

        holder.userName.setText(opinion.getAutor());
        holder.title.setText(opinion.getTitle());
        holder.textOpinion.setText(opinion.getOpinion());
        holder.score.setText(opinion.getScore() + " de 5");
        holder.numLikes.setText(String.valueOf(opinion.getNum_likes()));

        // Configurar las estrellas basadas en la puntuación
        setStars(holder, opinion.getScore());

        // Cargar la imagen de la opinión
        Glide.with(context)
                .load(opinion.getImagen())
                .into(holder.imageOpinion);

        // Ejemplo de cómo cargar una imagen localmente, reemplázalo con Glide/Picasso si es necesario
        holder.userIcon.setImageResource(R.drawable.user_icon); // Reemplaza con tu lógica de carga de imágenes

        // Verificar si el usuario ha dado like y cambiar el ícono en consecuencia
        holder.checkIfUserLiked(String.valueOf(opinion.getOpinionId()));

            // Manejar clics en el ícono de like
            holder.likeIcon.setOnClickListener(v -> {
                int numLikes = Integer.parseInt(holder.numLikes.getText().toString());

                if (holder.likeIcon.getTag() != null && holder.likeIcon.getTag().equals("liked")) {
                    // Usuario ha dado like, ahora se elimina
                    numLikes--;
                    holder.likeIcon.setImageResource(R.drawable.like_icon);
                    holder.likeIcon.setTag(null);
                    String opinionId = String.valueOf(opinion.getOpinionId());
                    Log.d(TAG, "OpinionAdapter: "+opinionId);
                    holder.updateLike(String.valueOf(opinion.getOpinionId()), false);
                } else {
                    // Usuario da un nuevo like
                    numLikes++;
                    holder.likeIcon.setImageResource(R.drawable.liked_icon);
                    holder.likeIcon.setTag("liked");
                    String opinionId = String.valueOf(opinion.getOpinionId());
                    Log.d(TAG, "OpinionAdapter: "+opinionId);
                    holder.updateLike(String.valueOf(opinion.getOpinionId()), true);
                }

                // Actualizar el número de likes mostrado
                holder.numLikes.setText(String.valueOf(numLikes));
            });

        holder.moreIcon.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                String opinionId = String.valueOf(opinion.getOpinionId());

                new AlertDialog.Builder(context)
                        .setTitle("Reportar Reseña")
                        .setMessage("¿Quieres reportar esta reseña?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            MyEmailSender.sendReportEmail((Activity) context, userEmail, opinionId); // Cast a Activity
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(context, "No hay un usuario autenticado", Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void setStars(OpinionViewHolder holder, double score) {
            int fullStars = (int) score;
            int emptyStars = 5 - fullStars;

            for (int i = 1; i <= fullStars; i++) {
                getStarImageView(holder, i).setImageResource(R.drawable.star_full);
            }
            for (int i = fullStars + 1; i <= 5; i++) {
                getStarImageView(holder, i).setImageResource(R.drawable.star_empty);
            }
        }

    private ImageView getStarImageView(OpinionViewHolder holder, int starNumber) {
        switch(starNumber){
            case 1:
                return holder.estrella1;
            case 2:
                return holder.estrella2;
            case 3:
                return holder.estrella3;
            case 4:
                return holder.estrella4;
            case 5:
                return holder.estrella5;
            default:
                throw new IllegalArgumentException("El número de estrella debe estar entre 1 y 5");
        }
    }

    @Override
    public int getItemCount(){
        return opinions.size();
    }

    public void addOpinions(List<Opinion> newOpinions){
        // Agregar log para mostrar las opiniones que se están cargando
        for (Opinion opinion : newOpinions) {
            Log.d(TAG, "Añadiendo opinion: " + opinion.toString());
        }

        // Limpiar la lista de opiniones antes de agregar nuevas opiniones
        // Si es apropiado para tu caso de uso
        // this.opinions.clear();

        // Añadir las nuevas opiniones sin duplicar
        for (Opinion newOpinion : newOpinions) {
            if (!opinions.contains(newOpinion)) {
                opinions.add(newOpinion);
            }
        }

        notifyDataSetChanged();
    }

    public class OpinionViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView title;
        TextView textOpinion;
        TextView score;
        TextView numLikes;
        ImageView userIcon;
        ImageView moreIcon;
        ImageView estrella1, estrella2, estrella3, estrella4, estrella5;
        ImageView imageOpinion;
        ImageView likeIcon;

        public OpinionViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            title = itemView.findViewById(R.id.title);
            textOpinion = itemView.findViewById(R.id.textOpinion);
            score = itemView.findViewById(R.id.score);
            numLikes = itemView.findViewById(R.id.numLikes);
            userIcon = itemView.findViewById(R.id.userIcon);
            moreIcon = itemView.findViewById(R.id.moreIcon);
            estrella1 = itemView.findViewById(R.id.estrella1);
            estrella2 = itemView.findViewById(R.id.estrella2);
            estrella3 = itemView.findViewById(R.id.estrella3);
            estrella4 = itemView.findViewById(R.id.estrella4);
            estrella5 = itemView.findViewById(R.id.estrella5);
            imageOpinion = itemView.findViewById(R.id.imageOpinion);
            likeIcon = itemView.findViewById(R.id.likeIcon);
        }

        private void updateLike(String opinionID, boolean addLike) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference opinionRef = db.collection("Opinions").document(opinionID);
            DocumentReference likeRef = opinionRef.collection("likes").document(userID);

            // Actualizar el campo num_likes en la opinión
            opinionRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    long currentLikes = documentSnapshot.getLong("num_likes");

                    // Incrementar o decrementar los likes según addLike sea true o false
                    long newLikes = addLike ? currentLikes + 1 : currentLikes - 1;

                    // Asegurar que el número de likes no sea negativo
                    if (newLikes < 0) {
                        newLikes = 0;
                    }

                    // Actualizar el campo num_likes en Firestore
                    opinionRef.update("num_likes", newLikes)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Número de likes actualizado con éxito"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error al actualizar el número de likes", e));

                    // Añadir o eliminar el like en la subcolección de likes
                    if (addLike) {
                        likeRef.set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario añadido a la subcolección de likes."))
                                .addOnFailureListener(e -> Log.w(TAG, "Error añadiendo el usuario a la lista de likes", e));
                    } else {
                        likeRef.delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario eliminado de la subcolección de likes."))
                                .addOnFailureListener(e -> Log.w(TAG, "Error eliminando el usuario de la lista de likes", e));
                    }
                } else {
                    Log.d(TAG, "El documento de opinión no existe.");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Error al obtener el documento de opinión", e));
        }

        private void checkIfUserLiked(String opinionID) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("Opinions")
                    .document(opinionID).collection("likes").document(userID);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // El usuario ha dado like, cambiar el ícono
                        likeIcon.setImageResource(R.drawable.liked_icon);
                        likeIcon.setTag("liked");
                    } else {
                        // El usuario no ha dado like, usar el ícono por defecto
                        likeIcon.setImageResource(R.drawable.like_icon);
                        likeIcon.setTag(null);
                    }
                } else {
                    Log.d(TAG, "Fallo al obtener el documento: ", task.getException());
                }
            });
        }
    }
}