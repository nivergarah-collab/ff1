package com.example.ff1.motor.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;

/**
 * Declara los parámetros ajustables del motor (nombre, rango y valor por defecto).
 * Cada módulo del motor declara los suyos; los valores llegan en un documento
 * {@code "configuracion"} (ver docs/contrato-de-datos.md) y se validan aquí.
 */
public final class EsquemaConfiguracion {

    public static final String TIPO = "configuracion";
    public static final int VERSION = 1;

    /** Definición de un parámetro. */
    static final class Parametro {
        final String nombre;
        final boolean entero;
        final double minimo;
        final double maximo;
        final double defecto;

        Parametro(String nombre, boolean entero, double minimo, double maximo, double defecto) {
            this.nombre = nombre;
            this.entero = entero;
            this.minimo = minimo;
            this.maximo = maximo;
            this.defecto = defecto;
        }

        double validar(Number valor) {
            double v = valor.doubleValue();
            if (entero && (v != Math.rint(v) || valor instanceof Double || valor instanceof Float)) {
                throw new ErrorDeDatos(nombre + ": se esperaba un entero y hay " + valor);
            }
            if (Double.isNaN(v) || v < minimo || v > maximo) {
                throw new ErrorDeDatos(nombre + ": " + valor + " fuera del rango ["
                        + texto(minimo) + ", " + texto(maximo) + "]");
            }
            return v;
        }

        private String texto(double d) {
            return entero ? String.valueOf((long) d) : String.valueOf(d);
        }
    }

    private final Map<String, Parametro> parametros = new LinkedHashMap<>();

    public EsquemaConfiguracion entero(String nombre, int minimo, int maximo, int defecto) {
        return declarar(new Parametro(nombre, true, minimo, maximo, defecto));
    }

    public EsquemaConfiguracion decimal(String nombre, double minimo, double maximo, double defecto) {
        return declarar(new Parametro(nombre, false, minimo, maximo, defecto));
    }

    private EsquemaConfiguracion declarar(Parametro p) {
        if (parametros.containsKey(p.nombre)) {
            throw new IllegalStateException("parámetro repetido: " + p.nombre);
        }
        if (p.minimo > p.maximo || p.defecto < p.minimo || p.defecto > p.maximo) {
            throw new IllegalArgumentException("rango o valor por defecto inválido: " + p.nombre);
        }
        parametros.put(p.nombre, p);
        return this;
    }

    Parametro parametro(String nombre) {
        Parametro p = parametros.get(nombre);
        if (p == null) {
            throw new IllegalArgumentException("parámetro no declarado: " + nombre);
        }
        return p;
    }

    /** Nombres de los parámetros declarados, en el orden de declaración (los usan las herramientas). */
    public java.util.List<String> nombres() {
        return new java.util.ArrayList<>(parametros.keySet());
    }

    public boolean esEntero(String nombre) {
        return parametro(nombre).entero;
    }

    public double minimo(String nombre) {
        return parametro(nombre).minimo;
    }

    public double maximo(String nombre) {
        return parametro(nombre).maximo;
    }

    public double defecto(String nombre) {
        return parametro(nombre).defecto;
    }

    Map<String, Parametro> parametros() {
        return Collections.unmodifiableMap(parametros);
    }

    /** Configuración con todos los valores por defecto. */
    public Configuracion porDefecto() {
        Map<String, Double> valores = new LinkedHashMap<>();
        for (Parametro p : parametros.values()) {
            valores.put(p.nombre, p.defecto);
        }
        return new Configuracion(this, valores);
    }

    /** Crea una configuración desde un documento; lo que falte toma el valor por defecto. */
    public Configuracion crear(Nodo doc) {
        Documentos.exigir(doc, TIPO, VERSION);
        Map<String, Double> valores = new LinkedHashMap<>();
        for (Parametro p : parametros.values()) {
            valores.put(p.nombre, p.defecto);
        }
        if (doc.tiene("valores")) {
            Nodo v = doc.objeto("valores");
            for (String clave : v.claves()) {
                Parametro p = parametros.get(clave);
                if (p == null) {
                    throw new ErrorDeDatos("valores." + clave + ": parámetro desconocido");
                }
                Object crudo = v.campo(clave).aJava();
                if (!(crudo instanceof Number)) {
                    throw new ErrorDeDatos(clave + ": se esperaba un número");
                }
                valores.put(clave, p.validar((Number) crudo));
            }
        }
        return new Configuracion(this, valores);
    }
}
