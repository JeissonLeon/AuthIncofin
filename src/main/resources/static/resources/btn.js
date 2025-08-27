document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("StartTest");

    btn.addEventListener("click", async () => {
        try {
            const response = await fetch("http://localhost:8080/crearProductor", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (response.ok) {
                const data = await response.json();
                alert("Productor creado: " + JSON.stringify(data));
            } else {
                alert("Error al crear productor");
            }
        } catch (err) {
            console.error("Error en fetch:", err);
            alert("No se pudo conectar con el servidor");
        }
    });
});
