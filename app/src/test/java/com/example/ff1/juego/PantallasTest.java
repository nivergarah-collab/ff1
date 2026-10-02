package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import org.junit.Test;

public class PantallasTest {

    /** Avanza hasta que el combate pide una orden a un héroe (o termina). */
    private static void hastaElMenu(Juego j, PantallaCombate c) {
        for (int i = 0; i < 5000 && c.fase() != PantallaCombate.Fase.MENU && c.fase() != PantallaCombate.Fase.FIN; i++) {
            j.avanzar(Juego.MS_POR_PASO);
        }
    }

    @Test
    public void exploracionDibujaElMapaYAlJugador() {
        Juego j = JuegoTest.enExploracion(1);
        Escena e = new Escena();
        j.dibujar(e);
        assertEquals(16 * 10, e.de(Escena.Tipo.CASILLA).size()); // el campo cabe entero
        assertTrue(e.contieneTexto("Bruna"));
        assertTrue(e.contieneTexto("Oro 50"));
        int sendero = 0xFF000000 | 0xB59A62;
        // el grupo empieza en (6, 8), un sendero
        Escena.Orden inicio = e.de(Escena.Tipo.CASILLA).get(8 * 16 + 6);
        assertEquals(sendero, inicio.color);
    }

    @Test
    public void exploracionChocaConLosObstaculos() {
        Juego j = JuegoTest.enExploracion(1);
        j.pulsar(Boton.ABAJO); // (6, 9) es risco
        assertEquals(8, j.partida().explorador().y());
        j.pulsar(Boton.ARRIBA); // (6, 7) es pradera
        assertEquals(7, j.partida().explorador().y());
        j.pulsar(Boton.ACEPTAR); // sin efecto en el mapa
        assertTrue(j.pantalla() instanceof PantallaExploracion);
    }

    @Test
    public void camaraCentraSinSalirseDelMapa() {
        assertEquals(0, PantallaExploracion.origen(3, 40, 16));
        assertEquals(12, PantallaExploracion.origen(20, 40, 16));
        assertEquals(24, PantallaExploracion.origen(39, 40, 16));
        assertEquals(0, PantallaExploracion.origen(5, 10, 10));
    }

    @Test
    public void elMenuDelGuardianNoTieneMagiaYCancelarVuelve() {
        Juego j = JuegoTest.enExploracion(7);
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        hastaElMenu(j, c);
        Combatiente actor = c.actor();
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Turno de " + actor.nombre()));
        assertTrue(e.contieneTexto("Huir"));
        boolean conMagia = !actor.definicion().habilidades.isEmpty();
        assertEquals(conMagia, c.menuHabilitado().get(1));
        j.pulsar(Boton.ACEPTAR); // Atacar → elegir objetivo
        assertEquals(PantallaCombate.Fase.OBJETIVO, c.fase());
        j.pulsar(Boton.CANCELAR);
        assertEquals(PantallaCombate.Fase.MENU, c.fase());
    }

    @Test
    public void usarUnObjetoLoGastaDelInventario() {
        Juego j = JuegoTest.enExploracion(7);
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        hastaElMenu(j, c);
        int antes = j.partida().inventario().cantidad("tonico-de-raiz");
        j.pulsar(Boton.ABAJO);
        if (!c.menuHabilitado().get(1)) {
            assertEquals(2, c.cursor()); // Magia deshabilitada: salta a Objeto
        } else {
            j.pulsar(Boton.ABAJO);
        }
        j.pulsar(Boton.ACEPTAR); // Objeto
        assertEquals(PantallaCombate.Fase.OBJETO, c.fase());
        j.pulsar(Boton.ACEPTAR); // primer consumible: tónico de raíz
        assertEquals(PantallaCombate.Fase.OBJETIVO, c.fase());
        j.pulsar(Boton.ACEPTAR); // primer aliado
        assertEquals(PantallaCombate.Fase.MENSAJE, c.fase());
        assertEquals(antes - 1, j.partida().inventario().cantidad("tonico-de-raiz"));
        assertTrue(c.mensaje(), c.mensaje().contains("Tónico de raíz"));
    }

    @Test
    public void elAvanceRapidoAceleraElCombateYSeVe() {
        Juego lento = JuegoTest.enExploracion(7);
        PantallaCombate cLento = JuegoTest.hastaUnEncuentro(lento);
        int pasosLento = JuegoTest.jugarAtacando(lento, cLento);
        Juego rapido = JuegoTest.enExploracion(7);
        rapido.pulsar(Boton.RAPIDO);
        PantallaCombate c = JuegoTest.hastaUnEncuentro(rapido);
        Escena e = new Escena();
        rapido.avanzar(Juego.MS_POR_PASO);
        rapido.dibujar(e);
        assertTrue(e.contieneTexto("Avance rápido x2"));
        assertTrue(e.contieneTexto("Rápido: SÍ"));
        int pasosRapido = JuegoTest.jugarAtacando(rapido, c);
        assertTrue(pasosRapido + " < " + pasosLento, pasosRapido < pasosLento);
        assertEquals(cLento.desenlace().estado, c.desenlace().estado); // mismas reglas, más rápido
    }

    @Test
    public void huirVuelveAlMapaSinRecompensa() {
        Juego j = JuegoTest.enExploracion(7);
        Pantalla mapa = j.pantalla();
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        for (int i = 0; i < 20_000 && c.fase() != PantallaCombate.Fase.FIN; i++) {
            if (c.fase() == PantallaCombate.Fase.MENU) {
                j.pulsar(Boton.ARRIBA); // da la vuelta hasta Huir
                j.pulsar(Boton.ACEPTAR);
            }
            j.avanzar(Juego.MS_POR_PASO);
        }
        assertEquals(PantallaCombate.Fase.FIN, c.fase());
        assertEquals(Combate.Estado.HUIDA, c.desenlace().estado);
        {
            assertNull(c.desenlace().recompensa);
            assertEquals(50, j.partida().oro());
            j.pulsar(Boton.ACEPTAR);
            assertSame(mapa, j.pantalla());
        }
    }
}
