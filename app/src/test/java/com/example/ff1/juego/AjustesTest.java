package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import org.junit.Test;

public class AjustesTest {

    private static PantallaAjustes abrir(Juego j) {
        j.pulsar(Boton.CANCELAR);
        for (int i = 0; i < 5; i++) {
            j.pulsar(Boton.ABAJO); // Ajustes
        }
        j.pulsar(Boton.ACEPTAR);
        return (PantallaAjustes) j.pantalla();
    }

    @Test
    public void laVelocidadDelCombateSeCambiaEnCaliente() {
        Juego j = JuegoTest.enExploracion(1);
        int antes = j.partida().config().actual().entero(ConfiguracionCombate.TICKS_POR_PASO);
        final int[] avisos = {0};
        j.partida().config().alCambiar(c -> avisos[0]++);
        abrir(j);
        j.pulsar(Boton.DERECHA);
        j.pulsar(Boton.DERECHA);
        assertEquals(antes + 2, j.partida().config().actual().entero(ConfiguracionCombate.TICKS_POR_PASO));
        assertEquals(2, avisos[0]); // los oyentes se enteran: es en caliente
        j.pulsar(Boton.IZQUIERDA);
        assertEquals(antes + 1, j.partida().config().actual().entero(ConfiguracionCombate.TICKS_POR_PASO));
    }

    @Test
    public void enElLimiteElValorNoCambiaNiFalla() {
        Juego j = JuegoTest.enExploracion(1);
        PantallaAjustes p = abrir(j);
        for (int i = 0; i < 200; i++) {
            j.pulsar(Boton.IZQUIERDA);
        }
        assertEquals(1, p.valor(PantallaAjustes.Ajuste.COMBATE));
        for (int i = 0; i < 200; i++) {
            j.pulsar(Boton.DERECHA);
        }
        assertEquals(100, p.valor(PantallaAjustes.Ajuste.COMBATE));
    }

    /** Pasos que dura el primer mensaje de un combate con ese tiempo de mensajes. */
    private static int pasosDelPrimerMensaje(int ms) {
        Juego j = JuegoTest.enExploracion(7);
        j.partida().config().reemplazar(j.partida().config().actual().con(ConfiguracionJuego.MS_MENSAJE, ms));
        PantallaCombate c = JuegoTest.hastaUnEncuentro(j);
        for (int i = 0; i < 20_000 && c.fase() != PantallaCombate.Fase.MENSAJE; i++) {
            j.avanzar(Juego.MS_POR_PASO);
        }
        assertEquals(PantallaCombate.Fase.MENSAJE, c.fase());
        int pasos = 0;
        while (c.fase() == PantallaCombate.Fase.MENSAJE && pasos < 1000) {
            j.avanzar(Juego.MS_POR_PASO);
            pasos++;
        }
        return pasos;
    }

    @Test
    public void elTiempoDeMensajesCambiaElRitmoDelCombate() {
        int corto = pasosDelPrimerMensaje(100);
        int largo = pasosDelPrimerMensaje(1000);
        assertTrue(corto + " debería ser menor que " + largo, corto < largo);
        assertEquals(2, corto);
        assertEquals(20, largo);
    }

    @Test
    public void elAvanceRapidoAlEmpezarSeAplicaEnLaPartidaNueva() {
        Juego j = JuegoTest.nuevo(1);
        j.pulsar(Boton.ACEPTAR);
        assertFalse(j.rapido());
        PantallaAjustes p = abrir(j);
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.ABAJO); // Avance rápido al empezar
        j.pulsar(Boton.ACEPTAR);
        assertEquals(1, p.valor(PantallaAjustes.Ajuste.RAPIDO));
        assertFalse(j.rapido()); // no cambia el interruptor de ahora
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Sí"));
        j.pulsar(Boton.ACEPTAR); // vuelve a No
        assertEquals(0, p.valor(PantallaAjustes.Ajuste.RAPIDO));
    }

    @Test
    public void unPaqueteConAvanceRapidoAlEmpezarArrancaRapido() {
        String config = "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"juego.rapidoAlEmpezar\":1}}";
        Juego j = new Juego(com.example.ff1.PaqueteDelJuego.fuenteConConfiguracion(config),
                new com.example.ff1.motor.fuentes.AzarSemilla(1));
        assertFalse(j.rapido());
        j.pulsar(Boton.ACEPTAR); // nueva partida
        assertTrue(j.rapido());
        j.pulsar(Boton.RAPIDO); // el interruptor sigue funcionando
        assertFalse(j.rapido());
    }

    @Test
    public void laPantallaMuestraLosTresAjustes() {
        Juego j = JuegoTest.enExploracion(1);
        abrir(j);
        Escena e = new Escena();
        j.dibujar(e);
        for (PantallaAjustes.Ajuste a : PantallaAjustes.Ajuste.values()) {
            assertTrue(a.etiqueta, e.contieneTexto(a.etiqueta));
        }
        j.pulsar(Boton.CANCELAR);
        assertTrue(j.pantalla() instanceof PantallaMenu);
    }
}
