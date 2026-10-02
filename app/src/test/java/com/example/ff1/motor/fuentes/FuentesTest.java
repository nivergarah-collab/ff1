package com.example.ff1.motor.fuentes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;

public class FuentesTest {

    private static FuenteContenido fuenteMemoria() {
        Map<String, String> archivos = new HashMap<>();
        archivos.put("enemigos.json", "{\"tipo\":\"enemigos\",\"version\":1,\"lista\":[]}");
        archivos.put("roto.json", "{\"tipo\":");
        return new FuenteContenidoJson(new LectorMemoria(archivos));
    }

    @Test
    public void cargaDocumentoValidandoTipoYVersion() {
        Nodo doc = fuenteMemoria().cargar("enemigos", "enemigos", 1);
        assertEquals(0, doc.lista("lista").size());
    }

    @Test
    public void recursoInexistenteEsErrorDeDatos() {
        try {
            fuenteMemoria().cargar("heroes", "heroes", 1);
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("heroes.json"));
        }
    }

    @Test
    public void jsonRotoIndicaElRecurso() {
        try {
            fuenteMemoria().cargar("roto", "x", 1);
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("roto.json"));
        }
    }

    @Test
    public void lectorDeArchivosLeeDesdeUnaCarpeta() throws IOException {
        File carpeta = Files.createTempDirectory("ff1-contenido").toFile();
        File f = new File(carpeta, "config.json");
        Files.write(f.toPath(), "{\"tipo\":\"configuracion\",\"version\":1}".getBytes(StandardCharsets.UTF_8));
        FuenteContenido fuente = new FuenteContenidoJson(new LectorArchivos(carpeta));
        assertEquals(1, fuente.cargar("config", "configuracion", 1).entero("version"));
        f.delete();
        carpeta.delete();
    }

    @Test(expected = ErrorDeDatos.class)
    public void lectorDeArchivosNoSaleDeSuCarpeta() throws IOException {
        File carpeta = Files.createTempDirectory("ff1-contenido").toFile();
        try {
            new FuenteContenidoJson(new LectorArchivos(carpeta)).cargar("../secreto", "x", 1);
        } finally {
            carpeta.delete();
        }
    }

    @Test
    public void azarConSemillaEsReproducible() {
        Azar a = new AzarSemilla(42);
        Azar b = new AzarSemilla(42);
        for (int i = 0; i < 20; i++) {
            int x = a.entero(100);
            assertEquals(x, b.entero(100));
            assertTrue(x >= 0 && x < 100);
        }
    }

    @Test
    public void azarSecuenciaDevuelveLosValoresDados() {
        Azar a = new AzarSecuencia(3, 0, 7);
        assertEquals(3, a.entero(10));
        assertEquals(0, a.entero(10));
        assertEquals(7, a.entero(10));
        assertEquals(3, a.entero(10)); // vuelve a empezar
    }

    @Test(expected = IllegalStateException.class)
    public void azarSecuenciaRechazaValorFueraDelLimite() {
        new AzarSecuencia(5).entero(5);
    }

    @Test
    public void tiempoManualAvanzaSoloCuandoSeLePide() {
        TiempoManual t = new TiempoManual();
        assertEquals(0, t.milisegundos());
        t.avanzar(16);
        t.avanzar(17);
        assertEquals(33, t.milisegundos());
    }

    @Test
    public void almacenEnMemoriaGuardaYBorra() {
        Almacen al = new AlmacenMemoria();
        assertFalse(al.existe("ranura1"));
        assertNull(al.cargar("ranura1"));
        al.guardar("ranura1", "{\"a\":1}");
        assertTrue(al.existe("ranura1"));
        assertEquals("{\"a\":1}", al.cargar("ranura1"));
        al.borrar("ranura1");
        assertFalse(al.existe("ranura1"));
    }

    @Test
    public void semillasDistintasDanSecuenciasDistintas() {
        Azar a = new AzarSemilla(1);
        Azar b = new AzarSemilla(2);
        StringBuilder sa = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sa.append(a.entero(1000)).append(',');
            sb.append(b.entero(1000)).append(',');
        }
        assertNotEquals(sa.toString(), sb.toString());
    }
}
