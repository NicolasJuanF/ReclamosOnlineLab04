package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo;


import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by mdominguez on 26/10/17.
 */

public class Reclamo {
    private Integer id;
    private String titulo;
    private String detalle;
    private Date fecha;
    private TipoReclamo tipo;
    private LatLng lugar;
    private Estado estado;


    // private foto
    // private audio
    public Reclamo() {
    }

    public Reclamo(Integer id, String detalle) {
        this.id = id;
        this.detalle = detalle;
    }

    public Reclamo(Integer id, String titulo, String detalle, Date fecha, TipoReclamo tipo, Estado estado , LatLng lugar) {
        this.id = id;
        this.titulo = titulo;
        this.detalle = detalle;
        this.fecha = fecha;
        this.tipo = tipo;
        this.estado = estado;
        this.lugar = lugar;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public TipoReclamo getTipo() {
        return tipo;
    }

    public void setTipo(TipoReclamo tipo) {
        this.tipo = tipo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LatLng getLugar() {
        return lugar;
    }

    public void setLugar(LatLng lugar) {
        this.lugar = lugar;
    }


}
