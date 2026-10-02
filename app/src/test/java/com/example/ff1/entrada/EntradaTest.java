package com.example.ff1.entrada;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ff1.dibujo.Escena;

import org.junit.Test;

public class EntradaTest {

    @Test
    public void tocarDevuelveElBotonDeLaZona() {
        Controles c = new Controles();
        for (Boton b : Boton.values()) {
            int[] z = c.zona(b);
            assertEquals(b, c.tocar(z[0] + z[2] / 2, z[1] + z[3] / 2));
        }
        assertNull(c.tocar(10, 10)); // zona de juego, no es un control
        assertNull(c.tocar(-5, 600)); // banda fuera de la rejilla
    }

    @Test
    public void losControlesNoSeSolapanYCabenEnLaFranja() {
        Controles c = new Controles();
        for (Boton a : Boton.values()) {
            int[] z = c.zona(a);
            assertTrue(a + " en la franja", z[1] >= Controles.FRANJA_Y && z[1] + z[3] <= Escena.ALTO);
            assertTrue(a + " dentro del ancho", z[0] >= 0 && z[0] + z[2] <= Escena.ANCHO);
            for (Boton b : Boton.values()) {
                int[] w = c.zona(b);
                boolean solapa = z[0] < w[0] + w[2] && w[0] < z[0] + z[2] && z[1] < w[1] + w[3] && w[1] < z[1] + z[3];
                assertTrue(a + " solapa " + b, a == b || !solapa);
            }
        }
    }

    @Test
    public void dibujaCadaBotonYElEstadoDelAvanceRapido() {
        Escena e = new Escena();
        new Controles().dibujar(e, true, null);
        assertTrue(e.contieneTexto("Aceptar"));
        assertTrue(e.contieneTexto("Rápido: SÍ"));
        e.limpiar();
        new Controles().dibujar(e, false, null);
        assertTrue(e.contieneTexto("Rápido: no"));
    }

    @Test
    public void unToqueEsUnaPulsacion() {
        Entrada en = new Entrada(250, 150);
        en.presionar(Boton.ACEPTAR);
        en.avanzar(1000); // aceptar no se repite al mantener
        en.soltar();
        assertEquals(Boton.ACEPTAR, en.siguiente());
        assertNull(en.siguiente());
    }

    @Test
    public void mantenerUnaDireccionLaRepite() {
        Entrada en = new Entrada(250, 150);
        en.presionar(Boton.DERECHA);
        en.avanzar(249);
        assertEquals(Boton.DERECHA, en.siguiente());
        assertNull(en.siguiente());
        en.avanzar(1); // 250: primera repetición
        en.avanzar(300); // 550: dos más (400 y 550)
        int n = 0;
        while (en.siguiente() != null) {
            n++;
        }
        assertEquals(3, n);
        en.presionar(Boton.DERECHA); // mover el dedo dentro del mismo botón no repite
        assertNull(en.siguiente());
        en.presionar(Boton.ARRIBA); // deslizar a otra dirección la pulsa
        assertEquals(Boton.ARRIBA, en.siguiente());
        en.soltar();
        en.avanzar(1000);
        assertNull(en.siguiente());
    }

    @Test
    public void bucleConvierteTiempoEnPasosFijos() {
        long[] reloj = {1000};
        Bucle b = new Bucle(() -> reloj[0], 50, 4);
        assertEquals(0, b.pasos());
        reloj[0] += 120;
        assertEquals(2, b.pasos()); // sobran 20 ms
        reloj[0] += 30;
        assertEquals(1, b.pasos());
        reloj[0] += 10_000; // pausa larga: como mucho 4 y se descarta el resto
        assertEquals(4, b.pasos());
        reloj[0] += 10;
        assertEquals(0, b.pasos());
        reloj[0] += 5_000;
        b.reanudar();
        assertEquals(0, b.pasos());
    }
}
