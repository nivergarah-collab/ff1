package com.example.ff1.herramientas;

import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.EfectoEstado;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.combate.TipoHabilidad;
import com.example.ff1.guion.Guion;
import com.example.ff1.inventario.CatalogoObjetos;
import com.example.ff1.inventario.TablaBotin;
import com.example.ff1.juego.Partida;
import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;
import com.example.ff1.mundo.Mapa;
import com.example.ff1.mundo.TablaEncuentros;
import com.example.ff1.progresion.TablaProgresion;
import com.example.ff1.pueblo.Servicios;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Valida una carpeta de contenido con los cargadores reales del motor (Java puro, sin Android).
 * Da un resultado por documento, con la ruta del campo en los errores. Lo usa
 * {@code scripts/validar-contenido.sh} y, con {@code --json}, el editor de parámetros.
 *
 * <p>Los documentos que dependen de otro con error se marcan {@code OMITIDO}: no se pueden
 * comprobar hasta que el primero se corrija.
 */
public final class ValidadorContenido {

    public enum Estado { OK, ERROR, OMITIDO }

    /** Resultado de un documento. {@code detalle} es null si está bien. */
    public static final class Resultado {
        public final String documento;
        public final Estado estado;
        public final String detalle;

        Resultado(String documento, Estado estado, String detalle) {
            this.documento = documento;
            this.estado = estado;
            this.detalle = detalle;
        }
    }

    public static final class Informe {
        private final String carpeta;
        private final List<Resultado> resultados = new ArrayList<>();

        Informe(String carpeta) {
            this.carpeta = carpeta;
        }

        public List<Resultado> resultados() {
            return Collections.unmodifiableList(resultados);
        }

        public int errores() {
            return contar(Estado.ERROR);
        }

        public int omitidos() {
            return contar(Estado.OMITIDO);
        }

        /** Válido solo si no hay errores ni documentos sin comprobar. */
        public boolean ok() {
            return errores() == 0 && omitidos() == 0;
        }

        private int contar(Estado e) {
            int n = 0;
            for (Resultado r : resultados) {
                if (r.estado == e) {
                    n++;
                }
            }
            return n;
        }

        /** Una línea por documento; el detalle (con la ruta del campo) solo de los errores; resumen al final. */
        public String texto() {
            StringBuilder sb = new StringBuilder();
            for (Resultado r : resultados) {
                sb.append(String.format("%-9s %s", r.estado, r.documento));
                if (r.estado == Estado.OMITIDO) {
                    sb.append(" (").append(r.detalle).append(')');
                }
                sb.append('\n');
                if (r.estado == Estado.ERROR) {
                    sb.append("          ").append(r.detalle.replace("\n", "\n          ")).append('\n');
                }
            }
            sb.append(ok() ? "Contenido válido: " : "Contenido con problemas: ")
                    .append(resultados.size()).append(" documentos, ")
                    .append(errores()).append(" con errores, ")
                    .append(omitidos()).append(" sin comprobar");
            return sb.append('\n').toString();
        }

        public String json() {
            StringBuilder sb = new StringBuilder("{\"carpeta\":");
            cadena(carpeta, sb);
            sb.append(",\"ok\":").append(ok()).append(",\"errores\":").append(errores())
                    .append(",\"omitidos\":").append(omitidos()).append(",\"documentos\":[");
            for (int i = 0; i < resultados.size(); i++) {
                Resultado r = resultados.get(i);
                if (i > 0) {
                    sb.append(',');
                }
                sb.append("{\"documento\":");
                cadena(r.documento, sb);
                sb.append(",\"estado\":\"").append(r.estado.name().toLowerCase()).append("\",\"detalle\":");
                if (r.detalle == null) {
                    sb.append("null");
                } else {
                    cadena(r.detalle, sb);
                }
                sb.append('}');
            }
            return sb.append("]}\n").toString();
        }

        private static void cadena(String s, StringBuilder sb) {
            sb.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' || c == '\\') {
                    sb.append('\\').append(c);
                } else if (c == '\n') {
                    sb.append("\\n");
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\t') {
                    sb.append("\\t");
                } else if (c < 0x20) {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            sb.append('"');
        }
    }

    private final FuenteContenidoJson fuente;
    private final File carpeta;
    private final Informe informe;

    private ValidadorContenido(File carpeta) {
        this.carpeta = carpeta;
        this.fuente = new FuenteContenidoJson(new LectorArchivos(carpeta));
        this.informe = new Informe(carpeta.getPath());
    }

    /** Revisa toda la carpeta. Nunca lanza por datos inválidos: los deja en el informe. */
    public static Informe validar(File carpeta) {
        return new ValidadorContenido(carpeta).revisar();
    }

