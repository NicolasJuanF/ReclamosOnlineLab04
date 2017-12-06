package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo;

/**
 * Created by mdominguez on 26/10/17.
 */

public class TipoReclamo {
    private Integer id;
    private String tipo;

    public TipoReclamo() {

    }

    public TipoReclamo(Integer id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return  tipo;
    }
}
