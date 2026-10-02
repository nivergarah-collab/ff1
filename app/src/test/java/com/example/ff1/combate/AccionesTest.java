package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.AzarSecuencia;

public class AccionesTest {

    private final ProveedorConfiguracion config = Datos.config();

    private Acciones acciones(Azar azar) {
        return new Acciones(config, azar, Datos.tiposHabilidad(), Datos.estados());
    }

    /** Sin varianza: el azar no interviene en el daño. */
    private Acciones exactas() {
        config.reemplazar(config.actual().con(ConfiguracionCombate.VARIANZA, 0));
        return acciones(new AzarSecuencia(0));
    }

    private static Combatiente heroe(int ataque, int defensa, int poder, int magia) {
        return new Combatiente(Datos.definicion("heroe", Bando.HEROE, 40, magia, ataque, defensa, poder, 10));
    }

    private static Combatiente enemigo(int ataque, int defensa, int velocidad) {
        return new Combatiente(Datos.definicion("enemigo", Bando.ENEMIGO, 40, 0, ataque, defensa, 0, velocidad));
    }

    private static Habilidad habilidad(String tipo, int coste, int poder, Habilidad.Objetivo obj,
            String estado, int duracion) {
        return new Habilidad("h", "h", tipo, coste, poder, obj, estado, duracion);
    }

    @Test
    public void atacarRestaAtaqueMenosMitadDeDefensa() {
        Combatiente e = enemigo(5, 4, 10);
        ResultadoAccion r = exactas().atacar(heroe(12, 0, 0, 0), e);
        assertTrue(r.exito());
        assertEquals(10, r.cantidad);
        assertEquals(30, e.vida());
    }

    @Test
    public void elDanioFisicoEsAlMenosUno() {
        assertEquals(1, exactas().atacar(heroe(2, 0, 0, 0), enemigo(5, 40, 10)).cantidad);
    }

    @Test
    public void laVarianzaVieneDelAzar() {
        Combatiente h = heroe(12, 0, 0, 0);
        assertEquals(9, acciones(new AzarSecuencia(0)).atacar(h, enemigo(5, 4, 10)).cantidad);
        assertEquals(10, acciones(new AzarSecuencia(10)).atacar(h, enemigo(5, 4, 10)).cantidad);
        assertEquals(11, acciones(new AzarSecuencia(20)).atacar(h, enemigo(5, 4, 10)).cantidad);
    }

