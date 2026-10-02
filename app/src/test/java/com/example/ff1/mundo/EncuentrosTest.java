package com.example.ff1.mundo;

import com.example.ff1.PaqueteDelJuego;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.example.ff1.combate.Acciones;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.fuentes.AzarSecuencia;

public class EncuentrosTest {

    private static final CatalogoCombate COMBATE = CatalogoCombate.cargar(PaqueteDelJuego.fuente(),
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final TablaEncuentros TABLA = TablaEncuentros.cargar(PaqueteDelJuego.fuente(), COMBATE);
    private static final Mapa CAMPO = Mapa.cargar(PaqueteDelJuego.fuente(), "campo");

    private static ProveedorConfiguracion config(int min, int max) {
        EsquemaConfiguracion e = ConfiguracionMundo.declarar(ConfiguracionCombate.declarar(new EsquemaConfiguracion()));
        return new ProveedorConfiguracion(e.porDefecto()
                .con(ConfiguracionMundo.PASOS_MINIMOS, min).con(ConfiguracionMundo.PASOS_MAXIMOS, max));
    }

    private static TablaEncuentros tabla(String zonas) {
        return TablaEncuentros.desde(LectorJson.leer("{\"tipo\":\"encuentros\",\"version\":1,\"zonas\":"
                + zonas + "}"), COMBATE);
    }

    private static void rechaza(String zonas, String fragmento) {
        try {
            tabla(zonas);
            fail("se esperaba ErrorDeDatos con " + fragmento);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(fragmento));
        }
    }

    @Test
    public void elPaqueteCubreLasZonasDelCampo() {
        TABLA.validarMapa(CAMPO);
        assertEquals("llanura", CAMPO.casilla(1, 1).zona);
        assertNull(CAMPO.casilla(2, 2).zona); // el sendero es seguro
    }

    @Test
    public void eligeGrupoSegunPeso() {
        // llanura: pesos 3, 2, 1 → tiradas 0–2, 3–4 y 5
        assertEquals(Arrays.asList("musgoso"), TABLA.elegir("llanura", new AzarSecuencia(2)));
        assertEquals(Arrays.asList("musgoso", "musgoso"), TABLA.elegir("llanura", new AzarSecuencia(3)));
        assertEquals(Arrays.asList("ala-de-hollin"), TABLA.elegir("llanura", new AzarSecuencia(5)));
    }

    @Test
    public void rechazaDatosInvalidos() {
        rechaza("[{\"zona\":\"a\",\"grupos\":[{\"enemigos\":[\"guardian\"]}]}]", "no es un enemigo");
        rechaza("[{\"zona\":\"a\",\"grupos\":[{\"enemigos\":[]}]}]", "de 1 a 6");
        rechaza("[{\"zona\":\"a\",\"grupos\":[{\"enemigos\":[\"musgoso\"],\"peso\":0}]}]", "peso");
        rechaza("[{\"zona\":\"a\",\"grupos\":[]}]", "no tiene grupos");
        rechaza("[{\"zona\":\"a\",\"grupos\":[{\"enemigos\":[\"musgoso\"]}]},"
                + "{\"zona\":\"a\",\"grupos\":[{\"enemigos\":[\"musgoso\"]}]}]", "repetida");
    }

    @Test
    public void mapaConZonaSinGruposEsRechazado() {
        TablaEncuentros soloLlanura = tabla("[{\"zona\":\"llanura\",\"grupos\":[{\"enemigos\":[\"musgoso\"]}]}]");
        try {
            soloLlanura.validarMapa(CAMPO);
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("maleza"));
        }
    }

    @Test
    public void laCuentaAtrasSaleDeLaConfiguracion() {
        // tirada 2 sobre [15, 30] → 17 pasos
        assertEquals(17, new Encuentros(config(15, 30), TABLA, new AzarSecuencia(2)).restantes());
        // mínimo mayor que el máximo: se usa el mínimo
        assertEquals(4, new Encuentros(config(4, 2), TABLA, new AzarSecuencia(0)).restantes());
    }

    @Test
    public void soloCuentanLasCasillasConZona() {
        Encuentros enc = new Encuentros(config(2, 2), TABLA, new AzarSecuencia(0));
        assertNull(enc.trasPaso(CAMPO.casilla(2, 2))); // sendero
        assertEquals(2, enc.restantes());
        assertNull(enc.trasPaso(CAMPO.casilla(1, 1)));
        List<String> grupo = enc.trasPaso(CAMPO.casilla(1, 1));
        assertEquals(Arrays.asList("musgoso"), grupo);
        assertEquals(2, enc.restantes()); // vuelve a tirar
    }

    @Test
    public void caminarPorElCampoLlevaAUnCombate() {
        Encuentros enc = new Encuentros(config(3, 3), TABLA, new AzarSecuencia(0));
        Explorador ex = new Explorador(CAMPO); // (6, 8) en el sendero
        assertNull(enc.mover(ex, Direccion.ABAJO)); // risco: no se mueve ni cuenta
        assertEquals(3, enc.restantes());
        assertNull(enc.mover(ex, Direccion.IZQUIERDA)); // pradera (5, 8)
        assertNull(enc.mover(ex, Direccion.IZQUIERDA)); // matorral (4, 8)
        List<String> ids = enc.mover(ex, Direccion.IZQUIERDA); // matorral (3, 8)
        assertNotNull(ids);
        assertEquals(Arrays.asList("ala-de-hollin", "musgoso"), ids);

        List<Combatiente> enemigos = Encuentros.crearEnemigos(ids, COMBATE);
        List<Combatiente> heroes = Arrays.asList(new Combatiente(COMBATE.combatiente("guardian")));
        ProveedorConfiguracion cfg = config(3, 3);
        AzarSecuencia azar = new AzarSecuencia(0);
        Combate c = new Combate(cfg, new Acciones(cfg, azar, ReglasCombate.tiposHabilidad(),
                ReglasCombate.estados()), azar, heroes, enemigos, true);
        assertEquals(Combate.Estado.EN_CURSO, c.estado());
        assertEquals(2, c.enemigos().size());
    }
}
