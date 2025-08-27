package com.example.incofinaut.DBQuery;

import com.example.incofinaut.Modelos.ConstanteSistema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConsultarConstantes {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ConstanteSistema obtenerConstante(String codigo) {
        String sql = """
                SELECT c.Codigo,
                       c.Descripcion,
                       c.Valor,
                       c.IndicadorHabilitado
                FROM constantes c
                WHERE c.Codigo = ?
                ORDER BY c.AuditoriaFecha DESC""";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            ConstanteSistema cs = new ConstanteSistema();
            cs.setCodigo(rs.getString("Codigo"));
            cs.setDescripcion(rs.getString("Descripcion"));
            cs.setValor(rs.getString("Valor"));
            cs.setIndicadorHabilitado(rs.getInt("IndicadorHabilitado"));
            return cs;
        }, codigo);
    }
}