    @Test
    public void cambiarLaFuerzaFisicaEnCalienteCambiaElDanio() {
        Acciones a = exactas();
        config.reemplazar(config.actual().con(ConfiguracionCombate.FUERZA_FISICA, 20));
        assertEquals(22, a.atacar(heroe(12, 0, 0, 0), enemigo(5, 4, 10)).cantidad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSePuedeAtacarAUnAliado() {
        exactas().atacar(heroe(5, 0, 0, 0), heroe(5, 0, 0, 0));
    }

    @Test
    public void habilidadDeDanioGastaMagiaYSumaElPoderDelActor() {
        Combatiente h = heroe(5, 0, 11, 24);
        Combatiente e = enemigo(5, 4, 10);
        ResultadoAccion r = exactas().usarHabilidad(h, habilidad("danio", 4, 14, Habilidad.Objetivo.ENEMIGO, null, 0), e);
        assertEquals(ResultadoAccion.Accion.HABILIDAD, r.accion);
        assertEquals(24, r.cantidad); // 14 + 11 - 4/4
        assertEquals(20, h.magia());
    }

    @Test
    public void sinMagiaSuficienteFallaSinCambiarNada() {
        Combatiente h = heroe(5, 0, 11, 3);
        Combatiente e = enemigo(5, 4, 10);
        ResultadoAccion r = exactas().usarHabilidad(h, habilidad("danio", 4, 14, Habilidad.Objetivo.ENEMIGO, null, 0), e);
        assertEquals(ResultadoAccion.Fallo.SIN_MAGIA, r.fallo);
        assertEquals(3, h.magia());
        assertEquals(40, e.vida());
    }

    @Test
    public void sobreUnObjetivoCaidoFallaSinGastarMagia() {
        Combatiente h = heroe(5, 0, 11, 10);
        Combatiente e = enemigo(5, 4, 10);
        e.recibirDanio(40);
        ResultadoAccion r = exactas().usarHabilidad(h, habilidad("danio", 4, 14, Habilidad.Objetivo.ENEMIGO, null, 0), e);
        assertEquals(ResultadoAccion.Fallo.OBJETIVO_CAIDO, r.fallo);
        assertEquals(10, h.magia());
    }

    @Test
    public void laCuracionNoPasaDelMaximo() {
        Combatiente sanador = heroe(5, 0, 10, 20);
        Combatiente herido = heroe(5, 0, 0, 0);
        herido.recibirDanio(10);
        ResultadoAccion r = exactas().usarHabilidad(sanador, habilidad("curacion", 5, 18, Habilidad.Objetivo.ALIADO, null, 0), herido);
        assertEquals(10, r.cantidad);
        assertEquals(40, herido.vida());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unaCuracionDeAliadoNoSeUsaSobreUnEnemigo() {
        exactas().usarHabilidad(heroe(5, 0, 10, 20),
                habilidad("curacion", 5, 18, Habilidad.Objetivo.ALIADO, null, 0), enemigo(5, 0, 10));
    }

    @Test
    public void unObjetoNoGastaMagiaNiUsaElPoderDelActor() {
        Combatiente h = heroe(5, 0, 10, 0);
        h.recibirDanio(30);
        ResultadoAccion r = exactas().usarObjeto(h, habilidad("curacion", 5, 20, Habilidad.Objetivo.ALIADO, null, 0), h);
        assertEquals(ResultadoAccion.Accion.OBJETO, r.accion);
        assertEquals(20, r.cantidad);
        assertEquals(0, h.magia());
    }

    @Test
    public void elVenenoDuelePorCadaTurnoHastaAgotarse() {
        Acciones a = exactas();
        Combatiente e = new Combatiente(Datos.definicion("e", Bando.ENEMIGO, 50, 0, 1, 0, 0, 10));
        ResultadoAccion r = a.usarHabilidad(heroe(5, 0, 0, 10),
                habilidad("alteracion", 3, 4, Habilidad.Objetivo.ENEMIGO, "veneno", 2), e);
        assertEquals("veneno", r.estadoAplicado);
        assertEquals(4, r.cantidad);
        assertEquals(4, a.terminarTurno(e)); // 8 % de 50
        assertEquals(4, a.terminarTurno(e));
        assertFalse(e.tieneEstado("veneno"));
        assertEquals(0, a.terminarTurno(e));
        assertEquals(38, e.vida());
    }

    @Test
    public void elSuenoQuitaElTurnoYSeQuitaConDanio() {
        Acciones a = exactas();
        Combatiente e = enemigo(5, 0, 10);
        a.usarHabilidad(heroe(5, 0, 0, 10), habilidad("alteracion", 5, 0, Habilidad.Objetivo.ENEMIGO, "sueno", 2), e);
        assertFalse(a.puedeActuar(e));
        assertEquals(ResultadoAccion.Accion.PERDER_TURNO, a.perderTurno(e).accion);
        a.atacar(heroe(5, 0, 0, 0), e);
        assertTrue(a.puedeActuar(e));
    }

    @Test
    public void laProteccionReduceElDanioALaMitad() {
        Acciones a = exactas();
        Combatiente h = heroe(5, 0, 0, 10);
        a.usarHabilidad(h, habilidad("alteracion", 4, 0, Habilidad.Objetivo.ALIADO, "proteccion", 3), h);
        assertEquals(6, a.atacar(enemigo(12, 0, 10), h).cantidad);
    }

    @Test
    public void huirConVelocidadesIgualesUsaLaProbabilidadBase() {
        Combatiente h = heroe(5, 0, 0, 0);
        Combatiente e = enemigo(5, 0, 10);
        assertEquals(50, exactas().probabilidadHuida(h, Collections.singletonList(e)));
        assertTrue(acciones(new AzarSecuencia(49)).huir(h, Collections.singletonList(e), true).exito());
        assertEquals(ResultadoAccion.Fallo.HUIDA_FALLIDA,
                acciones(new AzarSecuencia(50)).huir(h, Collections.singletonList(e), true).fallo);
    }

    @Test
    public void laProbabilidadDeHuirQuedaEntreCincoYNoventaYCinco() {
        Acciones a = exactas();
        Combatiente h = heroe(5, 0, 0, 0);
        assertEquals(5, a.probabilidadHuida(h, Arrays.asList(enemigo(1, 0, 200))));
        Combatiente lento = enemigo(1, 0, 1);
        assertEquals(68, a.probabilidadHuida(h, Arrays.asList(lento)));
        lento.recibirDanio(40);
        assertEquals(50, a.probabilidadHuida(h, Arrays.asList(lento)));
    }

    @Test
    public void huirPuedeEstarProhibido() {
        ResultadoAccion r = exactas().huir(heroe(5, 0, 0, 0), Collections.singletonList(enemigo(1, 0, 1)), false);
        assertEquals(ResultadoAccion.Fallo.HUIDA_PROHIBIDA, r.fallo);
        assertNull(r.objetivo);
    }

    @Test
    public void unTipoDeHabilidadNuevoSeRegistraSinTocarElNucleo() {
        Registro<TipoHabilidad> tipos = Datos.tiposHabilidad()
                .registrar("drenar", (acc, poder, h, obj) -> acc.danar(obj, h.poder));
        config.reemplazar(config.actual().con(ConfiguracionCombate.VARIANZA, 0));
        Acciones a = new Acciones(config, new AzarSecuencia(0), tipos, Datos.estados());
        Combatiente e = enemigo(5, 0, 10);
        assertEquals(7, a.usarHabilidad(heroe(5, 0, 0, 0), habilidad("drenar", 0, 7, Habilidad.Objetivo.ENEMIGO, null, 0), e).cantidad);
        assertEquals(33, e.vida());
    }
}
