package com.application.markus.easymeal;

import java.util.List;

/**
 * Created by markus on 14/05/2017.
 */

public class InfoGeneralReceta {
    String nick;
    String nombre;
    String imgReceta;
    String imgUser;
    String fecha;
    String descripcion;
    List<Steps> pasos;
    List<Ingredient> ingredientes;

    public InfoGeneralReceta(String nick, String imgUser, String nombre, String imgReceta, String fecha, String descripcion, List<Steps> pasos, List<Ingredient> ingredientes) {
        this.nick = nick;
        this.imgUser = imgUser;
        this.nombre = nombre;
        this.imgReceta = imgReceta;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.pasos = pasos;
        this.ingredientes = ingredientes;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getImgUser() { return imgUser; }

    public void setImgUser(String imgUser) { this.imgUser = imgUser; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImgReceta() {
        return imgReceta;
    }

    public void setImgReceta(String imgReceta) {
        this.imgReceta = imgReceta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Steps> getPasos() {
        return pasos;
    }

    public void setPasos(List<Steps> pasos) {
        this.pasos = pasos;
    }

    public List<Ingredient> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<Ingredient> ingredientes) {
        this.ingredientes = ingredientes;
    }
}
