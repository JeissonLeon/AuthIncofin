package com.example.incofinaut.DBQuery;

import com.example.incofinaut.Modelos.ElementoLista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ListasElementos {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ElementoLista> obtenerElementosLita(String nombreTabla) {
        String sql = String.format("""
                SELECT l.Codigo,
                       l.Nombre,
                       l.Orden,
                       l.IndicadorHabilitado
                FROM %s l ORDER BY l.Orden ASC""",  nombreTabla);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ElementoLista td = new ElementoLista();
            td.setCodigo(rs.getString("Codigo"));
            td.setNombre(rs.getString("Nombre"));
            td.setOrden(rs.getInt("Orden"));
            td.setIndicadorHabilitado(rs.getInt("IndicadorHabilitado"));
            return td;
        });
    }
}

