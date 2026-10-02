package com.example.ff1.motor.datos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Valor de un documento de datos: objeto, lista, texto, número, booleano o nulo.
 * Los accesos tipados lanzan {@link ErrorDeDatos} con la ruta del campo
 * (por ejemplo {@code enemigos[2].vida}) cuando falta o tiene otro tipo.
 */
public final class Nodo {

    private final Object valor;
    private final String ruta;

    private Nodo(Object valor, String ruta) {
        this.valor = valor;
        this.ruta = ruta;
    }

    // ---- Construcción ----

    static Nodo de(Object valor, String ruta) {
        return new Nodo(valor, ruta);
    }

    /** Crea un nodo a partir de valores Java simples (Map, List, String, Number, Boolean o null). */
    public static Nodo desde(Object valor) {
        return convertir(valor, "");
    }

    private static Nodo convertir(Object v, String ruta) {
        if (v instanceof Nodo) {
            return convertir(((Nodo) v).aJava(), ruta);
        }
        if (v instanceof Map) {
            Map<String, Nodo> mapa = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                String clave = String.valueOf(e.getKey());
                mapa.put(clave, convertir(e.getValue(), unir(ruta, clave)));
            }
            return new Nodo(Collections.unmodifiableMap(mapa), ruta);
        }
        if (v instanceof List) {
            List<Nodo> lista = new ArrayList<>();
            int i = 0;
            for (Object o : (List<?>) v) {
                lista.add(convertir(o, ruta + "[" + i++ + "]"));
            }
            return new Nodo(Collections.unmodifiableList(lista), ruta);
        }
        if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) {
            return new Nodo(((Number) v).longValue(), ruta);
        }
        if (v instanceof Number) {
            return new Nodo(((Number) v).doubleValue(), ruta);
        }
        if (v == null || v instanceof String || v instanceof Boolean) {
            return new Nodo(v, ruta);
        }
        throw new IllegalArgumentException("tipo no admitido: " + v.getClass().getName());
    }

    static String unir(String ruta, String clave) {
        return ruta.isEmpty() ? clave : ruta + "." + clave;
    }

    // ---- Consultas de tipo ----

    public String ruta() {
        return ruta.isEmpty() ? "(raíz)" : ruta;
    }

    public boolean esObjeto() {
        return valor instanceof Map;
    }

    public boolean esLista() {
        return valor instanceof List;
    }

    public boolean esNulo() {
        return valor == null;
    }

    // ---- Acceso a objetos ----

    public boolean tiene(String clave) {
        return esObjeto() && mapa().containsKey(clave);
    }

    public List<String> claves() {
        return new ArrayList<>(mapa().keySet());
    }

    /** Campo obligatorio. */
    public Nodo campo(String clave) {
        Nodo n = mapa().get(clave);
        if (n == null) {
            throw new ErrorDeDatos(unir(ruta, clave) + ": falta el campo");
        }
        return n;
    }

    public Nodo objeto(String clave) {
        Nodo n = campo(clave);
        if (!n.esObjeto()) {
            throw n.tipoIncorrecto("objeto");
        }
        return n;
    }

    public List<Nodo> lista(String clave) {
        return campo(clave).comoLista();
    }

    public String texto(String clave) {
        return campo(clave).comoTexto();
    }

    public int entero(String clave) {
        return campo(clave).comoEntero();
    }

    public double decimal(String clave) {
        return campo(clave).comoDecimal();
    }

    public boolean booleano(String clave) {
        return campo(clave).comoBooleano();
    }

    public String textoO(String clave, String defecto) {
        return tiene(clave) ? texto(clave) : defecto;
    }

    public int enteroO(String clave, int defecto) {
        return tiene(clave) ? entero(clave) : defecto;
    }

    public double decimalO(String clave, double defecto) {
        return tiene(clave) ? decimal(clave) : defecto;
    }

    public boolean booleanoO(String clave, boolean defecto) {
        return tiene(clave) ? booleano(clave) : defecto;
    }

    // ---- Conversión del propio valor ----

    @SuppressWarnings("unchecked")
    public List<Nodo> comoLista() {
        if (!esLista()) {
            throw tipoIncorrecto("lista");
        }
        return (List<Nodo>) valor;
    }

    public String comoTexto() {
        if (!(valor instanceof String)) {
            throw tipoIncorrecto("texto");
        }
        return (String) valor;
    }

    public int comoEntero() {
        if (!(valor instanceof Long)) {
            throw tipoIncorrecto("entero");
        }
        long l = (Long) valor;
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new ErrorDeDatos(ruta() + ": entero fuera de rango (" + l + ")");
        }
        return (int) l;
    }

    public double comoDecimal() {
        if (!(valor instanceof Number)) {
            throw tipoIncorrecto("número");
        }
        return ((Number) valor).doubleValue();
    }

    public boolean comoBooleano() {
        if (!(valor instanceof Boolean)) {
            throw tipoIncorrecto("booleano");
        }
        return (Boolean) valor;
    }

    /** Valor como objetos Java simples (LinkedHashMap, ArrayList, String, Long, Double, Boolean o null). */
    public Object aJava() {
        if (esObjeto()) {
            Map<String, Object> m = new LinkedHashMap<>();
            for (Map.Entry<String, Nodo> e : mapa().entrySet()) {
                m.put(e.getKey(), e.getValue().aJava());
            }
            return m;
        }
        if (esLista()) {
            List<Object> l = new ArrayList<>();
            for (Nodo n : comoLista()) {
                l.add(n.aJava());
            }
            return l;
        }
        return valor;
    }

    Object valor() {
        return valor;
    }

    @SuppressWarnings("unchecked")
    Map<String, Nodo> mapa() {
        if (!esObjeto()) {
            throw tipoIncorrecto("objeto");
        }
        return (Map<String, Nodo>) valor;
    }

    private ErrorDeDatos tipoIncorrecto(String esperado) {
        return new ErrorDeDatos(ruta() + ": se esperaba " + esperado + " y hay " + describir());
    }

    private String describir() {
        if (valor == null) return "nulo";
        if (valor instanceof Map) return "objeto";
        if (valor instanceof List) return "lista";
        if (valor instanceof String) return "texto";
        if (valor instanceof Boolean) return "booleano";
        if (valor instanceof Long) return "entero";
        return "decimal";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Nodo && Objects.equals(valor, ((Nodo) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(valor);
    }

    @Override
    public String toString() {
        return EscritorJson.escribir(this);
    }
}
