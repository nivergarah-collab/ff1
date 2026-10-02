package com.example.ff1.motor.datos;

/**
 * Reglas comunes de los documentos de datos: todo documento es un objeto con
 * {@code "tipo"} (texto) y {@code "version"} (entero ≥ 1). Ver docs/contrato-de-datos.md.
 */
public final class Documentos {

    private Documentos() {
    }

    /** Comprueba tipo y versión; devuelve la versión del documento. */
    public static int exigir(Nodo doc, String tipo, int versionMaxima) {
        if (!doc.esObjeto()) {
            throw new ErrorDeDatos("el documento debe ser un objeto JSON");
        }
        String tipoDoc = doc.texto("tipo");
        if (!tipoDoc.equals(tipo)) {
            throw new ErrorDeDatos("tipo: se esperaba \"" + tipo + "\" y hay \"" + tipoDoc + "\"");
        }
        int version = doc.entero("version");
        if (version < 1 || version > versionMaxima) {
            throw new ErrorDeDatos("version: " + tipo + " v" + version
                    + " no soportada (máxima " + versionMaxima + ")");
        }
        return version;
    }

    /** Entero obligatorio de {@code n.clave} dentro de [min, max]. */
    public static int rango(Nodo n, String clave, int min, int max) {
        return dentro(n, clave, n.entero(clave), min, max);
    }

    /** Entero opcional (con {@code defecto}) de {@code n.clave} dentro de [min, max]. */
    public static int rangoO(Nodo n, String clave, int min, int max, int defecto) {
        return dentro(n, clave, n.enteroO(clave, defecto), min, max);
    }

    /** Comprueba que {@code valor} (de {@code n.clave}) esté en [min, max]. */
    public static int dentro(Nodo n, String clave, int valor, int min, int max) {
        if (valor < min || valor > max) {
            throw new ErrorDeDatos(n.ruta() + "." + clave + ": " + valor
                    + " fuera del rango [" + min + ", " + max + "]");
        }
        return valor;
    }
}
