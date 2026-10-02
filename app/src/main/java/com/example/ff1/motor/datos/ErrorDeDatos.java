package com.example.ff1.motor.datos;

/**
 * Datos inválidos o con formato incorrecto. El mensaje indica dónde está el
 * problema (ruta del campo o línea y columna) para poder mostrarlo sin cerrar el juego.
 */
public class ErrorDeDatos extends RuntimeException {

    public ErrorDeDatos(String mensaje) {
        super(mensaje);
    }

    public ErrorDeDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
