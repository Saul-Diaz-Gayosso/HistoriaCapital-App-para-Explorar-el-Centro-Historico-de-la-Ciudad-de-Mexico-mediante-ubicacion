package com.example.appv1;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Opinion {
    private String itemID;
    private String UID;
    private String autor;
    private boolean denunciar_pub;
    private String imagen;
    private int num_likes;
    private boolean ocultar_pub;
    private String opinion;
    private long opinionId;
    private boolean reportar_pub;
    private double score;
    private Timestamp timestamp;
    private String title; //solo esta presente en la tarjeta

    // Constructor sin argumentos
    public Opinion() {

    }

    //Constructor con argumentos
    public void Opinions(String itemID, String UID, String autor, boolean denunciar_pub, String imagen, int num_likes, boolean ocultar_pub, String opinion, long opinionID, boolean reportar_pub, double score, Timestamp timestamp) {
        this.itemID = itemID;
        this.UID = UID;
        this.autor = autor;
        this.denunciar_pub = denunciar_pub;
        this.imagen = imagen;
        this.num_likes = num_likes;
        this.ocultar_pub = ocultar_pub;
        this.opinion = opinion;
        this.opinionId = opinionID;
        this.reportar_pub = reportar_pub;
        this.score = score;
        this.timestamp = timestamp;
    }

    // Getters y setters
    @PropertyName("ItemID")
    public String getItemID(){ return itemID; }

    @PropertyName("ItemID")
    public void setItemID(String itemID){ this.itemID = itemID; }

    @PropertyName("UID")
    public String getUID(){ return itemID; }

    @PropertyName("UID")
    public void setUID(String UID){ this.UID = UID; }

    @PropertyName("autor")
    public String getAutor() {
        return autor;
    }

    @PropertyName("autor")
    public void setAutor(String autor) {
        this.autor = autor;
    }

    @PropertyName("denunciar_pub")
    public boolean isDenunciar_pub() {
        return denunciar_pub;
    }

    @PropertyName("denunciar_pub")
    public void setDenunciar_pub(boolean denunciar_pub) {
        this.denunciar_pub = denunciar_pub;
    }

    @PropertyName("imagen")
    public String getImagen() {
        return imagen;
    }

    @PropertyName("imagen")
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    @PropertyName("num_likes")
    public int getNum_likes() {
        return num_likes;
    }

    @PropertyName("num_likes")
    public void setNum_likes(int num_likes) {
        this.num_likes = num_likes;
    }

    @PropertyName("ocultar_pub")
    public boolean isOcultar_pub() {
        return ocultar_pub;
    }

    @PropertyName("ocultar_pub")
    public void setOcultar_pub(boolean ocultar_pub) {
        this.ocultar_pub = ocultar_pub;
    }

    @PropertyName("opinion")
    public String getOpinion() {
        return opinion;
    }

    @PropertyName("opinion")
    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    @PropertyName("opinionId")
    public long getOpinionId() {
        return opinionId;
    }

    @PropertyName("opinionId")
    public void setOpinionId(long opinionId) {
        this.opinionId = opinionId;
    }

    @PropertyName("reportar_pub")
    public boolean isReportar_pub() {
        return reportar_pub;
    }

    @PropertyName("reportar_pub")
    public void setReportar_pub(boolean reportar_pub) {
        this.reportar_pub = reportar_pub;
    }

    @PropertyName("score")
    public double getScore() {
        return score;
    }

    @PropertyName("score")
    public void setScore(double score) {
        this.score = score;
    }

    @PropertyName("timestamp")
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Opinion{" +
                "itemID='" + itemID + '\'' +
                ", UID='" + UID + '\'' +
                ", autor='" + autor + '\'' +
                ", denunciar_pub=" + denunciar_pub +
                ", imagen='" + imagen + '\'' +
                ", num_likes=" + num_likes +
                ", ocultar_pub=" + ocultar_pub +
                ", opinion='" + opinion + '\'' +
                ", opinionId=" + opinionId +
                ", reportar_pub=" + reportar_pub +
                ", score=" + score +
                ", timestamp=" + timestamp +
                ", title='" + title + '\'' +
                '}';
    }
}
