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
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaMenu || j.pantalla() instanceof PantallaObjetos);
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
}
