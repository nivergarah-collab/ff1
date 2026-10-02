package com.example.ff1.dibujo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EscenaTest {

    @Test
    public void guardaLasOrdenesEnOrdenDeDibujo() {
        Escena e = new Escena()
                .rectangulo(0, 0, 10, 20, 0xFF000000)
                .texto(5, 6, "Hola", 16, 0xFFFFFFFF)
                .casilla(22, 44, 22, 0xFF00FF00, "^")
                .marco(1, 2, 3, 4, 0xFFFF0000);
        assertEquals(4, e.ordenes().size());
        Escena.Orden r = e.ordenes().get(0);
        assertEquals(Escena.Tipo.RECTANGULO, r.tipo);
        assertTrue(r.relleno);
        assertEquals(10, r.ancho);
        assertEquals(20, r.alto);
        Escena.Orden t = e.ordenes().get(1);
        assertEquals("Hola", t.texto);
        assertEquals(16, t.alto);
        assertEquals(Escena.Alineacion.IZQUIERDA, t.alineacion);
        Escena.Orden c = e.ordenes().get(2);
        assertEquals(Escena.Tipo.CASILLA, c.tipo);
        assertEquals(22, c.ancho);
        assertEquals(22, c.alto);
        assertEquals("^", c.texto);
        assertFalse(e.ordenes().get(3).relleno);
    }

    @Test
    public void limpiarVaciaYFiltrarPorTipo() {
        Escena e = new Escena().texto(0, 0, "a", 8, 0).texto(0, 10, "b", 8, 0, Escena.Alineacion.CENTRO)
                .rectangulo(0, 0, 1, 1, 0);
        assertEquals(2, e.de(Escena.Tipo.TEXTO).size());
        assertTrue(e.contieneTexto("b"));
        assertFalse(e.contieneTexto("c"));
        e.limpiar();
        assertTrue(e.ordenes().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaMedidasNegativas() {
        new Escena().rectangulo(0, 0, -1, 5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaTextoNulo() {
        new Escena().texto(0, 0, null, 8, 0);
    }

    @Test
    public void escaladoCentraConBandasLaterales() {
        // pantalla 1080×1920 = 3 × (360×640): sin bandas
        Escalado justo = new Escalado(1080, 1920);
        assertEquals(3f, justo.escala, 1e-6);
        assertEquals(0f, justo.desplazamientoX, 1e-6);
        // pantalla más ancha (1440×1920): escala 3, bandas de 180 px a cada lado
        Escalado ancho = new Escalado(1440, 1920);
        assertEquals(3f, ancho.escala, 1e-6);
        assertEquals(180f, ancho.desplazamientoX, 1e-6);
        assertEquals(0f, ancho.desplazamientoY, 1e-6);
        assertEquals(180f + 30f, ancho.pantallaX(10), 1e-6);
        assertEquals(10, ancho.virtualX(211f));
        assertEquals(-1, ancho.virtualX(179f)); // en la banda, fuera de la rejilla
    }

    @Test
    public void escaladoConBandasArribaYAbajo() {
        Escalado alto = new Escalado(720, 2000); // escala 2, sobran 720 px de alto
        assertEquals(2f, alto.escala, 1e-6);
        assertEquals(360f, alto.desplazamientoY, 1e-6);
        assertEquals(100, alto.virtualY(360f + 200f));
    }
}
