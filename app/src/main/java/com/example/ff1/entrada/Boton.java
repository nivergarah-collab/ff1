package com.example.ff1.entrada;

/** Botones lógicos del juego, independientes de cómo se pulsen (pantalla táctil, teclado...). */
public enum Boton {
    ARRIBA, ABAJO, IZQUIERDA, DERECHA, ACEPTAR, CANCELAR, RAPIDO;

    public boolean esDireccion() {
        return this == ARRIBA || this == ABAJO || this == IZQUIERDA || this == DERECHA;
    }
}
