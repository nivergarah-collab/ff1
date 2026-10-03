package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.mundo.Mapa;
import com.example.ff1.progresion.Heroe;
import com.example.ff1.pueblo.Servicios;

import org.junit.Test;

/** Pozaluz: salidas entre mapas, tienda, posada y vecinos. */
public class PuebloTest {

    /** Partida nueva con la apertura saltada: el grupo está en Pozaluz, en (6, 5). */
    static Juego enPozaluz() {
        Juego j = JuegoTest.nuevo(1);
        j.pulsar(Boton.ACEPTAR);
        for (int i = 0; i < 50 && j.pantalla() instanceof PantallaEscena; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        return j;
    }

    private static void pulsar(Juego j, Boton b, int veces) {
        for (int i = 0; i < veces; i++) {
            j.pulsar(b);
        }
    }

    static void hastaLaTienda(Juego j) {
        pulsar(j, Boton.DERECHA, 2);
        pulsar(j, Boton.ARRIBA, 3);
        j.pulsar(Boton.ARRIBA); // el mostrador bloquea: solo se gira
        j.pulsar(Boton.ACEPTAR);
    }

    static void hastaLaPosada(Juego j) {
        pulsar(j, Boton.IZQUIERDA, 4);
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ACEPTAR);
    }

    private static void herirA(Heroe h, int danio) {
        com.example.ff1.combate.Combatiente c = h.entrarEnCombate();
        c.recibirDanio(danio);
        h.salirDeCombate(c);
    }

    @Test
    public void laPartidaEmpiezaEnPozaluzYLasSalidasLlevanAlCampoYDeVuelta() {
        Juego j = enPozaluz();
        Partida p = j.partida();
        assertEquals("pozaluz", p.explorador().mapa().id);
        assertEquals(6, p.explorador().x());
        assertEquals(5, p.explorador().y());
        j.pulsar(Boton.IZQUIERDA); // la fuente ocupa (6..7, 3..4): se rodea por la izquierda
        pulsar(j, Boton.ARRIBA, 4); // (5, 1)
        j.pulsar(Boton.DERECHA);
        j.pulsar(Boton.ARRIBA); // (6, 0) es la salida
        assertEquals("campo", p.explorador().mapa().id);
        assertEquals(2, p.explorador().x());
        assertEquals(1, p.explorador().y());
        j.pulsar(Boton.ARRIBA); // (2, 0) devuelve al pueblo
        assertEquals("pozaluz", p.explorador().mapa().id);
        assertEquals(1, p.explorador().y());
        assertTrue(j.pantalla() instanceof PantallaExploracion);
    }

    @Test
    public void enPozaluzNoSaltanEncuentros() {
        Juego j = enPozaluz();
        for (int i = 0; i < 200; i++) {
            j.pulsar(i % 2 == 0 ? Boton.IZQUIERDA : Boton.DERECHA);
        }
        assertTrue(j.pantalla() instanceof PantallaExploracion);
    }

    @Test
    public void aceptarFrenteAlMostradorAbreLaTiendaYCancelarVuelve() {
        Juego j = enPozaluz();
        Pantalla mapa = j.pantalla();
        hastaLaTienda(j);
        assertTrue(j.pantalla() instanceof PantallaTienda);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Tienda de Lupe"));
        j.pulsar(Boton.CANCELAR);
        assertEquals(mapa, j.pantalla());
    }

