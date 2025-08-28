package com.example.incofinaut.DBQuery;

import com.example.incofinaut.Modelos.ConstanteSistema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class Productor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void ActivarProductorArtificialmente(String documento) {
        try {
            String sqlCodigo = """
                SELECT l.codigo 
                FROM incofin.productores p
                INNER JOIN incofin.listastiposdocumentos l 
                       ON p.IdTipoIdentificacion = l.IdLista
                WHERE p.NumeroDocumento = ?
            """;

            String codigoTipoDocumento = jdbcTemplate.queryForObject(sqlCodigo, String.class, documento);

            if (codigoTipoDocumento == null) {
                System.err.println("⚠️ No se encontró tipo de documento para el documento " + documento);
                return;
            }

            String uuid = "UUIDPRUEBA";
            String faceId = codigoTipoDocumento + documento + uuid;

            String sqlUpdate = """
                UPDATE incofin.productores
                SET IndicadorHabilitado = 1, 
                    Validacion = 1, 
                    FaceId = ?
                WHERE NumeroDocumento = ?
            """;

            int rows = jdbcTemplate.update(sqlUpdate, faceId, documento);

            if (rows > 0) {
                System.out.println("✅ Productor con documento " + documento +
                        " actualizado correctamente con FaceId: " + faceId);
            } else {
                System.out.println("⚠️ No se encontró productor con documento " + documento);
            }

        } catch (Exception e) {
            System.err.println("❌ Error actualizando productor: " + e.getMessage());
        }
    }
}

