package com.example.ff1.herramientas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ff1.juego.Partida;
import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

/**
 * El editor lleva una copia del esquema de la configuración
 * ({@code herramientas/editor/esquema-configuracion.js}). Esta prueba la compara con la que declara
 * el motor: si alguien añade o cambia un parámetro y olvida el editor, falla.
 */
public class EsquemaEditorTest {

    private static final String CABECERA = "window.ESQUEMA_CONFIGURACION = ";

    private static Nodo esquemaDelEditor() throws IOException {
        File f = new File("herramientas/editor/esquema-configuracion.js");
        if (!f.isFile()) {
            f = new File("../herramientas/editor/esquema-configuracion.js"); // Gradle corre desde app/
        }
        String texto = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim();
        int inicio = texto.indexOf(CABECERA); // puede haber un comentario antes
        assertTrue(inicio >= 0);
        assertTrue(texto.endsWith(";"));
        return LectorJson.leer(texto.substring(inicio + CABECERA.length(), texto.length() - 1));
    }

    @Test
    public void elEditorDeclaraExactamenteLosParametrosDelMotor() throws IOException {
        EsquemaConfiguracion motor = Partida.esquema();
        Nodo editor = esquemaDelEditor();
        Set<String> delEditor = new LinkedHashSet<>();
        for (Nodo p : editor.lista("parametros")) {
            String nombre = p.texto("nombre");
            assertTrue("repetido en el editor: " + nombre, delEditor.add(nombre));
            assertTrue("el motor no declara " + nombre, motor.nombres().contains(nombre));
            assertEquals(nombre + ": tipo", motor.esEntero(nombre), p.texto("tipo").equals("entero"));
            assertEquals(nombre + ": mínimo", motor.minimo(nombre), p.decimal("minimo"), 0);
            assertEquals(nombre + ": máximo", motor.maximo(nombre), p.decimal("maximo"), 0);
            assertEquals(nombre + ": defecto", motor.defecto(nombre), p.decimal("defecto"), 0);
            assertFalse(nombre + ": falta la descripción", p.texto("descripcion").trim().isEmpty());
            assertFalse(nombre + ": falta el grupo", p.texto("grupo").trim().isEmpty());
        }
        assertEquals(new LinkedHashSet<>(motor.nombres()), delEditor);
    }

    @Test
    public void elEsquemaDelMotorExponeSusParametros() {
        EsquemaConfiguracion motor = Partida.esquema();
        assertTrue(motor.nombres().contains("combate.varianza"));
        assertEquals(0, motor.minimo("combate.varianza"), 0);
        assertEquals(50, motor.maximo("combate.varianza"), 0);
        assertEquals(10, motor.defecto("combate.varianza"), 0);
        assertTrue(motor.esEntero("combate.varianza"));
    }
}
