package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.progresion.Heroe;

import org.junit.Test;

public class EquipoMenuTest {

    private static Partida partida() {
        return JuegoTest.enExploracion(1).partida();
    }

    @Test
    public void equiparQuitaDelInventarioYSubeLasEstadisticas() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0);
        int ataque = bruna.estadisticas().ataque;
        assertEquals(Partida.Cambio.HECHO, p.equipar(bruna, "hoja-de-ensayo"));
        assertEquals(ataque + 4, bruna.estadisticas().ataque);
        assertEquals(0, p.inventario().cantidad("hoja-de-ensayo"));
        assertEquals("hoja-de-ensayo", bruna.equipo().get("arma").id);
    }

    @Test
    public void quitarUnaPiezaLaDevuelveAlInventario() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0);
        p.equipar(bruna, "hoja-de-ensayo");
        assertEquals(0, p.inventario().cantidad("hoja-de-ensayo"));
        assertEquals(Partida.Cambio.HECHO, p.quitarEquipo(bruna, "arma"));
        assertEquals(1, p.inventario().cantidad("hoja-de-ensayo"));
        assertNull(bruna.equipo().get("arma"));
    }

    @Test
    public void laClaseLimitaLasPiezasYSeNecesitaTenerlas() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0); // guardián: la vara es de arcanista y herbolaria
        assertEquals(Partida.Cambio.NO_PUEDE, p.equipar(bruna, "vara-de-sauce"));
        assertEquals(Partida.Cambio.NO_PUEDE, p.equipar(bruna, "tonico-de-raiz")); // no es equipo
        p.inventario().quitar("jubon-acolchado", 2);
        assertEquals(Partida.Cambio.NO_LLEVA, p.equipar(bruna, "jubon-acolchado"));
        assertEquals(1, p.inventario().cantidad("vara-de-sauce"));
        assertEquals(Partida.Cambio.NO_PUEDE, p.quitarEquipo(bruna, "arma")); // ranura vacía
    }

    @Test
    public void reemplazarUnaPiezaDevuelveLaQueLlevaba() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0);
        p.equipar(bruna, "jubon-acolchado");
        assertEquals(1, p.inventario().cantidad("jubon-acolchado"));
        assertEquals(Partida.Cambio.HECHO, p.equipar(bruna, "jubon-acolchado")); // la misma: queda igual
        assertEquals(1, p.inventario().cantidad("jubon-acolchado"));
        assertEquals("jubon-acolchado", bruna.equipo().get("armadura").id);
    }

    @Test
    public void laVidaActualSeAjustaAlQuitarUnBonoDeVida() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0);
        assertEquals(bruna.estadisticas().vida, bruna.vida());
        int antes = bruna.estadisticas().defensa;
        p.equipar(bruna, "jubon-acolchado");
        assertEquals(antes + 3, bruna.estadisticas().defensa);
        p.quitarEquipo(bruna, "armadura");
        assertEquals(antes, bruna.estadisticas().defensa);
    }

    @Test
    public void laComparacionMuestraElCambioSinAplicarlo() {
        Partida p = partida();
        Heroe bruna = p.grupo().get(0);
        int[] d = Comparacion.diferencias(bruna.estadisticas(),
                bruna.estadisticasCon("arma", p.objetos().objeto("hoja-de-ensayo")));
        assertEquals(4, d[2]); // ataque
        assertEquals(0, d[3]);
        assertNull(bruna.equipo().get("arma")); // solo se compara
        assertEquals("+4", Comparacion.signo(4));
        assertEquals("-2", Comparacion.signo(-2));
        assertEquals("=", Comparacion.signo(0));
    }

    @Test
    public void laPantallaEquipaConComparacionYQuita() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe bruna = j.partida().grupo().get(0);
        j.pulsar(Boton.CANCELAR); // menú
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ABAJO); // Equipo
        j.pulsar(Boton.ACEPTAR);
        PantallaEquipo pe = (PantallaEquipo) j.pantalla();
        j.pulsar(Boton.ACEPTAR); // Bruna
        assertEquals(PantallaEquipo.Fase.RANURA, pe.fase());
        j.pulsar(Boton.ACEPTAR); // Arma: solo la hoja de ensayo
        assertEquals(PantallaEquipo.Fase.PIEZA, pe.fase());
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Hoja de ensayo"));
        assertTrue(e.contieneTexto("+4")); // comparación antes de confirmar
        assertNull(bruna.equipo().get("arma"));

        j.pulsar(Boton.ACEPTAR);
        assertEquals("hoja-de-ensayo", bruna.equipo().get("arma").id);
        assertEquals(PantallaEquipo.Fase.RANURA, pe.fase());

        j.pulsar(Boton.ACEPTAR); // otra vez Arma: ahora solo "(Quitar)"
        j.dibujar(e);
        assertTrue(e.contieneTexto("(Quitar)"));
        j.pulsar(Boton.ACEPTAR);
        assertNull(bruna.equipo().get("arma"));
        assertEquals(1, j.partida().inventario().cantidad("hoja-de-ensayo"));
    }

    @Test
    public void cadaHeroeVeSoloLasPiezasQuePuedeLlevar() {
        Juego j = JuegoTest.enExploracion(1);
        j.pulsar(Boton.CANCELAR);
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ACEPTAR);
        PantallaEquipo pe = (PantallaEquipo) j.pantalla();
        j.pulsar(Boton.ACEPTAR); // Bruna
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ABAJO); // Accesorio: hay un cordel, sin clases
        j.pulsar(Boton.ACEPTAR);
        assertEquals(PantallaEquipo.Fase.PIEZA, pe.fase());
        j.pulsar(Boton.CANCELAR);
        j.pulsar(Boton.CANCELAR); // al héroe; Tadeo (arcanista) no puede llevar la hoja de ensayo
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.ACEPTAR); // Arma: solo la vara de sauce
        assertEquals(PantallaEquipo.Fase.PIEZA, pe.fase());
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Vara de sauce"));
        assertTrue(!e.contieneTexto("Hoja de ensayo"));
    }
}