    private Informe revisar() {
        Registro<TipoHabilidad> tipos = ReglasCombate.tiposHabilidad();
        Registro<EfectoEstado> estados = ReglasCombate.estados();

        etapa(Arrays.asList("configuracion.json"), true, () -> {
            new ProveedorConfiguracion(Partida.esquema().porDefecto()).reemplazarDesde(
                    fuente.cargar("configuracion", EsquemaConfiguracion.TIPO, EsquemaConfiguracion.VERSION));
            return Boolean.TRUE;
        });
        CatalogoCombate catalogo = etapa(Arrays.asList("habilidades.json", "combatientes.json"), true,
                () -> CatalogoCombate.cargar(fuente, tipos, estados));
        CatalogoObjetos objetos = etapa(Arrays.asList("objetos.json"), catalogo != null,
                () -> CatalogoObjetos.cargar(fuente, catalogo, tipos, estados));
        etapa(Arrays.asList("progresion.json"), catalogo != null, () -> TablaProgresion.cargar(fuente, catalogo));
        etapa(Arrays.asList("botin.json"), catalogo != null && objetos != null,
                () -> TablaBotin.cargar(fuente, catalogo, objetos));
        TablaEncuentros encuentros = etapa(Arrays.asList("encuentros.json"), catalogo != null,
                () -> TablaEncuentros.cargar(fuente, catalogo));
        Servicios servicios = null;
        if (fuente.existe("servicios")) {
            servicios = etapa(Arrays.asList("servicios.json"), catalogo != null && objetos != null,
                    () -> Servicios.cargar(fuente, objetos, catalogo));
        } else {
            servicios = Servicios.vacio();
        }

        for (String id : ids("mapas")) {
            String doc = "mapas/" + id + ".json";
            final TablaEncuentros te = encuentros;
            final Servicios sv = servicios;
            Mapa m = etapa(Arrays.asList(doc), true, () -> Mapa.cargar(fuente, id));
            if (m != null && (te == null || sv == null)) {
                // El mapa se lee bien, pero no hay con qué comprobar sus zonas y lugares.
                informe.resultados.set(informe.resultados.size() - 1, new Resultado(doc, Estado.OMITIDO,
                        "sus zonas y lugares dependen de encuentros.json y servicios.json"));
            } else if (m != null) {
                etapaReemplazando(doc, () -> {
                    te.validarMapa(m);
                    sv.validarMapa(m);
                    return Boolean.TRUE;
                });
            }
        }
        for (String id : ids("escenas")) {
            etapa(Arrays.asList("escenas/" + id + ".json"), true, () -> Guion.cargar(fuente, id));
        }

        boolean todoBien = informe.errores() == 0 && informe.omitidos() == 0;
        etapa(Arrays.asList("inicio.json"), todoBien, () -> Partida.nueva(fuente, new AzarSemilla(1)));
        return informe;
    }

    /** Ids (nombre sin extensión) de los .json de una subcarpeta, en orden. */
    private List<String> ids(String subcarpeta) {
        List<String> ids = new ArrayList<>();
        File[] archivos = new File(carpeta, subcarpeta).listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                if (f.isFile() && f.getName().endsWith(".json")) {
                    ids.add(f.getName().substring(0, f.getName().length() - 5));
                }
            }
        }
        Collections.sort(ids);
        return ids;
    }

    /**
     * Ejecuta la comprobación de un grupo de documentos. Si no se puede intentar (depende de otro con
     * error) los marca {@code OMITIDO}; si falla, el documento nombrado en el mensaje queda {@code ERROR}
     * y los demás del grupo, {@code OMITIDO}.
     */
    private <T> T etapa(List<String> docs, boolean puede, Supplier<T> trabajo) {
        if (!puede) {
            for (String d : docs) {
                informe.resultados.add(new Resultado(d, Estado.OMITIDO, "depende de documentos con error"));
            }
            return null;
        }
        try {
            T valor = trabajo.get();
            for (String d : docs) {
                informe.resultados.add(new Resultado(d, Estado.OK, null));
            }
            return valor;
        } catch (RuntimeException e) {
            String msg = mensaje(e);
            String culpable = docs.get(0);
            for (String d : docs) {
                if (msg.startsWith(d) || msg.startsWith(d.substring(0, d.length() - 5))) {
                    culpable = d;
                    break;
                }
            }
            // Los documentos del grupo se leen en el orden dado: los anteriores al culpable ya estaban bien.
            boolean antes = true;
            for (String d : docs) {
                if (d.equals(culpable)) {
                    antes = false;
                    informe.resultados.add(new Resultado(d, Estado.ERROR, msg));
                } else {
                    informe.resultados.add(antes ? new Resultado(d, Estado.OK, null)
                            : new Resultado(d, Estado.OMITIDO, "depende de " + culpable + ", que tiene un error"));
                }
            }
            return null;
        }
    }

    /** Segunda comprobación del mismo documento (referencias cruzadas): reemplaza el OK por el error. */
    private void etapaReemplazando(String doc, Supplier<Boolean> trabajo) {
        try {
            trabajo.get();
        } catch (RuntimeException e) {
            informe.resultados.set(informe.resultados.size() - 1, new Resultado(doc, Estado.ERROR, mensaje(e)));
        }
    }

    private static String mensaje(RuntimeException e) {
        if (e instanceof ErrorDeDatos) {
            return e.getMessage();
        }
        return "error inesperado (" + e.getClass().getSimpleName() + "): " + e.getMessage();
    }

    private static final String USO = "Uso: validar-contenido <carpeta> [--json]";

    /** Punto de entrada probable sin terminar el proceso. Devuelve 0 válido, 1 con errores, 2 uso incorrecto. */
    public static int ejecutar(String[] args, PrintStream salida, PrintStream error) {
        String ruta = null;
        boolean json = false;
        for (String a : args) {
            if (a.equals("--json")) {
                json = true;
            } else if (a.startsWith("--") || ruta != null) {
                error.println(USO);
                return 2;
            } else {
                ruta = a;
            }
        }
        if (ruta == null) {
            error.println(USO);
            return 2;
        }
        File carpeta = new File(ruta);
        if (!carpeta.isDirectory()) {
            error.println("No existe la carpeta: " + ruta);
            error.println(USO);
            return 2;
        }
        Informe informe = validar(carpeta);
        salida.print(json ? informe.json() : informe.texto());
        return informe.ok() ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        PrintStream salida = new PrintStream(System.out, true, "UTF-8");
        PrintStream error = new PrintStream(System.err, true, "UTF-8");
        System.exit(ejecutar(args, salida, error));
    }
}
