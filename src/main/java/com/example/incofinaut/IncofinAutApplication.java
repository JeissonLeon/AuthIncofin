package com.example.incofinaut;

import com.example.incofinaut.IncofinCrearTest.CrearProductor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class IncofinAutApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(IncofinAutApplication.class, args);

        CrearProductor crearProductor = context.getBean(CrearProductor.class);
        crearProductor.setUp();
        crearProductor.LoginTest();

    }

}
