package com.example.ff1.motor.fuentes;

/**
 * Tiempo que ve el motor. La lógica avanza por ticks simulados; solo la capa Android
 * convierte el tiempo real en ticks, así las pruebas no dependen del reloj.
 */
public interface Tiempo {

    /** Milisegundos transcurridos desde un origen arbitrario, nunca decrecientes. */
    long milisegundos();
}
