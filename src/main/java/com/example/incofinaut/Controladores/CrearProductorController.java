package com.example.incofinaut.Controladores;

import com.example.incofinaut.IncofinAutApplication;
import com.example.incofinaut.IncofinCrearTest.CrearProductor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrearProductorController {

    private final CrearProductor crearProductor;

    public CrearProductorController(CrearProductor crearProductor) {
        this.crearProductor = crearProductor;
    }

    @PostMapping("/crearProductor")
    public ResponseEntity<String> crearProductor() {
        System.out.println("Creando Productor");
        // Aquí llamas tu lógica de servicio
        crearProductor.setUp();
        crearProductor.LoginTest();
        return ResponseEntity.ok("Productor creado correctamente");
    }
}

