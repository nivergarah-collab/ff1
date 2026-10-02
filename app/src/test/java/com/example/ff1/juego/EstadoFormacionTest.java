package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import java.util.Arrays;

import org.junit.Test;

public class EstadoFormacionTest {

    private static void irA(Juego j, int opcion) {
        j.pulsar(Boton.CANCELAR); // menú
        for (int i = 0; i < opcion; i++) {
            j.pulsar(Boton.ABAJO);
        }
        j.pulsar(Boton.ACEPTAR);
    }

    @Test
    public void laFichaMuestraLosDatosDelHeroeYCambiaConLaCruceta() {
        Juego j = JuegoTest.enExploracion(1);
        j.partida().equipar(j.partida().grupo().get(0), "hoja-de-ensayo");
        irA(j, 3); // Estado
        PantallaEstado p = (PantallaEstado) j.pantalla();
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Bruna"));
        assertTrue(e.contieneTexto("Guardián  Nv 1"));
        assertTrue(e.contieneTexto("En pie"));
        assertTrue(e.contieneTexto("Hoja de ensayo"));
        assertTrue(e.contieneTexto("PV 48/48"));
        assertTrue(e.contieneTexto("Faltan " + j.partida().grupo().get(0).experienciaFaltante()));

        j.pulsar(Boton.ABAJO);
        assertEquals(1, p.heroe());
        j.dibujar(e);
        assertTrue(e.contieneTexto("Tadeo"));
        assertTrue(e.contieneTexto("Chispa, Escarcha, Canción de cuna"));

        j.pulsar(Boton.ARRIBA);
        j.pulsar(Boton.ARRIBA); // da la vuelta
        assertEquals(3, p.heroe());
        j.pulsar(Boton.CANCELAR);
        assertTrue(j.pantalla() instanceof PantallaMenu);
    }

    @Test
    public void unHeroeCaidoSeVeCaido() {
        Juego j = JuegoTest.enExploracion(1);
        Combatiente c = j.partida().grupo().get(0).entrarEnCombate();
        c.recibirDanio(99999);
        j.partida().grupo().get(0).salirDeCombate(c);
        irA(j, 3);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Caído"));
    }

    @Test
    public void laFormacionIntercambiaDosHeroes() {
        Juego j = JuegoTest.enExploracion(1);
        irA(j, 4); // Formación
        PantallaFormacion p = (PantallaFormacion) j.pantalla();
        j.pulsar(Boton.ACEPTAR); // marca a Bruna (1.º)
        assertEquals(0, p.marcado());
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ACEPTAR); // cambia con Ilke (3.º)
        assertEquals(-1, p.marcado());
        assertEquals(Arrays.asList("Ilke", "Tadeo", "Bruna", "Mirta"), nombres(j));
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("1. Ilke"));
        assertTrue(e.contieneTexto("3. Bruna"));
    }

    @Test
    public void cancelarSueltaLaMarcaYDespuesVuelve() {
        Juego j = JuegoTest.enExploracion(1);
        irA(j, 4);
        PantallaFormacion p = (PantallaFormacion) j.pantalla();
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.CANCELAR);
        assertEquals(-1, p.marcado());
        assertTrue(j.pantalla() == p);
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.ACEPTAR); // marcar y soltar sobre sí mismo: no cambia nada
        assertEquals(Arrays.asList("Bruna", "Tadeo", "Ilke", "Mirta"), nombres(j));
        j.pulsar(Boton.CANCELAR);
        assertTrue(j.pantalla() instanceof PantallaMenu);
    }

    @Test
    public void elCombateRespetaElOrdenDeLaFormacion() {
        Juego j = JuegoTest.enExploracion(1);
        j.partida().intercambiarHeroes(0, 3);
        Combate c = j.partida().empezarCombate(Arrays.asList("musgoso"));
        assertEquals("Mirta", c.heroes().get(0).nombre());
        assertEquals("Bruna", c.heroes().get(3).nombre());
        // al terminar, cada héroe recibe su propio resultado aunque el orden haya cambiado
        c.heroes().get(0).recibirDanio(5);
        com.example.ff1.progresion.Heroe mirta = j.partida().grupo().get(0);
        assertEquals("Mirta", mirta.nombre());
        mirta.salirDeCombate(c.heroes().get(0));
        assertEquals(mirta.estadisticas().vida - 5, mirta.vida());
    }

    private static java.util.List<String> nombres(Juego j) {
        java.util.List<String> r = new java.util.ArrayList<>();
        for (com.example.ff1.progresion.Heroe h : j.partida().grupo()) {
            r.add(h.nombre());
        }
        return r;
    }
}
