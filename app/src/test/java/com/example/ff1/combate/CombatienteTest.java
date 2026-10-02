package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CombatienteTest {

    private static Combatiente nuevo() {
        return new Combatiente(Datos.definicion("prueba", Bando.HEROE, 30, 10, 10), "Ilse");
    }

    @Test
    public void empiezaConVidaYMagiaMaximasYNombrePropio() {
        Combatiente c = nuevo();
        assertEquals(30, c.vida());
        assertEquals(10, c.magia());
        assertEquals("Ilse", c.nombre());
        assertTrue(c.vivo());
    }

    @Test
    public void elDanioNoBajaDeCeroYDevuelveLoHecho() {
        Combatiente c = nuevo();
        assertEquals(30, c.recibirDanio(99));
        assertEquals(0, c.vida());
        assertFalse(c.vivo());
    }

    @Test
    public void laCuracionNoPasaDelMaximoYNoReviveAlCaido() {
        Combatiente c = nuevo();
        c.recibirDanio(5);
        assertEquals(5, c.curar(50));
        assertEquals(30, c.vida());
        c.recibirDanio(30);
        assertEquals(0, c.curar(10));
        assertEquals(0, c.vida());
    }

    @Test
    public void sinMagiaSuficienteNoGastaNada() {
        Combatiente c = nuevo();
        assertFalse(c.gastarMagia(11));
        assertEquals(10, c.magia());
        assertTrue(c.gastarMagia(4));
        assertEquals(6, c.magia());
        assertEquals(4, c.recuperarMagia(9));
    }

    @Test
    public void estadoRenovadoConservaLaDuracionMayorYSeAgotaPorTurnos() {
        Combatiente c = nuevo();
        c.aplicarEstado("veneno", 2);
        c.aplicarEstado("veneno", 1);
        assertEquals(Integer.valueOf(2), c.estados().get("veneno"));
        c.terminarTurno();
        assertTrue(c.tieneEstado("veneno"));
        c.terminarTurno();
        assertFalse(c.tieneEstado("veneno"));
    }

    @Test
    public void alCaerPierdeLosEstadosYNoRecibeNuevos() {
        Combatiente c = nuevo();
        c.aplicarEstado("proteccion", 3);
        c.recibirDanio(30);
        assertTrue(c.estados().isEmpty());
        c.aplicarEstado("sueno", 2);
        assertTrue(c.estados().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void danioNegativoEsUnError() {
        nuevo().recibirDanio(-1);
    }
}
