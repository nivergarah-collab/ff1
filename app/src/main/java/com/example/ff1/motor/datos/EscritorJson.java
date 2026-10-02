package com.example.ff1.motor.datos;

import java.util.List;
import java.util.Map;

/** Escribe un {@link Nodo} como JSON compacto. Lo usa el guardado de partidas. */
public final class EscritorJson {

    private EscritorJson() {
    }

    public static String escribir(Nodo nodo) {
        StringBuilder sb = new StringBuilder();
        escribir(nodo, sb);
        return sb.toString();
    }

    private static void escribir(Nodo n, StringBuilder sb) {
        if (n.esObjeto()) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<String, Nodo> e : n.mapa().entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                cadena(e.getKey(), sb);
                sb.append(':');
                escribir(e.getValue(), sb);
            }
            sb.append('}');
        } else if (n.esLista()) {
            sb.append('[');
            List<Nodo> l = n.comoLista();
            for (int i = 0; i < l.size(); i++) {
                if (i > 0) sb.append(',');
                escribir(l.get(i), sb);
            }
            sb.append(']');
        } else if (n.valor() instanceof String) {
            cadena((String) n.valor(), sb);
        } else if (n.valor() instanceof Double) {
            double d = (Double) n.valor();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new ErrorDeDatos(n.ruta() + ": número no representable en JSON");
            }
            sb.append(d);
        } else {
            sb.append(n.valor()); // Long, Boolean o null
        }
    }

    private static void cadena(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
