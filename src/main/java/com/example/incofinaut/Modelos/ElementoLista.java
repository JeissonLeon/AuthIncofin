package com.example.incofinaut.Modelos;

import lombok.Getter;

public class ElementoLista {
    @Getter
    private String nombre;
    @Getter
    private int orden;
    private int indicadorHabilitado;
    @Getter
    private String codigo;

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean getIndicadorHabilitado() {
        return indicadorHabilitado != 0;
    }

    public void setIndicadorHabilitado(int indicadorHabilitado) {
        this.indicadorHabilitado = indicadorHabilitado;
    }
}
