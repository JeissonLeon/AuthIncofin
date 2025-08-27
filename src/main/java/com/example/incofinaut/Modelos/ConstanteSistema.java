package com.example.incofinaut.Modelos;

import lombok.Getter;
import lombok.Setter;

public class ConstanteSistema {

    @Setter
    @Getter
    private String Codigo;
    @Setter
    @Getter
    private String Descripcion;
    @Setter
    @Getter
    private String Valor;
    @Setter
    private int indicadorHabilitado;

    public boolean getIndicadorHabilitado() {
        return indicadorHabilitado != 0;
    }
}