    @Test
    public void aceptarSinNadaDelanteNoHaceNada() {
        Juego j = enPozaluz();
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaExploracion);
        assertNull(j.partida().lugarDelante());
    }

    @Test
    public void comprarGastaOroYSumaElObjetoYSinOroNoCompra() {
        Juego j = enPozaluz();
        Partida p = j.partida();
        hastaLaTienda(j);
        j.pulsar(Boton.ACEPTAR); // primer objeto: Tónico de raíz (20)
        assertEquals(30, p.oro());
        assertEquals(4, p.inventario().cantidad("tonico-de-raiz"));
        assertEquals(Partida.Compra.SIN_ORO, p.comprar("cordel-de-viento")); // 120 > 30
        assertEquals(30, p.oro());
        assertEquals(Partida.Compra.HECHA, p.comprar("tonico-de-raiz"));
        assertEquals(10, p.oro());
    }

    @Test
    public void noSeCompraMasDelMaximoDeLaBolsa() {
        Partida p = enPozaluz().partida();
        p.inventario().agregar("tonico-de-raiz", 999);
        assertEquals(0, p.inventario().espacio("tonico-de-raiz"));
        assertEquals(Partida.Compra.SIN_ESPACIO, p.comprar("tonico-de-raiz"));
        assertEquals(50, p.oro());
    }

    @Test
    public void venderPagaLaMitadYNoSeVendeLoQueNoSeLleva() {
        Juego j = enPozaluz();
        Partida p = j.partida();
        assertEquals(10, p.precioVenta("tonico-de-raiz"));
        assertEquals(Partida.Venta.HECHA, p.vender("tonico-de-raiz"));
        assertEquals(60, p.oro());
        assertEquals(2, p.inventario().cantidad("tonico-de-raiz"));
        assertEquals(Partida.Venta.NO_LLEVA, p.vender("frasco-de-brasas"));
        assertEquals(60, p.oro());
    }

    @Test
    public void elPorcentajeDeVentaSeAjustaEnCaliente() {
        Partida p = enPozaluz().partida();
        p.config().reemplazar(p.config().actual().con("pueblo.ventaPorCiento", 100));
        assertEquals(20, p.precioVenta("tonico-de-raiz"));
        try {
            p.config().reemplazar(p.config().actual().con("pueblo.ventaPorCiento", 101));
            fail("fuera de rango");
        } catch (ErrorDeDatos esperado) {
            // bien
        }
    }

    @Test
    public void laTiendaPorPantallaCambiaAVenderYVende() {
        Juego j = enPozaluz();
        hastaLaTienda(j);
        PantallaTienda t = (PantallaTienda) j.pantalla();
        j.pulsar(Boton.DERECHA);
        assertTrue(t.vendiendo());
        j.pulsar(Boton.ACEPTAR); // primer objeto del inventario: Tónico de raíz
        assertEquals(60, j.partida().oro());
        assertEquals("Vendes Tónico de raíz.", t.mensaje());
    }

    @Test
    public void laPosadaCuraAlGrupoYLevantaALosCaidosPorElPrecio() {
        Juego j = enPozaluz();
        Partida p = j.partida();
        herirA(p.grupo().get(0), 5);
        herirA(p.grupo().get(1), 9999);
        assertEquals(0, p.grupo().get(1).vida());
        hastaLaPosada(j);
        assertTrue(j.pantalla() instanceof PantallaPosada);
        j.pulsar(Boton.ACEPTAR);
        assertEquals(35, p.oro());
        for (Heroe h : p.grupo()) {
            assertEquals(h.estadisticas().vida, h.vida());
        }
    }

    @Test
    public void laPosadaNoCobraSiNadieLoNecesitaNiSiFaltaOro() {
        Juego j = enPozaluz();
        Partida p = j.partida();
        Servicios.Posada pos = p.servicios().posada("posada-de-casilda");
        assertEquals(Partida.Descanso.NADA_QUE_CURAR, p.descansar(pos));
        assertEquals(50, p.oro());
        herirA(p.grupo().get(2), 3);
        // Se gasta el oro en la tienda hasta no llegar al precio de la posada.
        while (p.oro() >= pos.precio && p.comprar("tonico-de-raiz") == Partida.Compra.HECHA) {
            // compra hasta quedarse corto
        }
        assertTrue(p.oro() < pos.precio);
        assertEquals(Partida.Descanso.SIN_ORO, p.descansar(pos));
        assertTrue(p.grupo().get(2).vida() < p.grupo().get(2).estadisticas().vida);
    }

    @Test
    public void hablarConUnVecinoMuestraSuEscenaYVuelveAlMapa() {
        Juego j = enPozaluz();
        Pantalla mapa = j.pantalla();
        pulsar(j, Boton.DERECHA, 3);
        j.pulsar(Boton.ARRIBA); // (9, 4)
        j.pulsar(Boton.DERECHA); // (10, 4) es la vecina: solo gira
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Ofelia"));
        for (int i = 0; i < 10 && j.pantalla() instanceof PantallaEscena; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        assertEquals(mapa, j.pantalla());
    }

    @Test
    public void elMapaCargaSalidasYLugaresYLosValida() {
        Mapa m = enPozaluz().partida().mapa("pozaluz");
        assertNotNull(m.salidaEn(6, 0));
        assertEquals("campo", m.salidaEn(7, 0).mapa);
        assertEquals(Mapa.LUGAR_TIENDA, m.lugarEn(8, 1).tipo);
        assertFalse(m.pasable(8, 1));
        assertNull(m.salidaEn(1, 1));
        String roto = "{\"tipo\":\"mapa\",\"version\":1,\"id\":\"x\",\"leyenda\":{\".\":{\"nombre\":\"a\",\"pasable\":true},"
                + "\"#\":{\"nombre\":\"b\",\"pasable\":false}},\"filas\":[\".#\"],\"inicio\":{\"x\":0,\"y\":0},"
                + "\"salidas\":[{\"x\":1,\"y\":0,\"mapa\":\"y\",\"destino\":{\"x\":0,\"y\":0}}]}";
        try {
            Mapa.desde(LectorJson.leer(roto));
            fail("una salida sobre una casilla no pasable");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage().contains("salidas"));
        }
        String lugarMalo = roto.replace("\"salidas\":[{\"x\":1,\"y\":0,\"mapa\":\"y\",\"destino\":{\"x\":0,\"y\":0}}]",
                "\"lugares\":[{\"x\":1,\"y\":0,\"tipo\":\"castillo\",\"ref\":\"z\"}]");
        try {
            Mapa.desde(LectorJson.leer(lugarMalo));
            fail("tipo de lugar desconocido");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage().contains("tipo"));
        }
    }

    @Test
    public void unLugarQueApuntaAUnServicioInexistenteSeRechaza() {
        Mapa m = enPozaluz().partida().mapa("pozaluz");
        try {
            Servicios.vacio().validarMapa(m);
            fail("servicios inexistentes");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage().contains("tienda-de-lupe"));
        }
    }

    @Test
    public void cadaLugarDelPuebloTieneSuServicioConNombre() {
        Partida p = enPozaluz().partida();
        assertFalse(p.explorador().mapa().lugares().isEmpty());
        for (Mapa.Lugar l : p.explorador().mapa().lugares()) {
            Servicios.Servicio s = p.servicios().servicio(l);
            assertEquals(l.ref, s.id);
            assertFalse(s.nombre.isEmpty());
        }
    }
}
