package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.combate.Combate;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorMemoria;

import java.util.Collections;

import org.junit.Test;

public class JuegoTest {

    static Juego nuevo(long semilla) {
        return new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(semilla));
    }

    static Juego enExploracion(long semilla) {
        Juego j = nuevo(semilla);
        j.pulsar(Boton.ACEPTAR); // Nueva partida
        for (int i = 0; i < 400 && (j.pantalla() instanceof PantallaNombres || j.pantalla() instanceof PantallaEscena); i++) {
            j.pulsar(Boton.ACEPTAR); // salta la escena de apertura
        }
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        j.partida().irAMapa("campo", 6, 8); // las pruebas de exploración y combate parten del campo
        return j;
    }

    /** Va y viene entre el sendero seguro y el matorral hasta que salta un encuentro. */
    static PantallaCombate hastaUnEncuentro(Juego j) {
        for (int i = 0; i < 400 && !(j.pantalla() instanceof PantallaCombate); i++) {
            j.pulsar(i % 2 == 0 ? Boton.IZQUIERDA : Boton.DERECHA);
        }
        assertTrue("no saltó ningún encuentro", j.pantalla() instanceof PantallaCombate);
        return (PantallaCombate) j.pantalla();
    }

    /** Juega el combate atacando siempre al primer enemigo vivo; devuelve los pasos usados. */
    static int jugarAtacando(Juego j, PantallaCombate c) {
        int pasos = 0;
        while (c.fase() != PantallaCombate.Fase.FIN && pasos < 20_000) {
            if (c.fase() == PantallaCombate.Fase.MENU) {
                j.pulsar(Boton.ACEPTAR); // Atacar
                j.pulsar(Boton.ACEPTAR); // primer objetivo
            }
            j.avanzar(Juego.MS_POR_PASO);
            pasos++;
        }
        assertEquals(PantallaCombate.Fase.FIN, c.fase());
        return pasos;
    }

    @Test
    public void empiezaEnElTituloConContinuarDeshabilitado() {
        Juego j = nuevo(1);
        assertTrue(j.pantalla() instanceof PantallaTitulo);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Nueva partida"));
        assertTrue(e.contieneTexto("Continuar"));
        assertTrue(e.contieneTexto("Crónica de la Cantera"));
        j.pulsar(Boton.ABAJO); // Continuar está deshabilitado: el cursor no se mueve
        assertEquals(0, ((PantallaTitulo) j.pantalla()).cursor());
        assertNull(j.partida());
    }

    @Test
    public void nuevaPartidaLlevaALaExploracionConElGrupoInicial() {
        Juego j = enExploracion(1);
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        Partida p = j.partida();
        assertEquals(4, p.grupo().size());
        assertEquals("Bruna", p.grupo().get(0).nombre());
        assertEquals(3, p.inventario().cantidad("tonico-de-raiz"));
        assertEquals(50, p.oro());
        assertEquals(6, p.explorador().x());
        assertEquals(8, p.explorador().y());
    }

    @Test
    public void unPaqueteInvalidoMuestraElErrorSinCerrarElJuego() {
        Juego j = new Juego(new FuenteContenidoJson(new LectorMemoria(Collections.<String, String>emptyMap())),
                new AzarSemilla(1));
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaError);
        assertTrue(((PantallaError) j.pantalla()).mensaje().contains("configuracion.json"));
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaTitulo);
    }

    @Test
    public void elAvanceRapidoSeAlternaEnCualquierPantalla() {
        Juego j = nuevo(1);
        assertFalse(j.rapido());
        j.pulsar(Boton.RAPIDO);
        assertTrue(j.rapido());
        assertTrue(j.pantalla() instanceof PantallaTitulo); // no llega a la pantalla
        j.pulsar(Boton.RAPIDO);
        assertFalse(j.rapido());
    }

    @Test
    public void lasPulsacionesDeLaEntradaLleganAlAvanzar() {
        Juego j = nuevo(1);
        j.entrada().presionar(Boton.ACEPTAR);
        j.entrada().soltar();
        j.avanzar(Juego.MS_POR_PASO);
        assertTrue(j.pantalla() instanceof PantallaNombres); // primero, los nombres
    }

    @Test
    public void unCombateCompletoVuelveAlMapaConRecompensas() {
        Juego j = enExploracion(7);
        Pantalla mapa = j.pantalla();
        PantallaCombate c = hastaUnEncuentro(j);
        jugarAtacando(j, c);
        assertEquals(Combate.Estado.VICTORIA, c.desenlace().estado);
        assertNotNull(c.desenlace().recompensa);
        assertEquals(50 + c.desenlace().recompensa.oro, j.partida().oro());
        assertTrue(j.partida().grupo().get(0).experiencia() > 0);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("¡Victoria!"));
        j.pulsar(Boton.ACEPTAR);
        assertSame(mapa, j.pantalla());
    }
}
