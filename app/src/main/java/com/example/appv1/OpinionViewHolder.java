package com.example.appv1;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        //Inicialización de los elementos de la vista
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
}
