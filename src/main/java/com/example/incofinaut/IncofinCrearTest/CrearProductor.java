package com.example.incofinaut.IncofinCrearTest;

import com.example.incofinaut.DBQuery.ListasTiposDocumentos;
import com.example.incofinaut.DriverManager.DriverManager;
import com.example.incofinaut.Modelos.TipoDocumento;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CrearProductor {
    WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(CrearProductor.class.getName());

    @Autowired
    private ListasTiposDocumentos listasTiposDocumentos; // sin new

    public void setUp() {
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
        logger.info("✅ Set Up iniciado");
    }

    public void LoginTest() {
        try {
            /*
            driver.get("https://front.incofin.hexasolutions.co/login");
            logger.info("🔵 Navegando a login incofin");

            driver.findElement(By.id("username")).sendKeys("administrador.sistema");
            driver.findElement(By.xpath("//input[@placeholder='Contraseña']")).sendKeys("Administrador.2025");
            logger.info("🟢 Documento y contraseña ingresados");

            // Buscar el botón "Ingresar"
            driver.findElement(By.xpath("//button[.//span[text()='Ingresar']]")).click();
            logger.info("🟢 Botón 'Ingresar' presionado");

            // Esperar a que cargue el dashboard
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-plantilla")));
            logger.info("🟢 Dashboard Cargado");

            // Redirigir a la página de productores
            driver.get("https://front.incofin.hexasolutions.co/auth/productores/crear");
            logger.info("🟢 Ingresando a crear productores");

            WebElement Select = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p-dropdown//div[@id='idTipoIdentificacion']")));
            Select.click();
            */

            List<TipoDocumento> ltd = listasTiposDocumentos.obtenerListasTiposDocumentos();

            for (TipoDocumento td : ltd) {
                System.out.println(td.getNombre());
            }

        } catch (Exception | AssertionError e) {
            logger.log(Level.SEVERE, "❌ Excepción encontrada: " + e.getMessage(), e);
            Assert.fail("❌ Prueba fallida. Revisa logs anteriores para más detalles.");
        } finally {
            logger.info("🟢 Test finalizado");
        }
    }
}