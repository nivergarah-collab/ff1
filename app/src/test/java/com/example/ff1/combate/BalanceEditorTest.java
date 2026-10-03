package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.AzarSecuencia;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

/**
 * La vista de balance del editor ({@code herramientas/editor/balance.js}) replica las fórmulas de
 * {@link Acciones}. Esta prueba lee los mismos casos que la prueba de node
 * ({@code pruebas/balance-casos.json}) y comprueba que el motor real da el mínimo y el máximo
 * esperados (azar en −varianza y en +varianza): si el motor cambia una fórmula y el editor no, falla.
 */
public class BalanceEditorTest {

    private static Nodo casos() throws IOException {
        File f = new File("herramientas/editor/pruebas/balance-casos.json");
        if (!f.isFile()) {
            f = new File("../herramientas/editor/pruebas/balance-casos.json"); // Gradle corre desde app/
        }
        assertTrue("falta " + f, f.isFile());
        return LectorJson.leer(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
    }

    private static ProveedorConfiguracion config(int fuerzaFisica, int fuerzaMagica, int varianza) {
        ProveedorConfiguracion c = Datos.config();
        c.reemplazar(c.actual()
                .con(ConfiguracionCombate.FUERZA_FISICA, fuerzaFisica)
                .con(ConfiguracionCombate.FUERZA_MAGICA, fuerzaMagica)
                .con(ConfiguracionCombate.VARIANZA, varianza));
        return c;
    }

    private static int[] valor(Nodo caso, ProveedorConfiguracion c, int varianza) {
        int[] r = new int[2];
        for (int k = 0; k < 2; k++) {
            // Con varianza 0 el motor no consulta el azar; si no, 0 da r = −v y 2v da r = +v.
            Acciones a = new Acciones(c, new AzarSecuencia(k == 0 ? 0 : 2 * varianza),
                    Datos.tiposHabilidad(), Datos.estados());
            switch (caso.texto("tipo")) {
                case "fisico": {
                    Combatiente atacante = new Combatiente(Datos.definicion("a", Bando.HEROE, 40, 0,
                            caso.entero("ataque"), 0, 0, 10));
                    Combatiente defensor = new Combatiente(Datos.definicion("d", Bando.ENEMIGO, 40, 0,
                            0, caso.entero("defensa"), 0, 10));
                    r[k] = a.danioFisico(atacante, defensor);
                    break;
                }
                case "magico": {
                    Combatiente defensor = new Combatiente(Datos.definicion("d", Bando.ENEMIGO, 40, 0,
                            0, caso.entero("defensa"), 0, 10));
                    r[k] = a.danioMagico(caso.entero("poderHabilidad"), caso.entero("poderActor"), defensor);
                    break;
                }
                case "curacion":
                    r[k] = a.curacion(caso.entero("poderHabilidad"), caso.entero("poderActor"));
                    break;
                default:
                    throw new AssertionError("tipo de caso desconocido: " + caso.texto("tipo"));
            }
        }
        return r;
    }

    @Test
    public void elMotorDaLosMismosMinimosYMaximosQueLaVistaDeBalance() throws IOException {
        Nodo doc = casos();
        int comprobados = 0;
        for (Nodo caso : doc.lista("casos")) {
            int varianza = caso.entero("varianza");
            ProveedorConfiguracion c = config(caso.enteroO("fuerzaFisica", 10), caso.enteroO("fuerzaMagica", 10), varianza);
            int[] motor = valor(caso, c, varianza);
            assertEquals("mínimo de " + caso, caso.entero("min"), motor[0]);
            assertEquals("máximo de " + caso, caso.entero("max"), motor[1]);
            comprobados++;
        }
        assertTrue(comprobados >= 30);
    }
}
