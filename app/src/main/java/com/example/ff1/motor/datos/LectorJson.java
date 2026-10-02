package com.example.ff1.motor.datos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector de JSON (RFC 8259) en Java puro, sin librerías, para que el mismo código
 * funcione en la JVM de pruebas y en Android. Los errores indican línea y columna.
 */
public final class LectorJson {

    private final String texto;
    private int pos;

    private LectorJson(String texto) {
        this.texto = texto;
    }

    public static Nodo leer(String texto) {
        if (texto == null) {
            throw new ErrorDeDatos("texto JSON nulo");
        }
        LectorJson l = new LectorJson(texto);
        l.espacios();
        Nodo n = l.valor("");
        l.espacios();
        if (l.pos < texto.length()) {
            throw l.error("texto sobrante después del valor");
        }
        return n;
    }

    private Nodo valor(String ruta) {
        if (pos >= texto.length()) {
            throw error("fin inesperado del texto");
        }
        char c = texto.charAt(pos);
        switch (c) {
            case '{':
                return objeto(ruta);
            case '[':
                return lista(ruta);
            case '"':
                return Nodo.de(cadena(), ruta);
            case 't':
                palabra("true");
                return Nodo.de(Boolean.TRUE, ruta);
            case 'f':
                palabra("false");
                return Nodo.de(Boolean.FALSE, ruta);
            case 'n':
                palabra("null");
                return Nodo.de(null, ruta);
            default:
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return Nodo.de(numero(), ruta);
                }
                throw error("carácter inesperado '" + c + "'");
        }
    }

    private Nodo objeto(String ruta) {
        pos++; // {
        Map<String, Nodo> mapa = new LinkedHashMap<>();
        espacios();
        if (consumir('}')) {
            return Nodo.de(Collections.unmodifiableMap(mapa), ruta);
        }
        do {
            espacios();
            if (pos >= texto.length() || texto.charAt(pos) != '"') {
                throw error("se esperaba una clave entre comillas");
            }
            int inicioClave = pos;
            String clave = cadena();
            if (mapa.containsKey(clave)) {
                pos = inicioClave;
                throw error("clave repetida \"" + clave + "\"");
            }
            espacios();
            esperar(':');
            espacios();
            mapa.put(clave, valor(Nodo.unir(ruta, clave)));
            espacios();
        } while (consumir(','));
        esperar('}');
        return Nodo.de(Collections.unmodifiableMap(mapa), ruta);
    }

    private Nodo lista(String ruta) {
        pos++; // [
        List<Nodo> lista = new ArrayList<>();
        espacios();
        if (consumir(']')) {
            return Nodo.de(Collections.unmodifiableList(lista), ruta);
        }
        do {
            espacios();
            lista.add(valor(ruta + "[" + lista.size() + "]"));
            espacios();
        } while (consumir(','));
        esperar(']');
        return Nodo.de(Collections.unmodifiableList(lista), ruta);
    }

    private String cadena() {
        pos++; // "
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= texto.length()) {
                throw error("texto sin comillas de cierre");
            }
            char c = texto.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c < 0x20) {
                pos--;
                throw error("carácter de control dentro de un texto");
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= texto.length()) {
                throw error("escape incompleto");
            }
            char e = texto.charAt(pos++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > texto.length()) {
                        throw error("escape \\u incompleto");
                    }
                    try {
                        sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("escape \\u inválido");
                    }
                    pos += 4;
                    break;
                default:
                    pos--;
                    throw error("escape inválido '\\" + e + "'");
            }
        }
    }

    private Object numero() {
        int inicio = pos;
        consumir('-');
        digitos();
        boolean decimal = false;
        if (consumir('.')) {
            decimal = true;
            digitos();
        }
        if (pos < texto.length() && (texto.charAt(pos) == 'e' || texto.charAt(pos) == 'E')) {
            decimal = true;
            pos++;
            if (!consumir('+')) {
                consumir('-');
            }
            digitos();
        }
        String s = texto.substring(inicio, pos);
        try {
            if (!decimal) {
                return Long.parseLong(s);
            }
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            pos = inicio;
            throw error("número inválido \"" + s + "\"");
        }
    }

    private void digitos() {
        int inicio = pos;
        while (pos < texto.length() && texto.charAt(pos) >= '0' && texto.charAt(pos) <= '9') {
            pos++;
        }
        if (pos == inicio) {
            throw error("se esperaba un dígito");
        }
    }

    private void palabra(String p) {
        if (!texto.startsWith(p, pos)) {
            throw error("se esperaba " + p);
        }
        pos += p.length();
    }

    private void espacios() {
        while (pos < texto.length()) {
            char c = texto.charAt(pos);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '﻿') {
                return;
            }
            pos++;
        }
    }

    private boolean consumir(char c) {
        if (pos < texto.length() && texto.charAt(pos) == c) {
            pos++;
            return true;
        }
        return false;
    }

    private void esperar(char c) {
        if (!consumir(c)) {
            throw error("se esperaba '" + c + "'");
        }
    }

    private ErrorDeDatos error(String mensaje) {
        int linea = 1;
        int columna = 1;
        for (int i = 0; i < pos && i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') {
                linea++;
                columna = 1;
            } else {
                columna++;
            }
        }
        return new ErrorDeDatos("JSON, línea " + linea + ", columna " + columna + ": " + mensaje);
    }
}
