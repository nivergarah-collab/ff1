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
}
