package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ResultadoAccion;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.fuentes.AzarSemilla;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** El Soterrado: golpe fuerte periódico, combate sin huida y las escenas de antes y después. */
public class JefeTest {

    private static Juego frenteALaGrieta() {
        Juego j = JuegoTest.enExploracion(3);
        j.partida().irAMapa("cantera-baja", 2, 7);
        j.pulsar(Boton.IZQUIERDA); // la grieta bloquea: solo se gira
        return j;
    }

    @Test
    public void elJefeUsaSuGolpeFuerteCadaTresTurnosYEntreMedioAtaca() {
        Partida p = Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(5));
        Combate c = p.empezarCombate(java.util.Collections.singletonList("soterrado"), false);
        List<ResultadoAccion> suyos = new ArrayList<>();
        for (int i = 0; i < 2000 && suyos.size() < 6 && !c.terminado(); i++) {
            Combatiente quien = c.esperarTurno(1000);
            if (quien == null) {
                break;
            }
            if (quien.bando() == com.example.ff1.combate.Bando.HEROE) {
                c.perderTurno();
            } else {
                suyos.add(c.turnoAutomatico());
            }
        }
        assertEquals(6, suyos.size());
        for (int i = 0; i < 6; i++) {
            boolean golpe = (i + 1) % 3 == 0;
            assertEquals("turno " + (i + 1), golpe ? ResultadoAccion.Accion.HABILIDAD : ResultadoAccion.Accion.ATACAR,
                    suyos.get(i).accion);
            if (golpe) {
                assertEquals("sacudida-de-piedra", suyos.get(i).habilidad.id);
                assertTrue(suyos.get(i).cantidad > 0);
            }
        }
    }

    @Test
    public void delJefeNoSeHuye() {
        Partida p = Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(5));
        Combate c = p.empezarCombate(java.util.Collections.singletonList("soterrado"), false);
        c.esperarTurno(1000);
        while (c.turno().bando() != com.example.ff1.combate.Bando.HEROE) {
            c.turnoAutomatico();
            c.esperarTurno(1000);
        }
        assertEquals(ResultadoAccion.Fallo.HUIDA_PROHIBIDA, c.huir().fallo);
    }

    @Test
    public void unGolpeFuerteQueCuestaMagiaSeRechazaAlCargar() throws Exception {
        String roto = PaqueteDelJuego.texto("habilidades.json").replace(
                "\"nombre\": \"Sacudida de piedra\", \"tipo\": \"danio\", \"coste\": 0", "\"nombre\": \"Sacudida de piedra\", \"tipo\": \"danio\", \"coste\": 2");
        String original = PaqueteDelJuego.texto("habilidades.json");
        if (roto.equals(original)) {
            roto = original.replaceAll("(\"id\": \"sacudida-de-piedra\",[^}]*?\"coste\": )0", "$12");
        }
        assertFalse(roto.equals(original));
        try {
            Partida.nueva(PaqueteDelJuego.fuenteCambiando("habilidades.json", roto), new AzarSemilla(1));
            fail("golpe fuerte con coste");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains("golpe fuerte"));
        }
    }

    @Test
    public void laEscenaPreviaLlevaAlCombateYLaVictoriaMuestraElCierreYVuelveAPozaluz() {
        Juego j = frenteALaGrieta();
        Partida p = j.partida();
        Pantalla mapa = j.pantalla();
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena);
        for (int i = 0; i < 20 && j.pantalla() instanceof PantallaEscena; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        assertTrue(j.pantalla() instanceof PantallaCombate);
        PantallaCombate c = (PantallaCombate) j.pantalla();
        Combatiente jefe = c.combate().enemigos().get(0);
        jefe.recibirDanio(jefe.vidaMaxima() - 1);
        JuegoTest.jugarAtacando(j, c);
        assertEquals(Combate.Estado.VICTORIA, c.desenlace().estado);
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena);
        assertTrue(p.jefeDerrotado("soterrado"));
        assertEquals("pozaluz", p.explorador().mapa().id);
        Escena e = new Escena();
        j.dibujar(e);
        for (int i = 0; i < 20 && j.pantalla() instanceof PantallaEscena; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        assertEquals(mapa, j.pantalla());
    }

    @Test
    public void unaVezVencidoLaGrietaYaNoTieneNada() {
        Juego j = frenteALaGrieta();
        j.partida().derrotarJefe(j.partida().servicios().jefe("soterrado"));
        j.partida().irAMapa("cantera-baja", 2, 7);
        j.pulsar(Boton.IZQUIERDA);
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        assertEquals("Ya no queda nada aquí.", ((PantallaExploracion) j.pantalla()).mensaje());
    }
}
