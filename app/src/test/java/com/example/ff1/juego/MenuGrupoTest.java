package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.ff1.combate.Combatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.progresion.Heroe;

import org.junit.Test;

public class MenuGrupoTest {

    private static PantallaMenu abrirMenu(Juego j) {
        j.pulsar(Boton.CANCELAR);
        assertTrue(j.pantalla() instanceof PantallaMenu);
        return (PantallaMenu) j.pantalla();
    }

    private static void herir(Heroe h, int danio) {
        Combatiente c = h.entrarEnCombate();
        c.recibirDanio(danio);
        h.salirDeCombate(c);
    }

    @Test
    public void elMenuSeAbreConCancelarYVuelveAlMapaSinPerderLaPosicion() {
        Juego j = JuegoTest.enExploracion(1);
        Pantalla mapa = j.pantalla();
        j.pulsar(Boton.ARRIBA);
        int x = j.partida().explorador().x();
        int y = j.partida().explorador().y();
        assertEquals(0, j.profundidad());

        abrirMenu(j);
        assertEquals(1, j.profundidad());
        j.pulsar(Boton.ARRIBA); // el menú no mueve al grupo
        assertEquals(y, j.partida().explorador().y());

        j.pulsar(Boton.CANCELAR);
        assertSame(mapa, j.pantalla());
        assertEquals(0, j.profundidad());
        assertEquals(x, j.partida().explorador().x());
        assertEquals(y, j.partida().explorador().y());
    }

    @Test
    public void cerrarSinNadaDebajoNoHaceNada() {
        Juego j = JuegoTest.enExploracion(1);
        Pantalla p = j.pantalla();
        j.cerrar();
        assertSame(p, j.pantalla());
    }

    @Test
    public void guardarApareceApagadoYElCursorLoSalta() {
        Juego j = JuegoTest.enExploracion(1);
        PantallaMenu m = abrirMenu(j);
        Escena e = new Escena();
        j.dibujar(e);
        for (PantallaMenu.Seccion s : PantallaMenu.Seccion.values()) {
            assertTrue(s.etiqueta, e.contieneTexto(s.etiqueta));
        }
        assertFalse(PantallaMenu.habilitada(PantallaMenu.Seccion.GUARDAR));
        assertEquals(PantallaMenu.Seccion.OBJETOS, m.seccion());
        for (int i = 0; i < 20; i++) {
            j.pulsar(Boton.ABAJO);
            assertTrue(PantallaMenu.habilitada(m.seccion()));
        }
    }

