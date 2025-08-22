package com.example.incofinaut.DBQuery;

import com.example.incofinaut.Modelos.TipoDocumento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Repository
public class ListasTiposDocumentos {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<TipoDocumento> obtenerListasTiposDocumentos() {
        String sql = "SELECT l.Codigo, l.Nombre, l.Orden, l.IndicadorHabilitado FROM listastiposdocumentos l ORDER BY l.Orden ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TipoDocumento td = new TipoDocumento();
            td.setCodigo(rs.getString("Codigo"));
            td.setNombre(rs.getString("Nombre"));
            td.setOrden(rs.getInt("Orden"));
            td.setIndicadorHabilitado(rs.getInt("IndicadorHabilitado"));
            return td;
        });
    }
}

