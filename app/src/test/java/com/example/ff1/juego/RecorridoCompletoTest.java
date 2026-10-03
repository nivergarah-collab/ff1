package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.fuentes.AlmacenMemoria;
import com.example.ff1.motor.fuentes.AzarSemilla;

import org.junit.Test;

/**
 * Una partida de principio a fin con el paquete del juego: título, apertura, Pozaluz, encuentro y
 * combate en el campo, puerta con llave, jefe con sus escenas, regreso, guardado y Continuar.
 * Los tramos entre sitios se recorren con {@link Partida#irAMapa}; cada parte tiene su prueba propia.
 */
public class RecorridoCompletoTest {

    private static void saltarEscena(Juego j) {
        for (int i = 0; i < 50 && j.pantalla() instanceof PantallaEscena; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
    }

    @Test
    public void deLaAperturaAlCierreYDeVueltaConContinuar() {
        AlmacenMemoria almacen = new AlmacenMemoria();
        Juego j = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(11), almacen);
        assertFalse(j.hayGuardado());

        j.pulsar(Boton.ACEPTAR); // Nueva partida
        assertTrue(j.pantalla() instanceof PantallaEscena); // apertura
        saltarEscena(j);
        Partida p = j.partida();
        assertEquals("pozaluz", p.explorador().mapa().id);
        Pantalla mapa = j.pantalla();

        // Campo: un encuentro aleatorio y un combate ganado.
        p.irAMapa("campo", 6, 8);
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        JuegoTest.jugarAtacando(j, c);
        assertEquals(Combate.Estado.VICTORIA, c.desenlace().estado);
        j.pulsar(Boton.ACEPTAR);
        assertEquals(mapa, j.pantalla());

        // La puerta de hierro cede porque el grupo lleva la llave.
        p.irAMapa("campo", 9, 1);
        j.pulsar(Boton.ARRIBA);
        assertEquals("cantera-alta", p.explorador().mapa().id);

        // El Soterrado (se le deja con 1 de vida para que la prueba no dependa del balance).
        p.irAMapa("cantera-baja", 2, 7);
        j.pulsar(Boton.IZQUIERDA); // la grieta bloquea: solo se gira
        j.pulsar(Boton.ACEPTAR);
        saltarEscena(j);
        PantallaCombate jefe = (PantallaCombate) j.pantalla();
        Combatiente soterrado = jefe.combate().enemigos().get(0);
        soterrado.recibirDanio(soterrado.vidaMaxima() - 1);
        JuegoTest.jugarAtacando(j, jefe);
        assertEquals(Combate.Estado.VICTORIA, jefe.desenlace().estado);
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena); // cierre
        saltarEscena(j);
        assertEquals(mapa, j.pantalla());
        assertEquals("pozaluz", p.explorador().mapa().id);
        assertTrue(p.jefeDerrotado("soterrado"));

        // Guardar y retomar desde el título de un juego nuevo.
        assertTrue(j.guardar());
        String guardado = p.guardar();
        Juego otro = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(12), almacen);
        assertTrue(otro.hayGuardado());
        otro.pulsar(Boton.ABAJO); // Continuar
        otro.pulsar(Boton.ACEPTAR);
        assertTrue(otro.pantalla() instanceof PantallaExploracion);
        assertEquals(guardado, otro.partida().guardar());
        assertTrue(otro.partida().jefeDerrotado("soterrado"));
    }
}
