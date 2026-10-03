package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ff1.combate.Combate;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.motor.fuentes.FuenteContenido;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;

import java.io.File;

import org.junit.Test;

/**
 * El motor con un segundo paquete de contenido, mínimo y distinto del juego
 * ({@code app/src/test/resources/contenido-minimo}): un héroe, un mapa sin pueblo, sin escena de
 * apertura, un enemigo y un jefe. Todo funciona sin cambiar código.
 */
public class PaqueteMinimoTest {

    static FuenteContenido fuente() {
        File carpeta = new File("app/src/test/resources/contenido-minimo");
        if (!carpeta.isDirectory()) {
            carpeta = new File("src/test/resources/contenido-minimo"); // Gradle corre desde app/
        }
        return new FuenteContenidoJson(new LectorArchivos(carpeta));
    }

    private static void saltarEscena(Juego j) {
        for (int i = 0; i < 20 && (j.pantalla() instanceof PantallaNombres || j.pantalla() instanceof PantallaEscena); i++) {
            j.pulsar(Boton.ACEPTAR);
        }
    }

    @Test
    public void unaPartidaEnteraConOtroPaquete() {
        Juego j = new Juego(fuente(), new AzarSemilla(5));
        Escena titulo = new Escena();
        j.dibujar(titulo);
        assertTrue(titulo.contieneTexto("El faro de la ensenada"));

        j.pulsar(Boton.ACEPTAR); // Nueva partida
        assertTrue(j.pantalla() instanceof PantallaNombres); // un solo héroe
        j.pulsar(Boton.ACEPTAR); // sin apertura: directo al mapa
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        Pantalla mapa = j.pantalla();
        Partida p = j.partida();
        assertEquals("playa", p.explorador().mapa().id);
        assertEquals(1, p.grupo().size());
        assertEquals(2, p.config().actual().entero("combate.ticksPorPaso"));

        // Encuentro por la arena y combate ganado con botín.
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        JuegoTest.jugarAtacando(j, c);
        assertEquals(Combate.Estado.VICTORIA, c.desenlace().estado);
        assertEquals(1, p.inventario().cantidad("remo-viejo"));
        j.pulsar(Boton.ACEPTAR);
        assertEquals(mapa, j.pantalla());

        // Jefe: escena previa, combate sin huida, escena final y regreso.
        p.irAMapa("playa", 3, 2);
        j.pulsar(Boton.ARRIBA); // el faro bloquea: solo se gira
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena);
        saltarEscena(j);
        PantallaCombate jefe = (PantallaCombate) j.pantalla();
        assertEquals("farero-hueco", jefe.combate().enemigos().get(0).definicion().id);
        JuegoTest.jugarAtacando(j, jefe);
        assertEquals(Combate.Estado.VICTORIA, jefe.desenlace().estado);
        j.pulsar(Boton.ACEPTAR);
        saltarEscena(j);
        assertEquals(mapa, j.pantalla());
        assertTrue(p.jefeDerrotado("farero"));
        assertEquals(1, p.explorador().x());
        assertEquals(3, p.explorador().y());

        // Guardado de ida y vuelta.
        String texto = p.guardar();
        assertEquals(texto, Partida.cargar(fuente(), new AzarSemilla(1), texto).guardar());
    }
}
