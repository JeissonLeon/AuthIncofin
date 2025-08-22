package com.example.incofinaut.Modelos;

public class TipoDocumento {
    private String nombre;
    private int orden;
    private int indicadorHabilitado;

    // getters y setters
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getIndicadorHabilitado() {
        return indicadorHabilitado;
    }

    public void setIndicadorHabilitado(int indicadorHabilitado) {
        this.indicadorHabilitado = indicadorHabilitado;
    }
}
