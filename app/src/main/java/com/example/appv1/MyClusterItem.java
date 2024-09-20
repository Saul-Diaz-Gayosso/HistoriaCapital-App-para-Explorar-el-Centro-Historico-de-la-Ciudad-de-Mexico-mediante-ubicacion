package com.example.appv1;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyClusterItem implements ClusterItem {
    private String telefono;
    private String precio;
    private String link_sitio;
    private String link_compra;
    private String horario_disp_L;
    private String horario_disp_M;
    private String horario_disp_Mi;
    private String horario_disp_J;
    private String horario_disp_V;
    private String horario_disp_S;
    private String horario_disp_D;
    private String desc_ubi;
    private LatLng position;
    private String title;
    private double score;
    private int totalOpinions;
    private String imgURL;
    private String descripcion;
    private String audio;
    private Long sitioInteresId;

    public MyClusterItem(LatLng position, String title, double score, int totalOpinions, String imgURL,
                         String telefono, String precio, String link_sitio, String link_compra, String horario_disp_L,
                         String horario_disp_M, String horario_disp_Mi, String horario_disp_J, String horario_disp_V,
                         String horario_disp_S, String horario_disp_D, String desc_ubi, String descripcion, String audio,
                         Long sitioInteresId) { // Agregando el campo de identificación
        this.position = position;
        this.title = title;
        this.score = score;
        this.totalOpinions = totalOpinions;
        this.imgURL = imgURL;
        this.telefono = telefono;
        this.precio = precio;
        this.link_sitio = link_sitio;
        this.link_compra = link_compra;
        this.horario_disp_L = horario_disp_L;
        this.horario_disp_M = horario_disp_M;
        this.horario_disp_Mi = horario_disp_Mi;
        this.horario_disp_J = horario_disp_J;
        this.horario_disp_V = horario_disp_V;
        this.horario_disp_S = horario_disp_S;
        this.horario_disp_D = horario_disp_D;
        this.desc_ubi = desc_ubi;
        this.descripcion = descripcion;
        this.audio = audio;
        this.sitioInteresId = sitioInteresId; // Asignando el valor del identificador
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return "Score: " + score + ", Opiniones: " + totalOpinions;
    }

    public double getScore() {
        return score;
    }

    public int getTotalOpinions() {
        return totalOpinions;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getPrecio() {
        return precio;
    }

    public String getHorarioL() {
        return horario_disp_L;
    }

    public String getHorarioM() {
        return horario_disp_M;
    }

    public String getHorarioMi() {
        return horario_disp_Mi;
    }

    public String getHorarioJ() {
        return horario_disp_J;
    }

    public String getHorarioV() {
        return horario_disp_V;
    }

    public String getHorarioS() {
        return horario_disp_S;
    }

    public String getHorarioD() {
        return horario_disp_D;
    }

    public String getLink_sitio() {
        return link_sitio;
    }

    public String getLink_compra() {
        return link_compra;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDescUbi() {
        return desc_ubi;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAudio() {
        return audio;
    }

    public Long getSitioInteresId() {
        return sitioInteresId;
    }
}
