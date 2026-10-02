package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.AzarSecuencia;
import com.example.ff1.motor.fuentes.AzarSemilla;

public class CombateTest {

    private final ProveedorConfiguracion config = Datos.config();

    private Combate combate(Azar azar, List<Combatiente> heroes, List<Combatiente> enemigos, boolean huida) {
        Acciones a = new Acciones(config, azar, Datos.tiposHabilidad(), Datos.estados());
        return new Combate(config, a, azar, heroes, enemigos, huida);
    }

    private static Combatiente heroe(int vida, int ataque, int velocidad) {
        return new Combatiente(Datos.definicion("heroe", Bando.HEROE, vida, 10, ataque, 0, 0, velocidad));
    }

    private static Combatiente enemigo(int vida, int ataque, int velocidad, int xp, int oro) {
        return new Combatiente(new DefinicionCombatiente("e", "e", Bando.ENEMIGO, vida, 0, ataque, 0, 0,
                velocidad, Collections.<String>emptyList(), xp, oro));
    }

    /** Juega todos los turnos en automático; devuelve los turnos jugados. */
    private static int jugar(Combate c, int maxTurnos) {
        int turnos = 0;
        while (!c.terminado() && turnos < maxTurnos) {
            assertNotNull(c.esperarTurno(100000));
            c.turnoAutomatico();
            turnos++;
        }
        return turnos;
    }

    @Test
    public void elHeroeRapidoActuaPrimeroYVence() {
        Combatiente h = heroe(30, 50, 20);
        Combatiente e = enemigo(10, 5, 5, 4, 3);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(e), true);
        assertSame(h, c.esperarTurno(1000));
        assertTrue(c.atacar(e).exito());
        assertEquals(Combate.Estado.VICTORIA, c.estado());
        assertEquals(30, h.vida());
    }

    @Test
    public void laRecompensaSumaLosEnemigosCaidos() {
        Combatiente h = heroe(30, 50, 20);
        Combatiente a = enemigo(10, 5, 5, 4, 3);
        Combatiente b = enemigo(10, 5, 5, 9, 8);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(a, b), true);
        jugar(c, 50);
        assertEquals(Combate.Estado.VICTORIA, c.estado());
        Recompensa r = c.recompensa();
        assertEquals(13, r.experiencia);
        assertEquals(11, r.oro);
    }

    @Test(expected = IllegalStateException.class)
    public void noHayRecompensaAntesDeVencer() {
        combate(new AzarSecuencia(0), Arrays.asList(heroe(30, 5, 10)),
                Arrays.asList(enemigo(10, 5, 5, 4, 3)), true).recompensa();
    }

    @Test
    public void sinHeroesEnPieEsDerrota() {
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(heroe(5, 1, 1)),
                Arrays.asList(enemigo(100, 50, 30, 4, 3)), true);
        jugar(c, 50);
        assertEquals(Combate.Estado.DERROTA, c.estado());
        assertTrue(c.avanzar(10).isEmpty());
    }

    @Test
    public void huirConExitoTerminaElCombate() {
        Combatiente h = heroe(30, 5, 20);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(enemigo(10, 5, 5, 4, 3)), true);
        c.esperarTurno(1000);
        assertTrue(c.huir().exito());
        assertEquals(Combate.Estado.HUIDA, c.estado());
    }

    @Test
    public void unaHuidaFallidaGastaElTurno() {
        Combatiente h = heroe(30, 5, 20);
        Combate c = combate(new AzarSecuencia(99), Arrays.asList(h), Arrays.asList(enemigo(10, 5, 5, 4, 3)), true);
        c.esperarTurno(1000);
        assertEquals(ResultadoAccion.Fallo.HUIDA_FALLIDA, c.huir().fallo);
        assertEquals(Combate.Estado.EN_CURSO, c.estado());
        assertEquals(0, c.barra().carga(h));
    }

    @Test
    public void anteUnJefeNoSeHuyeYSeConservaElTurno() {
        Combatiente h = heroe(30, 5, 20);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(enemigo(10, 5, 5, 4, 3)), false);
        c.esperarTurno(1000);
        assertEquals(ResultadoAccion.Fallo.HUIDA_PROHIBIDA, c.huir().fallo);
        assertSame(h, c.turno());
    }

    @Test
    public void sinMagiaSeConservaElTurno() {
        Combatiente h = heroe(30, 5, 20);
        Combatiente e = enemigo(10, 5, 5, 4, 3);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(e), true);
        c.esperarTurno(1000);
        Habilidad cara = new Habilidad("cara", "cara", "danio", 99, 10, Habilidad.Objetivo.ENEMIGO, null, 0);
        assertEquals(ResultadoAccion.Fallo.SIN_MAGIA, c.usarHabilidad(cara, e).fallo);
        assertSame(h, c.turno());
    }

    @Test
    public void unDormidoSoloPuedePerderElTurno() {
        Combatiente h = heroe(30, 5, 20);
        Combatiente e = enemigo(10, 5, 5, 4, 3);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(e), true);
        h.aplicarEstado("sueno", 1);
        c.esperarTurno(1000);
        assertFalse(c.puedeActuar());
        try {
            c.atacar(e);
            throw new AssertionError("debió rechazar la acción");
        } catch (IllegalStateException esperado) {
            // bien
        }
        assertEquals(ResultadoAccion.Accion.PERDER_TURNO, c.turnoAutomatico().accion);
        assertFalse(h.tieneEstado("sueno"));
    }

    @Test
    public void elVenenoPuedeCerrarElCombateAlTerminarElTurno() {
        Combatiente h = heroe(30, 5, 1);
        Combatiente e = enemigo(1, 5, 50, 4, 3);
        Combate c = combate(new AzarSecuencia(0), Arrays.asList(h), Arrays.asList(e), true);
        e.aplicarEstado("veneno", 3);
        assertSame(e, c.esperarTurno(1000));
        c.turnoAutomatico();
        assertEquals(Combate.Estado.VICTORIA, c.estado());
    }

    @Test
    public void unCombateConElContenidoDelJuegoTerminaConVictoria() {
        CatalogoCombate cat = Datos.catalogoDelJuego();
        List<Combatiente> heroes = new ArrayList<>();
        for (String id : Arrays.asList("guardian", "arcanista", "rastreador", "herbolaria")) {
            heroes.add(new Combatiente(cat.combatiente(id)));
        }
        List<Combatiente> enemigos = Arrays.asList(new Combatiente(cat.combatiente("musgoso")),
                new Combatiente(cat.combatiente("ala-de-hollin")),
                new Combatiente(cat.combatiente("lagarto-de-cantera")));
        Combate c = combate(new AzarSemilla(7), heroes, enemigos, true);
        int turnos = jugar(c, 500);
        assertEquals(Combate.Estado.VICTORIA, c.estado());
        assertTrue("turnos: " + turnos, turnos > 3 && turnos < 500);
        assertEquals(18, c.recompensa().experiencia);
        assertEquals(15, c.recompensa().oro);
    }
}
