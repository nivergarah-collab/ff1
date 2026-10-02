package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.example.ff1.motor.config.ProveedorConfiguracion;

public class BarraTiempoTest {

    /** Ticks hasta que {@code c} queda listo, solo, en una barra nueva. */
    private static int ticksHastaListo(ProveedorConfiguracion config, Combatiente c) {
        BarraTiempo barra = new BarraTiempo(config);
        barra.inscribir(c);
        int ticks = 0;
        while (barra.avanzar(1).isEmpty()) {
            ticks++;
        }
        return ticks + 1;
    }

    @Test
    public void conValoresPorDefectoVelocidadDiezTardaCienTicks() {
        assertEquals(100, ticksHastaListo(Datos.config(), Datos.combatiente("a", 10)));
    }

    @Test
    public void elMasRapidoActuaPrimero() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente lento = Datos.combatiente("lento", 5);
        Combatiente rapido = Datos.combatiente("rapido", 20);
        barra.inscribir(lento);
        barra.inscribir(rapido);
        List<Combatiente> listos = barra.avanzar(50);
        assertEquals(Arrays.asList(rapido), listos);
        assertSame(rapido, barra.siguiente());
        assertEquals(250, barra.carga(lento));
    }

    @Test
    public void aIgualdadDecideElOrdenDeInscripcion() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente a = Datos.combatiente("a", 10);
        Combatiente b = Datos.combatiente("b", 10);
        barra.inscribir(b);
        barra.inscribir(a);
        assertEquals(Arrays.asList(b, a), barra.avanzar(100));
    }

    @Test
    public void enElMismoTickVaPrimeroElQueMasSePaso() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente justo = Datos.combatiente("justo", 10);
        Combatiente sobrado = Datos.combatiente("sobrado", 10);
        barra.inscribir(justo, 990);
        barra.inscribir(sobrado, 995);
        assertEquals(Arrays.asList(sobrado, justo), barra.avanzar(1));
    }

    @Test
    public void quienEsperaTurnoNoSigueCargandoYAlActuarVuelveACero() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente c = Datos.combatiente("c", 10);
        barra.inscribir(c);
        barra.avanzar(150);
        assertEquals(1000, barra.carga(c));
        assertEquals(1, barra.cola().size());
        barra.consumirTurno(c);
        assertEquals(0, barra.carga(c));
        assertFalse(barra.listo(c));
        assertTrue(barra.avanzar(99).isEmpty());
        assertEquals(1, barra.avanzar(1).size());
    }

    @Test
    public void elCaidoSaleDeLaColaYNoCarga() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente c = Datos.combatiente("c", 10);
        barra.inscribir(c);
        barra.avanzar(100);
        c.recibirDanio(999);
        assertNull(barra.siguiente());
        assertTrue(barra.avanzar(300).isEmpty());
        assertEquals(0, barra.carga(c));
    }

    @Test
    public void cambiarLaConfiguracionEnCalienteCambiaElRitmo() {
        ProveedorConfiguracion config = Datos.config();
        Combatiente c = Datos.combatiente("c", 10);
        assertEquals(100, ticksHastaListo(config, c));
        config.reemplazar(config.actual().con(ConfiguracionCombate.VELOCIDAD_BARRA, 20));
        assertEquals(50, ticksHastaListo(config, c));
        config.reemplazar(config.actual().con(ConfiguracionCombate.CARGA_LLENA, 500));
        assertEquals(25, ticksHastaListo(config, c));
    }

    @Test
    public void elCambioSeAplicaAMitadDelLlenado() {
        ProveedorConfiguracion config = Datos.config();
        BarraTiempo barra = new BarraTiempo(config);
        Combatiente c = Datos.combatiente("c", 10);
        barra.inscribir(c);
        barra.avanzar(50);
        config.reemplazar(config.actual().con(ConfiguracionCombate.VELOCIDAD_BARRA, 50));
        assertTrue(barra.avanzar(9).isEmpty());
        assertEquals(1, barra.avanzar(1).size());
    }

    @Test
    public void elIncrementoMinimoEsUno() {
        ProveedorConfiguracion config = Datos.config();
        config.reemplazar(config.actual().con(ConfiguracionCombate.VELOCIDAD_BARRA, 1));
        BarraTiempo barra = new BarraTiempo(config);
        Combatiente c = Datos.combatiente("c", 1);
        barra.inscribir(c);
        assertEquals(1, barra.incremento(c));
    }

    @Test
    public void lasMismasEntradasDanLaMismaCola() {
        for (int vez = 0; vez < 2; vez++) {
            BarraTiempo barra = new BarraTiempo(Datos.config());
            Combatiente a = Datos.combatiente("a", 7);
            Combatiente b = Datos.combatiente("b", 9);
            barra.inscribir(a);
            barra.inscribir(b);
            assertEquals(Arrays.asList(b, a), barra.avanzar(143));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void consumirSinEstarListoEsUnError() {
        BarraTiempo barra = new BarraTiempo(Datos.config());
        Combatiente c = Datos.combatiente("c", 10);
        barra.inscribir(c);
        barra.consumirTurno(c);
    }
}
