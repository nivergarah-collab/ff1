package com.example.ff1.inventario;

import com.example.ff1.PaqueteDelJuego;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.fuentes.AzarSecuencia;

public class BotinTest {

    private static final CatalogoCombate COMBATE = CatalogoCombate.cargar(PaqueteDelJuego.fuente(),
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final CatalogoObjetos OBJETOS = CatalogoObjetos.cargar(PaqueteDelJuego.fuente(), COMBATE,
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final TablaBotin BOTIN = TablaBotin.cargar(PaqueteDelJuego.fuente(), COMBATE, OBJETOS);

    private static Combatiente caido(String id) {
        Combatiente c = new Combatiente(COMBATE.combatiente(id));
        c.recibirDanio(99999);
        return c;
    }

    @Test
    public void soloSueltanBotinLosCaidosYSegunElAzar() {
        Combatiente vivo = new Combatiente(COMBATE.combatiente("musgoso"));
        List<Combatiente> enemigos = Arrays.asList(vivo, caido("lagarto-de-cantera"), caido("musgoso"));
        // lagarto: tónico 39 < 40 sale ×2; jubón 5 no < 5; musgoso: tónico 24 < 25 sale
        Map<String, Integer> b = BOTIN.tirar(enemigos, new AzarSecuencia(39, 5, 24));
        assertEquals(Integer.valueOf(3), b.get("tonico-de-raiz"));
        assertEquals(1, b.size());
    }

    @Test
    public void sinSuerteNoHayBotin() {
        assertTrue(BOTIN.tirar(Arrays.asList(caido("musgoso")), new AzarSecuencia(99)).isEmpty());
    }

    @Test
    public void recogerDevuelveLoQueNoCabe() {
        Inventario inv = new Inventario(2);
        inv.agregar("tonico-de-raiz", 1);
        Map<String, Integer> sobra = TablaBotin.recoger(BOTIN.tirar(
                Arrays.asList(caido("lagarto-de-cantera")), new AzarSecuencia(0, 99)), inv);
        assertEquals(2, inv.cantidad("tonico-de-raiz"));
        assertEquals(Integer.valueOf(1), sobra.get("tonico-de-raiz"));
    }

    @Test
    public void datosInvalidosSeRechazan() {
        for (String lista : Arrays.asList(
                "[{\"enemigo\":\"guardian\",\"objetos\":[]}]",
                "[{\"enemigo\":\"musgoso\",\"objetos\":[{\"id\":\"nada\",\"probabilidad\":5}]}]",
                "[{\"enemigo\":\"musgoso\",\"objetos\":[{\"id\":\"tonico-de-raiz\",\"probabilidad\":0}]}]")) {
            try {
                TablaBotin.desde(LectorJson.leer("{\"tipo\":\"botin\",\"version\":1,\"lista\":" + lista + "}"),
                        COMBATE, OBJETOS);
                fail("debió rechazar " + lista);
            } catch (ErrorDeDatos esperado) {
                // bien
            }
        }
    }
}
