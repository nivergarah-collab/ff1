package com.example.ff1.herramientas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.ff1.herramientas.ValidadorContenido.Estado;
import com.example.ff1.herramientas.ValidadorContenido.Informe;
import com.example.ff1.herramientas.ValidadorContenido.Resultado;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Test;

/** El validador de contenido usa los cargadores reales del motor: paquetes buenos y malos. */
public class ValidadorContenidoTest {

    private Path temporal;

    @After
    public void limpiar() throws IOException {
        if (temporal != null) {
            try (Stream<Path> s = Files.walk(temporal)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    private static File carpeta(String ruta) {
        File f = new File("app/" + ruta);
        return f.isDirectory() ? f : new File(ruta); // Gradle corre desde app/
    }

    private static File juego() {
        return carpeta("src/main/assets/contenido");
    }

    private static File minimo() {
        return carpeta("src/test/resources/contenido-minimo");
    }

    /** Copia un paquete a una carpeta temporal para estropearlo. */
    private File copiar(File origen) throws IOException {
        temporal = Files.createTempDirectory("ff1-validador");
        Path base = origen.toPath();
        try (Stream<Path> s = Files.walk(base)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                Path destino = temporal.resolve(base.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(destino);
                } else {
                    Files.copy(p, destino);
                }
            }
        }
        return temporal.toFile();
    }

    private static String leer(File carpeta, String ruta) throws IOException {
        return new String(Files.readAllBytes(new File(carpeta, ruta).toPath()), StandardCharsets.UTF_8);
    }

    private static void escribir(File carpeta, String ruta, String texto) throws IOException {
        Files.write(new File(carpeta, ruta).toPath(), texto.getBytes(StandardCharsets.UTF_8));
    }

    private static Resultado de(Informe informe, String documento) {
        for (Resultado r : informe.resultados()) {
            if (r.documento.equals(documento)) {
                return r;
            }
        }
        throw new AssertionError("el informe no menciona " + documento + ": " + informe.texto());
    }

    @Test
    public void elPaqueteDelJuegoEsValido() {
        Informe informe = ValidadorContenido.validar(juego());
        assertTrue(informe.texto(), informe.ok());
        assertEquals(0, informe.errores());
        assertEquals(Estado.OK, de(informe, "configuracion.json").estado);
        assertEquals(Estado.OK, de(informe, "combatientes.json").estado);
        assertEquals(Estado.OK, de(informe, "mapas/campo.json").estado);
        assertEquals(Estado.OK, de(informe, "escenas/apertura.json").estado);
        assertEquals(Estado.OK, de(informe, "inicio.json").estado);
        assertEquals(informe.resultados().size(), informe.texto().split("\n").length - 1); // una línea por documento y un resumen
    }

    @Test
    public void elSegundoPaqueteTambienEsValido() {
        Informe informe = ValidadorContenido.validar(minimo());
        assertTrue(informe.texto(), informe.ok());
        assertEquals(Estado.OK, de(informe, "mapas/playa.json").estado);
    }

    @Test
    public void unaReferenciaRotaSeSenalaEnSuDocumentoYOmiteLosQueDependen() throws IOException {
        File c = copiar(juego());
        String combatientes = leer(c, "combatientes.json");
        // El jefe usa una habilidad inexistente.
        assertTrue(combatientes.contains("\"sacudida-de-piedra\""));
        escribir(c, "combatientes.json", combatientes.replace("\"sacudida-de-piedra\"", "\"no-existe\""));
        Informe informe = ValidadorContenido.validar(c);
        assertFalse(informe.ok());
        Resultado r = de(informe, "combatientes.json");
        assertEquals(Estado.ERROR, r.estado);
        assertTrue(r.detalle, r.detalle.contains("no-existe"));
        assertEquals(Estado.OK, de(informe, "configuracion.json").estado);
        assertEquals(Estado.OK, de(informe, "habilidades.json").estado);
        assertEquals(Estado.OMITIDO, de(informe, "objetos.json").estado);
        assertEquals(Estado.OMITIDO, de(informe, "inicio.json").estado);
    }

    @Test
    public void unJsonMalFormadoIndicaLineaYColumna() throws IOException {
        File c = copiar(juego());
        escribir(c, "mapas/campo.json", "{\n  \"tipo\": \"mapa\",\n  \"version\": 1,\n");
        Informe informe = ValidadorContenido.validar(c);
        Resultado r = de(informe, "mapas/campo.json");
        assertEquals(Estado.ERROR, r.estado);
        assertTrue(r.detalle, r.detalle.contains("línea"));
        assertEquals(Estado.OK, de(informe, "mapas/pozaluz.json").estado); // los demás mapas se revisan igual
    }

    @Test
    public void unValorFueraDeRangoDaLaRutaDelCampo() throws IOException {
        File c = copiar(juego());
        escribir(c, "configuracion.json",
                "{\"tipo\":\"configuracion\",\"version\":1,\"valores\":{\"combate.ticksPorPaso\":-5}}");
        Informe informe = ValidadorContenido.validar(c);
        Resultado r = de(informe, "configuracion.json");
        assertEquals(Estado.ERROR, r.estado);
        assertTrue(r.detalle, r.detalle.contains("ticksPorPaso") || r.detalle.contains("combate"));
    }

    @Test
    public void faltaUnDocumentoObligatorio() throws IOException {
        File c = copiar(juego());
        assertTrue(new File(c, "botin.json").delete());
        Informe informe = ValidadorContenido.validar(c);
        Resultado r = de(informe, "botin.json");
        assertEquals(Estado.ERROR, r.estado);
        assertTrue(r.detalle, r.detalle.contains("no existe"));
    }

    @Test
    public void salidaDeTextoYCodigosDeSalida() throws IOException {
        ByteArrayOutputStream sal = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int codigo = ValidadorContenido.ejecutar(new String[] {juego().getPath()}, new PrintStream(sal, true, "UTF-8"),
                new PrintStream(err, true, "UTF-8"));
        assertEquals(0, codigo);
        assertTrue(sal.toString("UTF-8").contains("OK"));

        File c = copiar(juego());
        escribir(c, "objetos.json", "no es json");
        sal.reset();
        codigo = ValidadorContenido.ejecutar(new String[] {c.getPath()}, new PrintStream(sal, true, "UTF-8"),
                new PrintStream(err, true, "UTF-8"));
        assertEquals(1, codigo);
        assertTrue(sal.toString("UTF-8").contains("ERROR"));
    }

    @Test
    public void usoIncorrecto() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream e = new PrintStream(err, true, "UTF-8");
        PrintStream sal = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        assertEquals(2, ValidadorContenido.ejecutar(new String[0], sal, e));
        assertEquals(2, ValidadorContenido.ejecutar(new String[] {"/no/existe/ff1"}, sal, e));
        assertEquals(2, ValidadorContenido.ejecutar(new String[] {juego().getPath(), "--raro"}, sal, e));
        assertTrue(err.toString("UTF-8").contains("Uso"));
    }

    @Test
    public void salidaJsonLegibleParaElEditor() throws IOException {
        File c = copiar(juego());
        escribir(c, "encuentros.json", "{\"tipo\":\"encuentros\",\"version\":1}");
        ByteArrayOutputStream sal = new ByteArrayOutputStream();
        int codigo = ValidadorContenido.ejecutar(new String[] {"--json", c.getPath()}, new PrintStream(sal, true, "UTF-8"),
                new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"));
        assertEquals(1, codigo);
        Nodo doc = LectorJson.leer(sal.toString("UTF-8"));
        assertFalse(doc.booleano("ok"));
        assertTrue(doc.entero("errores") >= 1);
        boolean visto = false;
        for (Nodo d : doc.lista("documentos")) {
            if (d.texto("documento").equals("encuentros.json")) {
                visto = true;
                assertEquals("error", d.texto("estado"));
                assertNotNull(d.texto("detalle"));
            }
        }
        assertTrue(visto);
    }

    @Test
    public void salidaJsonDeUnPaqueteBueno() throws IOException {
        ByteArrayOutputStream sal = new ByteArrayOutputStream();
        int codigo = ValidadorContenido.ejecutar(new String[] {juego().getPath(), "--json"}, new PrintStream(sal, true, "UTF-8"),
                new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"));
        assertEquals(0, codigo);
        Nodo doc = LectorJson.leer(sal.toString("UTF-8"));
        assertTrue(doc.booleano("ok"));
        assertEquals(0, doc.entero("errores"));
    }
}
