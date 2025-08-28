package com.example.incofinaut.IncofinCrearTest;


import com.example.incofinaut.DBQuery.ConsultarConstantes;
import com.example.incofinaut.DBQuery.Productor;
import com.example.incofinaut.DriverManager.DriverManager;
import com.example.incofinaut.Global.Constantes;

import com.example.incofinaut.Modelos.ConstanteSistema;
import com.example.incofinaut.Modelos.ElementoLista;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class CrearProductor {
    WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(CrearProductor.class.getName());

    @Autowired
    private ConsultarConstantes conConst;
    @Autowired
    private OrderedFormContentFilter formContentFilter;

    @Autowired
    private Productor productorRepo;

    private String numeroDocumentoGenerado;

    @Before
    public void setUp() {
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
        logger.info("✅ Set Up iniciado");
    }

    @Test
    public void LoginTest() {
        try {
            // ================== LOGIN ==================
            driver.get(Constantes.BaseUrl + "/login");
            logger.info("🔵 Navegando a login incofin");

            driver.findElement(By.id("username")).sendKeys(Constantes.User);
            driver.findElement(By.xpath("//input[@placeholder='Contraseña']")).sendKeys(Constantes.Password);
            logger.info("🟢 Documento y contraseña ingresados");

            driver.findElement(By.xpath("//button[.//span[text()='Ingresar']]")).click();
            logger.info("🟢 Botón 'Ingresar' presionado");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-plantilla")));
            logger.info("🟢 Dashboard cargado");

            // ================== FORMULARIO ==================
            driver.get(Constantes.BaseUrl + "/auth/productores/crear");
            logger.info("🟢 Ingresando a crear productores");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

            ConstanteSistema conSis = conConst.obtenerConstante("STRMAP");
            if (conSis.getIndicadorHabilitado() && conSis.getValor().equals("1")) {
                List<WebElement> mapa = driver.findElements(By.tagName("app-mapa"));
                if (!mapa.isEmpty()) {
                    logger.info("✅ Constante STRMAP habilitada, mapa renderizado");
                } else {
                    logger.severe("⚠️ Constante STRMAP habilitada, pero mapa NO renderizado");
                }
            } else {
                logger.info("Constante STRMAP inhabilitada o con valor 0, mapa no renderizado");
            }

            // ================== DROPDOWNS ==================
            List<WebElement> dropdowns = driver.findElements(
                    By.cssSelector("form p-dropdown > div[id].p-dropdown")
            );

            Set<String> idsDependientesDireccion = Set.of(
                    "idPais", "idDepartamento", "idMunicipio",
                    "idBarriovereda", "idListaNomenclatura", "idListaEstrato"
            );

            for (WebElement dropdown : dropdowns) {
                String id = dropdown.getAttribute("id");
                String clases = dropdown.getAttribute("class");

                // Omitir dropdowns deshabilitados
                if (clases != null && clases.contains("p-disabled")) {
                    logger.info("Dropdown deshabilitado, se omite: " + id);
                    continue;
                }

                if (id.contains("pn_id")) {
                    // Obtener el formControlName correcto desde el p-dropdown padre
                    WebElement pDropdown = dropdown.findElement(By.xpath("ancestor::p-dropdown"));
                    String formControlName = pDropdown.getAttribute("formcontrolname");

                    // ⚠️ Evitar seleccionar dropdowns dependientes de dirección aquí
                    if (idsDependientesDireccion.contains(formControlName)) {
                        logger.info("Dropdown dependiente de dirección, se seleccionará en llenarDireccion(): " + formControlName);
                        continue;
                    }
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

                    if (entry.getKey().equals("numeroDocumento")) {
                        numeroDocumentoGenerado = entry.getValue();
                    }
                } catch (NoSuchElementException e) {
                    logger.warning("⚠️ No se encontró el campo con id: " + entry.getKey());
                }
            }

            // ================== AUTOCOMPLETE LUGARES ==================
            llenarAutocompletePorLabel("Lugar de expedición", "florid", "Floridablanca");
            llenarAutocompletePorLabel("Lugar de nacimiento", "florid", "Floridablanca");

            llenarDireccion();

            Thread.sleep(5000);

            // ================== GUARDAR PRODUCTOR ==================
            WebElement btnGuardar = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath("//button[.//span[text()='Guardar']]")));
            btnGuardar.click();
            logger.info("💾 Botón 'Guardar' presionado");

            // Esperar confirmación de guardado
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'Registro actualizado correctamente') or contains(text(),'Éxito')]")
            ));
            logger.info("✅ Productor guardado exitosamente en UI");

            // ================== ACTIVAR PRODUCTOR EN BD ==================
            productorRepo.ActivarProductorArtificialmente(numeroDocumentoGenerado);
            logger.info("Productor activado artificialmente con documento: " + numeroDocumentoGenerado);

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
        try {
            WebElement select = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath("//div[@id='" + dropdownId + "']")));
            select.click();

            By opcionesLocator = By.cssSelector("#" + dropdownId + "_list li[role='option']");

            // 🔄 Intentar hasta 3 veces porque puede tardar en poblar el listado
            for (int i = 0; i < 3; i++) {
                List<WebElement> opciones = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(opcionesLocator));

                // Filtrar opciones válidas (a veces aparece "Sin resultados" como opción)
                List<WebElement> opcionesValidas = opciones.stream()
                        .filter(o -> !o.getText().trim().equalsIgnoreCase("Sin resultados"))
                        .toList();

                if (!opcionesValidas.isEmpty()) {
                    int randomIndex = ThreadLocalRandom.current().nextInt(opcionesValidas.size());
                    WebElement opcionAleatoria = opcionesValidas.get(randomIndex);

                    String textoSeleccionado = opcionAleatoria.getText().trim();
                    opcionAleatoria.click();
                    return textoSeleccionado;
                }

                logger.info("⌛ Esperando que se carguen opciones en [" + dropdownId + "] intento " + (i + 1));
                Thread.sleep(1000); // pequeña espera antes de reintentar
                select.click(); // volver a abrir el dropdown
            }

            throw new RuntimeException("No se encontraron opciones válidas en el dropdown: " + dropdownId);
        } catch (Exception e) {
            throw new RuntimeException("Error seleccionando aleatorio en [" + dropdownId + "]: " + e.getMessage(), e);
        }
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
        valores.put("numeroCelular", Constantes.Celular);
        valores.put("correoElectronico", Constantes.Correo);

        // montos económicos
        valores.put("valorActivos", String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 200_000_000)));
        valores.put("valorIngresosAnuales", String.valueOf(ThreadLocalRandom.current().nextInt(5_000_000, 100_000_000)));

        return valores;
    }

    private String randomDeLista(List<String> opciones) {
        return opciones.get(ThreadLocalRandom.current().nextInt(opciones.size()));
    }

    public void llenarAutocompletePorLabel(String labelTexto, String valor, String opcionEsperada) {
        try {
            // 1. Localizar el label que corresponde al campo
            WebElement label = driver.findElement(By.xpath("//label[contains(text(),'" + labelTexto + "')]"));

            // 2. Desde el label, buscar el input dentro del mismo contenedor
            WebElement input = label.findElement(By.xpath("following::input[@type='text'][1]"));

            input.clear();
            input.sendKeys(valor);

            // 3. Esperar a que aparezca la lista desplegable
            By opcionesLocator = By.cssSelector("ul.p-autocomplete-items li[role='option']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(opcionesLocator));

            List<WebElement> opciones = driver.findElements(opcionesLocator);

            if (opciones.isEmpty()) {
                throw new RuntimeException("❌ No se encontraron opciones para [" + labelTexto + "]");
            }

            // 4. Buscar la opción esperada (o la primera si no hay coincidencia)
            WebElement opcion = opciones.stream()
                    .filter(o -> o.getText().toLowerCase().contains(opcionEsperada.toLowerCase()))
                    .findFirst()
                    .orElse(opciones.get(0));

            opcion.click();

            // 5. Esperar hasta que el input refleje la opción seleccionada
            wait.until(driver -> input.getAttribute("value").equals(opcion.getText().trim()));

            logger.info("📍 Autocomplete [" + labelTexto + "] → Seleccionado: " + opcion.getText());

        } catch (Exception e) {
            logger.warning("⚠️ Error en autocomplete [" + labelTexto + "]: " + e.getMessage());
        }
    }


    public void seleccionarOpcionPorTexto(String formControlName, String texto) {
        try {
            // 1. Abrir el dropdown
            WebElement dropdown = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath("//p-dropdown[@formcontrolname='" + formControlName + "']//div[contains(@class,'p-dropdown')]")));
            dropdown.click();

            // 2. Esperar opciones visibles
            WebElement opcion = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//li[@role='option' and normalize-space()='" + texto + "']")));

            // 3. Seleccionar
            opcion.click();
            logger.info("✅ Seleccionado en [" + formControlName + "]: " + texto);

        } catch (Exception e) {
            logger.warning("⚠️ No se pudo seleccionar [" + texto + "] en dropdown " + formControlName + ": " + e.getMessage());
        }
    }



    public String seleccionarOpcionAleatoriaConCarga(String formControlName) {
        try {
            // 1. Abrir el dropdown
            WebElement dropdown = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath("//p-dropdown[@formcontrolname='" + formControlName + "']//div[contains(@class,'p-dropdown')]")));
            dropdown.click();

            // 2. Esperar hasta que aparezca al menos una opción válida
            wait.until(driver -> {
                List<WebElement> opciones = driver.findElements(By.xpath("//li[@role='option']"));
                return opciones.stream().anyMatch(op ->
                        !op.getText().trim().isEmpty()
                                && !op.getText().contains("Seleccione")
                                && !op.getText().contains("Sin resultados")
                );
            });

            // 3. Volver a leer las opciones
            List<WebElement> opciones = driver.findElements(By.xpath("//li[@role='option']"));

            List<WebElement> opcionesValidas = opciones.stream()
                    .filter(op -> !op.getText().trim().isEmpty()
                            && !op.getText().contains("Seleccione")
                            && !op.getText().contains("Sin resultados"))
                    .toList();

            if (opcionesValidas.isEmpty()) {
                logger.warning("⚠️ No hay opciones válidas en " + formControlName);
                return null;
            }

            // Selección especial para "idDepartamento"
            WebElement opcionElegida;
            switch (formControlName) {
                case "idDepartamento":
                    opcionElegida = opcionesValidas.stream()
                            .filter(op -> op.getText().equalsIgnoreCase("Santander"))
                            .findFirst()
                            .orElse(opcionesValidas.get(0)); // fallback: la primera si no está Santander
                    break;

                case "idMunicipio":
                    opcionElegida = opcionesValidas.stream()
                            .filter(op -> op.getText().equalsIgnoreCase("Bucaramanga"))
                            .findFirst()
                            .orElse(opcionesValidas.get(0)); // fallback: la primera si no está Santander
                    break;

                default:
                    opcionElegida = opcionesValidas.get(ThreadLocalRandom.current().nextInt(opcionesValidas.size()));
                    break;
            }

            String texto = opcionElegida.getText();
            opcionElegida.click();

            logger.info("✅ Seleccionado en [" + formControlName + "]: " + texto);
            return texto;

        } catch (Exception e) {
            logger.warning("⚠️ No se pudo seleccionar opción en dropdown " + formControlName + ": " + e.getMessage());
            return null;
        }
    }

    public void llenarDireccion() {
        try {
            // País fijo
            seleccionarOpcionPorTexto("idPais", "Colombia");

            String departamento = seleccionarOpcionAleatoriaConCarga("idDepartamento");
            logger.info("🏙 Departamento elegido: " + departamento);

            String municipio = seleccionarOpcionAleatoriaConCarga("idMunicipio");
            logger.info("🏙 Municipio elegido: " + municipio);

            // Barrio / vereda aleatorio
            String barrio = seleccionarOpcionAleatoriaConCarga("idBarriovereda");
            logger.info("🏘 Barrio elegido: " + barrio);

            // Nomenclatura aleatoria
            String nomenclatura = seleccionarOpcionAleatoriaConCarga("idListaNomenclatura");
            logger.info("🚦 Nomenclatura → " + nomenclatura);

            // Estrato aleatorio
            String estrato = seleccionarOpcionAleatoriaConCarga("idListaEstrato");
            logger.info("🏠 Estrato → " + estrato);

            // Inputs de dirección
            driver.findElement(By.id("viaPrincipal")).sendKeys(String.valueOf(ThreadLocalRandom.current().nextInt(1, 100)));
            driver.findElement(By.id("viaSecundaria")).sendKeys(String.valueOf(ThreadLocalRandom.current().nextInt(1, 100)));
            driver.findElement(By.id("complemento")).sendKeys(String.valueOf(ThreadLocalRandom.current().nextInt(1, 50)));
            driver.findElement(By.id("datosComplementarios")).sendKeys("Apartamento " + ThreadLocalRandom.current().nextInt(1, 20));

        } catch (Exception e) {
            logger.warning("⚠️ Error al llenar dirección: " + e.getMessage());
        }
    }
}