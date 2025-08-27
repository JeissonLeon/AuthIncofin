package com.example.incofinaut.IncofinCrearTest;

import com.example.incofinaut.DBQuery.ListasElementos;
import com.example.incofinaut.DriverManager.DriverManager;
import com.example.incofinaut.Modelos.ElementoLista;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CrearProductor {
    String CELULAR_FIJO = "3168215154";
    String CORREO_FIJO = "jeisson.leon@hexasolultions.co";
    WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(CrearProductor.class.getName());

    @Autowired
    private ListasElementos listasElementos; // sin new

    public void setUp() {
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
        logger.info("✅ Set Up iniciado");
    }

    public void LoginTest() {
        try {
            // ================== LOGIN ==================
            driver.get("https://front.incofin.hexasolutions.co/login");
            logger.info("🔵 Navegando a login incofin");

            driver.findElement(By.id("username")).sendKeys("administrador.sistema");
            driver.findElement(By.xpath("//input[@placeholder='Contraseña']")).sendKeys("12344321");
            logger.info("🟢 Documento y contraseña ingresados");

            driver.findElement(By.xpath("//button[.//span[text()='Ingresar']]")).click();
            logger.info("🟢 Botón 'Ingresar' presionado");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-plantilla")));
            logger.info("🟢 Dashboard cargado");

            // ================== FORMULARIO ==================
            driver.get("https://front.incofin.hexasolutions.co/auth/productores/crear");
            logger.info("🟢 Ingresando a crear productores");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

            // ================== DROPDOWNS ==================
            List<WebElement> dropdowns = driver.findElements(
                    By.cssSelector("form p-dropdown > div[id].p-dropdown")
            );

            for (WebElement dropdown : dropdowns) {
                String id = dropdown.getAttribute("id");
                String clases = dropdown.getAttribute("class");

                if ((clases != null && clases.contains("p-disabled"))) {
                    logger.info("Dropdown deshabilitado, se omite: " + id);
                    continue;
                }

                try {
                    String seleccion = seleccionarOpcionAleatoria(driver, wait, id);
                    logger.info(String.format("📌 Dropdown [%s] seleccionado aleatoriamente: %s", id, seleccion));
                } catch (Exception e) {
                    logger.warning(String.format("⚠️ No se pudo seleccionar en el dropdown [%s]: %s", id, e.getMessage()));
                }
            }

            // ================== FECHAS ==================
            LocalDate fechaExpedicion = fechaExpedicionRandom();
            LocalDate fechaNacimiento = fechaExpedicion.minusYears(18);

            WebElement inputExp = driver.findElement(By.cssSelector("#fechaExpedicion input[type='text']"));
            inputExp.clear();
            inputExp.sendKeys(formatear(fechaExpedicion));
            inputExp.sendKeys(Keys.TAB);
            logger.info("📅 fechaExpedicion → " + fechaExpedicion);

            WebElement inputNac = driver.findElement(By.cssSelector("#fechaNacimiento input[type='text']"));
            inputNac.clear();
            inputNac.sendKeys(formatear(fechaNacimiento));
            inputNac.sendKeys(Keys.TAB);
            logger.info("📅 fechaNacimiento → " + fechaNacimiento);

            // ================== INPUTS DE TEXTO ==================
            Map<String, String> valores = generarValoresTexto();

            for (Map.Entry<String, String> entry : valores.entrySet()) {
                try {
                    WebElement input = driver.findElement(By.id(entry.getKey()));
                    input.clear();
                    input.sendKeys(entry.getValue());
                    logger.info("✍️ Campo [" + entry.getKey() + "] → " + entry.getValue());
                } catch (NoSuchElementException e) {
                    logger.warning("⚠️ No se encontró el campo con id: " + entry.getKey());
                }
            }

            // ================== AUTOCOMPLETE LUGARES ==================
            llenarAutocomplete("idLugarExpedicion", valores.get("lugarExpedicion"));
            llenarAutocomplete("idLugarNacimiento", valores.get("lugarNacimiento"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Excepción en LoginTest: " + e.getMessage(), e);
            Assert.fail("❌ Prueba fallida. Revisa logs anteriores.");
        }
    }

// ================== MÉTODOS DE APOYO ==================

    public void llenarAutocomplete(String idCampo, String valor) {
        try {
            WebElement inputLugar = driver.findElement(By.cssSelector("#" + idCampo + " input[type='text']"));
            inputLugar.clear();
            inputLugar.sendKeys(valor);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.p-autocomplete-items li")));
            inputLugar.sendKeys(Keys.ARROW_DOWN);
            inputLugar.sendKeys(Keys.ENTER);

            logger.info("📍 Autocomplete [" + idCampo + "] → " + valor);
        } catch (Exception e) {
            logger.warning("⚠️ No se pudo llenar el autocomplete [" + idCampo + "]: " + e.getMessage());
        }
    }

    public String seleccionarOpcionAleatoria(WebDriver driver, WebDriverWait wait, String dropdownId) {
        WebElement select = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[@id='" + dropdownId + "']")));
        select.click();

        List<WebElement> opciones = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.cssSelector("#" + dropdownId + "_list li[role='option']")));

        if (opciones.isEmpty()) {
            throw new RuntimeException("No se encontraron opciones en el dropdown: " + dropdownId);
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(opciones.size());
        WebElement opcionAleatoria = opciones.get(randomIndex);

        String textoSeleccionado = opcionAleatoria.getText().trim();
        opcionAleatoria.click();

        return textoSeleccionado;
    }

    // ---------- Utilidades de fechas ----------
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LocalDate fechaRandomEntre(LocalDate desde, LocalDate hasta) {
        long minDay = desde.toEpochDay();
        long maxDay = hasta.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

    public LocalDate fechaExpedicionRandom() {
        LocalDate hoy = LocalDate.now();
        LocalDate hace5Anios = hoy.minusYears(5);
        return fechaRandomEntre(hace5Anios, hoy);
    }

    public String formatear(LocalDate fecha) {
        return fecha.format(FORMATTER);
    }

    public Map<String, String> generarValoresTexto() {
        Map<String, String> valores = new HashMap<>();

        // nombres y apellidos
        valores.put("primerNombre", randomDeLista(List.of("Juan", "Pedro", "Luis", "Ana", "Maria", "Laura")));
        valores.put("segundoNombre", randomDeLista(List.of("Andrés", "Camilo", "José", "Lucía", "Elena", "Paola")));
        valores.put("primerApellido", randomDeLista(List.of("Gómez", "Pérez", "Rodríguez", "Martínez", "Fernández")));
        valores.put("segundoApellido", randomDeLista(List.of("López", "Hernández", "Díaz", "Ramírez", "Castro")));

        // documento
        valores.put("numeroDocumento", String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999)));

        // contacto fijo
        valores.put("numeroCelular", CELULAR_FIJO);
        valores.put("correoElectronico", CORREO_FIJO);

        // montos económicos
        valores.put("valorActivos", String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 200_000_000)));
        valores.put("valorIngresosAnuales", String.valueOf(ThreadLocalRandom.current().nextInt(5_000_000, 100_000_000)));

        return valores;
    }

    private String randomDeLista(List<String> opciones) {
        return opciones.get(ThreadLocalRandom.current().nextInt(opciones.size()));
    }

}