    @Test
    public void salirAlTituloPideConfirmacion() {
        Juego j = JuegoTest.enExploracion(1);
        PantallaMenu m = abrirMenu(j);
        while (m.seccion() != PantallaMenu.Seccion.SALIR) {
            j.pulsar(Boton.ABAJO);
        }
        j.pulsar(Boton.ACEPTAR);
        assertTrue(m.confirmandoSalida());
        j.pulsar(Boton.CANCELAR); // no sale
        assertFalse(m.confirmandoSalida());
        assertSame(m, j.pantalla());
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.ACEPTAR); // confirma
        assertTrue(j.pantalla() instanceof PantallaTitulo);
        assertEquals(0, j.profundidad());
        assertEquals(null, j.partida());
    }

    @Test
    public void usarUnTonicoCuraAlHeroeYGastaUnaUnidad() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe h = j.partida().grupo().get(0);
        herir(h, 20);
        int antes = h.vida();
        int tonicos = j.partida().inventario().cantidad("tonico-de-raiz");
        assertTrue(tonicos > 0);

        abrirMenu(j);
        j.pulsar(Boton.ACEPTAR); // Objetos
        PantallaObjetos p = (PantallaObjetos) j.pantalla();
        j.pulsar(Boton.ACEPTAR); // el primer objeto del inventario
        assertTrue(p.eligiendoHeroe());
        j.pulsar(Boton.ACEPTAR); // primer héroe

        assertTrue(h.vida() > antes);
        assertEquals(tonicos - 1, j.partida().inventario().cantidad("tonico-de-raiz"));
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Bruna usa Tónico de raíz."));
    }

    @Test
    public void conLaVidaLlenaElObjetoNoSeGasta() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe h = j.partida().grupo().get(0);
        int tonicos = j.partida().inventario().cantidad("tonico-de-raiz");
        assertEquals(Partida.Uso.SIN_EFECTO, j.partida().usarObjeto("tonico-de-raiz", h));
        assertEquals(tonicos, j.partida().inventario().cantidad("tonico-de-raiz"));
    }

    @Test
    public void unHeroeCaidoNoSeCuraConUnTonico() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe h = j.partida().grupo().get(0);
        herir(h, 99999);
        assertEquals(0, h.vida());
        assertEquals(Partida.Uso.SIN_EFECTO, j.partida().usarObjeto("tonico-de-raiz", h));
        assertEquals(0, h.vida());
    }

    @Test
    public void losObjetosNoUsablesNoSeAplican() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe h = j.partida().grupo().get(0);
        j.partida().inventario().agregar("hoja-de-ensayo", 1);
        j.partida().inventario().agregar("frasco-de-brasas", 1);
        assertEquals(Partida.Uso.NO_USABLE, j.partida().usarObjeto("hoja-de-ensayo", h)); // equipo
        assertEquals(Partida.Uso.NO_USABLE, j.partida().usarObjeto("frasco-de-brasas", h)); // daña enemigos
        j.partida().inventario().quitar("agua-de-roca", 1);
        assertEquals(Partida.Uso.NO_USABLE, j.partida().usarObjeto("agua-de-roca", h)); // ya no lo lleva
        assertEquals(1, j.partida().inventario().cantidad("frasco-de-brasas"));
    }

    @Test
    public void ultimaUnidadVaciaLaListaYCancelarVuelve() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe h = j.partida().grupo().get(0);
        int n = j.partida().inventario().cantidad("tonico-de-raiz");
        abrirMenu(j);
        j.pulsar(Boton.ACEPTAR);
        PantallaObjetos p = (PantallaObjetos) j.pantalla();
        for (int i = 0; i < n; i++) {
            herir(h, 10);
            if (!p.eligiendoHeroe()) {
                j.pulsar(Boton.ACEPTAR); // objeto
            }
            j.pulsar(Boton.ACEPTAR); // héroe
        }
        assertFalse(p.eligiendoHeroe());
        assertEquals(0, j.partida().inventario().cantidad("tonico-de-raiz"));
        j.pulsar(Boton.CANCELAR);
        assertTrue(j.pantalla() instanceof PantallaMenu);
    }

    // --- Magia fuera de combate ---------------------------------------------------------------

    private static void gastarMagia(Heroe h, int cantidad) {
        Combatiente c = h.entrarEnCombate();
        assertTrue(c.gastarMagia(cantidad));
        h.salirDeCombate(c);
    }

    @Test
    public void unBalsamoCuraAUnAliadoYGastaMagia() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe bruna = j.partida().grupo().get(0);
        Heroe mirta = j.partida().grupo().get(3);
        herir(bruna, 30);
        int vida = bruna.vida();
        int pm = mirta.magia();
        com.example.ff1.combate.Habilidad balsamo = j.partida().catalogo().habilidad("balsamo");
        assertEquals(Partida.Uso.USADO, j.partida().usarHabilidad(mirta, balsamo, bruna));
        assertTrue(bruna.vida() > vida);
        assertEquals(pm - balsamo.coste, mirta.magia());
    }

    @Test
    public void sinEfectoOSinMagiaNoGastaNada() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe bruna = j.partida().grupo().get(0);
        Heroe mirta = j.partida().grupo().get(3);
        com.example.ff1.combate.Habilidad balsamo = j.partida().catalogo().habilidad("balsamo");
        int pm = mirta.magia();
        assertEquals(Partida.Uso.SIN_EFECTO, j.partida().usarHabilidad(mirta, balsamo, bruna)); // vida llena
        assertEquals(pm, mirta.magia());

        herir(bruna, 30);
        gastarMagia(mirta, mirta.magia() - 2);
        assertEquals(Partida.Uso.SIN_MAGIA, j.partida().usarHabilidad(mirta, balsamo, bruna));
        assertEquals(2, mirta.magia());
        assertEquals(bruna.estadisticas().vida - 30, bruna.vida());
    }

    @Test
    public void soloSirvenLasHabilidadesDeAliadosSinEstado() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe mirta = j.partida().grupo().get(3);
        Heroe tadeo = j.partida().grupo().get(1);
        assertEquals(Partida.Uso.NO_USABLE,
                j.partida().usarHabilidad(mirta, j.partida().catalogo().habilidad("muro-de-ramas"), mirta));
        assertEquals(Partida.Uso.NO_USABLE,
                j.partida().usarHabilidad(tadeo, j.partida().catalogo().habilidad("chispa"), mirta));
        assertFalse(Partida.sirveFueraDeCombate(j.partida().catalogo().habilidad("chispa")));
        assertTrue(Partida.sirveFueraDeCombate(j.partida().catalogo().habilidad("balsamo")));
    }

    @Test
    public void unHeroeCaidoNoLanzaMagia() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe mirta = j.partida().grupo().get(3);
        herir(mirta, 99999);
        assertEquals(Partida.Uso.NO_USABLE,
                j.partida().usarHabilidad(mirta, j.partida().catalogo().habilidad("balsamo"), mirta));
    }

    @Test
    public void laPantallaDeMagiaCuraConLaCruceta() {
        Juego j = JuegoTest.enExploracion(1);
        Heroe bruna = j.partida().grupo().get(0);
        Heroe mirta = j.partida().grupo().get(3);
        herir(bruna, 30);
        int vida = bruna.vida();
        PantallaMenu m = abrirMenu(j);
        j.pulsar(Boton.ABAJO); // Magia
        assertEquals(PantallaMenu.Seccion.MAGIA, m.seccion());
        j.pulsar(Boton.ACEPTAR);
        PantallaMagia p = (PantallaMagia) j.pantalla();

        j.pulsar(Boton.ACEPTAR); // Bruna no conoce magia
        assertEquals(PantallaMagia.Fase.HEROE, p.fase());
        assertEquals("Bruna no conoce magia.", p.mensaje());

        j.pulsar(Boton.ARRIBA); // da la vuelta hasta Mirta
        j.pulsar(Boton.ACEPTAR);
        assertEquals(PantallaMagia.Fase.HABILIDAD, p.fase());
        j.pulsar(Boton.ACEPTAR); // Bálsamo
        assertEquals(PantallaMagia.Fase.OBJETIVO, p.fase());
        j.pulsar(Boton.ABAJO); // de Mirta a Bruna (da la vuelta)
        j.pulsar(Boton.ACEPTAR);
        assertTrue(bruna.vida() > vida);
        assertEquals(PantallaMagia.Fase.HABILIDAD, p.fase());
        assertEquals("Mirta usa Bálsamo.", p.mensaje());

        j.pulsar(Boton.ABAJO); // Muro de ramas: no sirve aquí
        j.pulsar(Boton.ACEPTAR);
        assertEquals("Muro de ramas solo se usa en combate.", p.mensaje());
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Muro de ramas"));
        j.pulsar(Boton.CANCELAR); // a elegir héroe
        j.pulsar(Boton.CANCELAR); // al menú
        assertTrue(j.pantalla() instanceof PantallaMenu);
        assertTrue(mirta.magia() < mirta.estadisticas().magia);
    }
}
