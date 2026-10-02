package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;
import com.example.ff1.motor.fuentes.LectorMemoria;

public class CatalogoCombateTest {

    private static final String HABILIDADES = "{\"tipo\":\"habilidades\",\"version\":1,\"lista\":["
            + "{\"id\":\"rayo\",\"nombre\":\"Rayo\",\"tipo\":\"danio\",\"coste\":3,\"poder\":9},"
            + "{\"id\":\"siesta\",\"nombre\":\"Siesta\",\"tipo\":\"alteracion\",\"objetivo\":\"enemigo\","
            + "\"estado\":{\"id\":\"sueno\",\"duracion\":2}}]}";

    private static final String COMBATIENTES = "{\"tipo\":\"combatientes\",\"version\":1,\"lista\":["
            + "{\"id\":\"maga\",\"nombre\":\"Maga\",\"bando\":\"heroe\",\"vida\":20,\"magia\":12,"
            + "\"ataque\":3,\"defensa\":2,\"poder\":9,\"velocidad\":11,\"habilidades\":[\"rayo\",\"siesta\"]},"
            + "{\"id\":\"sapo\",\"nombre\":\"Sapo\",\"bando\":\"enemigo\",\"vida\":8,"
            + "\"ataque\":4,\"defensa\":1,\"velocidad\":5,\"experiencia\":2,\"oro\":1}]}";

    private static CatalogoCombate desde(String combatientes, String habilidades) {
        return CatalogoCombate.desde(LectorJson.leer(combatientes), LectorJson.leer(habilidades),
                Datos.tiposHabilidad(), Datos.estados());
    }

    private static void esperarError(String combatientes, String habilidades, String fragmento) {
        try {
            desde(combatientes, habilidades);
            fail("se esperaba ErrorDeDatos con: " + fragmento);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(fragmento));
        }
    }

    @Test
    public void leeCombatientesYHabilidadesConValoresPorDefecto() {
        CatalogoCombate cat = desde(COMBATIENTES, HABILIDADES);
        DefinicionCombatiente maga = cat.combatiente("maga");
        assertEquals(Bando.HEROE, maga.bando);
        assertEquals(11, maga.velocidad);
        assertEquals(2, maga.habilidades.size());
        DefinicionCombatiente sapo = cat.combatiente("sapo");
        assertEquals(0, sapo.magia);
        assertEquals(2, sapo.experiencia);
        Habilidad rayo = cat.habilidad("rayo");
        assertEquals(Habilidad.Objetivo.ENEMIGO, rayo.objetivo);
        assertNull(rayo.estado);
        Habilidad siesta = cat.habilidad("siesta");
        assertEquals("sueno", siesta.estado);
        assertEquals(2, siesta.duracionEstado);
        assertEquals(0, siesta.coste);
    }

    @Test
    public void cargaDesdeUnaFuenteDeContenido() {
        Map<String, String> archivos = new HashMap<>();
        archivos.put("combatientes.json", COMBATIENTES);
        archivos.put("habilidades.json", HABILIDADES);
        CatalogoCombate cat = CatalogoCombate.cargar(new FuenteContenidoJson(new LectorMemoria(archivos)),
                Datos.tiposHabilidad(), Datos.estados());
        assertEquals(2, cat.combatientes().size());
    }

    @Test
    public void elPaqueteDelJuegoEsValido() {
        File carpeta = new File("app/src/main/assets/contenido");
        if (!carpeta.isDirectory()) {
            carpeta = new File("src/main/assets/contenido"); // Gradle corre desde app/
        }
        CatalogoCombate cat = CatalogoCombate.cargar(new FuenteContenidoJson(new LectorArchivos(carpeta)),
                Datos.tiposHabilidad(), Datos.estados());
        int heroes = 0;
        for (DefinicionCombatiente d : cat.combatientes().values()) {
            if (d.bando == Bando.HEROE) {
                heroes++;
            }
        }
        assertEquals(4, heroes);
        assertTrue(cat.combatientes().size() > heroes);
    }

    @Test
    public void idRepetidoSeRechaza() {
        esperarError(COMBATIENTES.replace("\"sapo\"", "\"maga\""), HABILIDADES, "lista[1].id: \"maga\" repetido");
    }

    @Test
    public void idConMayusculasSeRechaza() {
        esperarError(COMBATIENTES.replace("\"sapo\"", "\"Sapo\""), HABILIDADES, "minúsculas");
    }

    @Test
    public void habilidadInexistenteSeRechazaConSuRuta() {
        esperarError(COMBATIENTES.replace("\"siesta\"]", "\"vuelo\"]"), HABILIDADES,
                "lista[0].habilidades[1]: habilidad \"vuelo\" no existe");
    }

    @Test
    public void tipoDeHabilidadNoRegistradoSeRechaza() {
        esperarError(COMBATIENTES, HABILIDADES.replace("\"danio\"", "\"invocar\""), "\"invocar\" no registrado");
    }

    @Test
    public void estadoNoRegistradoSeRechaza() {
        esperarError(COMBATIENTES, HABILIDADES.replace("\"sueno\"", "\"piedra\""), "\"piedra\" no registrado");
    }

    @Test
    public void valorFueraDeRangoIndicaElCampo() {
        esperarError(COMBATIENTES.replace("\"velocidad\":5", "\"velocidad\":0"), HABILIDADES,
                "lista[1].velocidad: 0 fuera del rango");
    }

    @Test
    public void campoObligatorioAusenteSeRechaza() {
        esperarError(COMBATIENTES.replace("\"vida\":8,", ""), HABILIDADES, "lista[1].vida: falta el campo");
    }

    @Test
    public void bandoYObjetivoDesconocidosSeRechazan() {
        esperarError(COMBATIENTES.replace("\"enemigo\"", "\"neutral\""), HABILIDADES, "bando \"neutral\"");
        esperarError(COMBATIENTES, HABILIDADES.replace("\"objetivo\":\"enemigo\"", "\"objetivo\":\"todos\""),
                "objetivo: \"todos\"");
    }

    @Test
    public void versionNoSoportadaSeRechaza() {
        esperarError(COMBATIENTES.replace("\"version\":1", "\"version\":2"), HABILIDADES, "no soportada");
    }

    @Test
    public void idPedidoQueNoExisteEsErrorDeDatos() {
        try {
            desde(COMBATIENTES, HABILIDADES).combatiente("dragon");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage().contains("dragon"));
        }
    }

    @Test
    public void listaVaciaEsValida() {
        Nodo vacio = LectorJson.leer("{\"tipo\":\"habilidades\",\"version\":1,\"lista\":[]}");
        Nodo sinNadie = LectorJson.leer("{\"tipo\":\"combatientes\",\"version\":1,\"lista\":[]}");
        assertTrue(CatalogoCombate.desde(sinNadie, vacio, Datos.tiposHabilidad(), Datos.estados())
                .combatientes().isEmpty());
    }
}
