package com.example.ff1.motor.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;

public class ConfiguracionTest {

    private static EsquemaConfiguracion esquema() {
        return new EsquemaConfiguracion()
                .entero("combate.velocidadBarra", 1, 100, 10)
                .entero("combate.avanceRapido", 1, 8, 2)
                .decimal("combate.factorDanio", 0.1, 10.0, 1.0);
    }

    @Test
    public void usaLosValoresPorDefectoSiFaltan() {
        Configuracion c = esquema().crear(LectorJson.leer("{\"tipo\":\"configuracion\",\"version\":1}"));
        assertEquals(10, c.entero("combate.velocidadBarra"));
        assertEquals(1.0, c.decimal("combate.factorDanio"), 0.0);
    }

    @Test
    public void leeValoresDelDocumento() {
        Configuracion c = esquema().crear(LectorJson.leer(
                "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"combate.avanceRapido\":4}}"));
        assertEquals(4, c.entero("combate.avanceRapido"));
    }

    @Test
    public void rechazaValorFueraDeRango() {
        try {
            esquema().crear(LectorJson.leer(
                    "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"combate.avanceRapido\":9}}"));
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("combate.avanceRapido"));
        }
    }

    @Test(expected = ErrorDeDatos.class)
    public void rechazaParametroDesconocido() {
        esquema().crear(LectorJson.leer(
                "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"combate.velocidad\":5}}"));
    }

    @Test(expected = ErrorDeDatos.class)
    public void rechazaDecimalEnParametroEntero() {
        esquema().crear(LectorJson.leer(
                "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"combate.velocidadBarra\":1.5}}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void pedirParametroNoDeclaradoEsErrorDeProgramacion() {
        esquema().porDefecto().entero("no.existe");
    }

    @Test
    public void conCambiaUnValorValidandolo() {
        Configuracion c = esquema().porDefecto().con("combate.avanceRapido", 3);
        assertEquals(3, c.entero("combate.avanceRapido"));
        try {
            c.con("combate.avanceRapido", 0);
            fail();
        } catch (ErrorDeDatos e) {
            assertEquals(3, c.entero("combate.avanceRapido"));
        }
    }

    @Test
    public void proveedorReemplazaEnCalienteYAvisa() {
        ProveedorConfiguracion p = new ProveedorConfiguracion(esquema().porDefecto());
        List<Integer> avisos = new ArrayList<>();
        p.alCambiar(c -> avisos.add(c.entero("combate.avanceRapido")));
        p.reemplazar(p.actual().con("combate.avanceRapido", 5));
        assertEquals(5, p.actual().entero("combate.avanceRapido"));
        assertEquals(1, avisos.size());
        assertEquals(5, (int) avisos.get(0));
    }

    @Test
    public void proveedorConservaLaConfiguracionSiLaNuevaEsInvalida() {
        EsquemaConfiguracion e = esquema();
        ProveedorConfiguracion p = new ProveedorConfiguracion(e.porDefecto());
        Configuracion antes = p.actual();
        try {
            p.reemplazarDesde(LectorJson.leer("{\"tipo\":\"configuracion\",\"version\":1,"
                    + "\"valores\":{\"combate.factorDanio\":50}}"));
            fail();
        } catch (ErrorDeDatos ex) {
            assertSame(antes, p.actual());
        }
    }
}
