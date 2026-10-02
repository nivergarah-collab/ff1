package com.example.ff1.progresion;

import com.example.ff1.PaqueteDelJuego;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.combate.Recompensa;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;

public class ProgresionTest {

    private static final CatalogoCombate CATALOGO = CatalogoCombate.cargar(PaqueteDelJuego.fuente(),
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final TablaProgresion TABLA = TablaProgresion.cargar(PaqueteDelJuego.fuente(), CATALOGO);

    private static TablaProgresion tabla(String json) {
        return TablaProgresion.desde(LectorJson.leer(json), CATALOGO);
    }

    private static Heroe guardian() {
        return new Heroe(CATALOGO.combatiente("guardian"), "Ilse", TABLA);
    }

    @Test
    public void elPaqueteDelJuegoCargaConQuinceNiveles() {
        assertEquals(15, TABLA.nivelMaximo());
        assertEquals(10, TABLA.experienciaPara(2));
    }

    @Test
    public void elNivelSeCalculaPorExperienciaAcumulada() {
        assertEquals(1, TABLA.nivelCon(0));
        assertEquals(1, TABLA.nivelCon(9));
        assertEquals(2, TABLA.nivelCon(10));
        assertEquals(4, TABLA.nivelCon(70));
        assertEquals(15, TABLA.nivelCon(999999));
    }

    @Test
    public void lasEstadisticasCrecenConElNivel() {
        DefinicionCombatiente base = CATALOGO.combatiente("guardian");
        DefinicionCombatiente n3 = TABLA.enNivel(base, 3);
        assertEquals(base.vida + 14, n3.vida);
        assertEquals(base.ataque + 4, n3.ataque);
        assertEquals(base.velocidad, n3.velocidad);
        assertEquals(base.vida, TABLA.enNivel(base, 1).vida);
    }

    @Test
    public void ganarExperienciaSubeVariosNivelesYSumaVida() {
        Heroe h = guardian();
        int vidaBase = h.vida();
        assertEquals(0, h.ganarExperiencia(9));
        assertEquals(1, h.experienciaFaltante());
        assertEquals(2, h.ganarExperiencia(16)); // 25 en total: nivel 3
        assertEquals(3, h.nivel());
        assertEquals(vidaBase + 14, h.vida());
    }

    @Test
    public void elHeridoSubeDeNivelSinCurarseDelTodo() {
        Heroe h = guardian();
        Combatiente c = h.entrarEnCombate();
        c.recibirDanio(20);
        h.salirDeCombate(c);
        h.ganarExperiencia(10);
        assertEquals(h.estadisticas().vida - 20, h.vida());
        h.restaurar();
        assertEquals(h.estadisticas().vida, h.vida());
    }

    @Test
    public void elCombatienteEntraConLaVidaActual() {
        Heroe h = guardian();
        Combatiente c = h.entrarEnCombate();
        c.recibirDanio(5);
        h.salirDeCombate(c);
        Combatiente otro = h.entrarEnCombate();
        assertEquals(h.vida(), otro.vida());
        assertEquals("Ilse", otro.nombre());
    }

    @Test
    public void elRepartoSoloPremiaALosHeroesEnPie() {
        Heroe vivo = guardian();
        Heroe caido = new Heroe(CATALOGO.combatiente("arcanista"), "Oto", TABLA);
        Combatiente c = caido.entrarEnCombate();
        c.recibirDanio(9999);
        caido.salirDeCombate(c);
        Map<Heroe, Integer> subidas = Reparto.experiencia(Arrays.asList(vivo, caido), new Recompensa(12, 5));
        assertEquals(Integer.valueOf(1), subidas.get(vivo));
        assertFalse(subidas.containsKey(caido));
        assertEquals(0, caido.experiencia());
    }

    @Test
    public void enElNivelMaximoNoFaltaExperiencia() {
        Heroe h = guardian();
        h.ganarExperiencia(Integer.MAX_VALUE);
        h.ganarExperiencia(5);
        assertEquals(15, h.nivel());
        assertEquals(0, h.experienciaFaltante());
    }

    @Test
    public void laTablaDebeEmpezarEnCeroYCrecer() {
        for (String xp : Arrays.asList("[5, 10]", "[0, 10, 10]", "[]")) {
            try {
                tabla("{\"tipo\":\"progresion\",\"version\":1,\"experiencia\":" + xp + ",\"clases\":[]}");
                fail("debió rechazar " + xp);
            } catch (ErrorDeDatos esperado) {
                // bien
            }
        }
    }

    @Test
    public void unEnemigoNoPuedeTenerCrecimiento() {
        try {
            tabla("{\"tipo\":\"progresion\",\"version\":1,\"experiencia\":[0],"
                    + "\"clases\":[{\"id\":\"musgoso\",\"crecimiento\":{}}]}");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("no es una clase de héroe"));
        }
    }
}
