package com.example.ff1.mundo;

import com.example.ff1.PaqueteDelJuego;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import org.junit.Test;

import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;

public class MapaTest {

    /** 5 × 4: muro alrededor, un árbol en (2, 1) y agua en (3, 2). */
    private static final String PEQUENO = "{\"tipo\":\"mapa\",\"version\":1,\"id\":\"p\","
            + "\"leyenda\":{\".\":{\"nombre\":\"suelo\",\"pasable\":true},"
            + "\"#\":{\"nombre\":\"muro\",\"pasable\":false},"
            + "\"T\":{\"nombre\":\"arbol\",\"pasable\":false},"
            + "\"~\":{\"nombre\":\"agua\",\"pasable\":false}},"
            + "\"filas\":[\"#####\",\"#.T.#\",\"#..~#\",\"#####\"],"
            + "\"inicio\":{\"x\":1,\"y\":1}}";

    private static Mapa pequeno() {
        return Mapa.desde(LectorJson.leer(PEQUENO));
    }

    private static void rechaza(String json, String fragmento) {
        try {
            Mapa.desde(LectorJson.leer(json));
            fail("se esperaba ErrorDeDatos con " + fragmento);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(fragmento));
        }
    }

    @Test
    public void leeDimensionesLeyendaEInicio() {
        Mapa m = pequeno();
        assertEquals(5, m.ancho);
        assertEquals(4, m.alto);
        assertEquals("arbol", m.casilla(2, 1).nombre);
        assertTrue(m.pasable(1, 2));
        assertFalse(m.pasable(3, 2));
        assertFalse(m.pasable(-1, 0));
        assertFalse(m.pasable(5, 1));
    }

    @Test
    public void rechazaFilasDeDistintoAncho() {
        rechaza(PEQUENO.replace("\"#..~#\"", "\"#..~\""), "mismo ancho");
    }

    @Test
    public void rechazaSimboloFueraDeLaLeyenda() {
        rechaza(PEQUENO.replace("\"#..~#\"", "\"#..X#\""), "'X'");
    }

    @Test
    public void rechazaInicioNoPasable() {
        rechaza(PEQUENO.replace("\"x\":1,\"y\":1", "\"x\":2,\"y\":1"), "no es una casilla pasable");
        rechaza(PEQUENO.replace("\"x\":1,\"y\":1", "\"x\":9,\"y\":1"), "no es una casilla pasable");
    }

    @Test
    public void rechazaClaveDeLeyendaLarga() {
        rechaza(PEQUENO.replace("\"T\":{", "\"TT\":{"), "un solo carácter");
    }

    @Test
    public void exploradorSeMueveYChocaConObstaculosYBordes() {
        Explorador e = new Explorador(pequeno());
        assertEquals(1, e.x());
        assertFalse(e.mover(Direccion.DERECHA)); // árbol
        assertEquals(Direccion.DERECHA, e.mirando());
        assertFalse(e.mover(Direccion.ARRIBA)); // muro
        assertTrue(e.mover(Direccion.ABAJO));
        assertTrue(e.mover(Direccion.DERECHA));
        assertFalse(e.mover(Direccion.DERECHA)); // agua
        assertEquals(2, e.x());
        assertEquals(2, e.y());
        assertEquals(2, e.pasos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void colocarEnCasillaNoPasableFalla() {
        new Explorador(pequeno()).colocar(pequeno(), 0, 0);
    }

    @Test
    public void elMapaDelPaqueteEsValido() {
        Mapa m = Mapa.cargar(PaqueteDelJuego.fuente(), "campo");
        assertTrue(m.pasable(m.inicioX, m.inicioY));
        assertEquals(16, m.ancho);
    }
}
