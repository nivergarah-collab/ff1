package com.example.ff1.combate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Combatiente en un combate: una {@link DefinicionCombatiente} con vida, magia y estados
 * actuales. La vida y la magia nunca salen del rango [0, máximo].
 */
public final class Combatiente {

    private final DefinicionCombatiente definicion;
    private final String nombre;
    private int vida;
    private int magia;
    /** Estado → turnos restantes, en orden de aplicación. */
    private final Map<String, Integer> estados = new LinkedHashMap<>();

    public Combatiente(DefinicionCombatiente definicion) {
        this(definicion, definicion.nombre);
    }

    /** Con un nombre propio (los héroes los elige el jugador). */
    public Combatiente(DefinicionCombatiente definicion, String nombre) {
        this.definicion = definicion;
        this.nombre = nombre;
        this.vida = definicion.vida;
        this.magia = definicion.magia;
    }

    /** Con vida y magia actuales (un héroe herido que entra en combate). */
    public Combatiente(DefinicionCombatiente definicion, String nombre, int vida, int magia) {
        this(definicion, nombre);
        if (vida < 0 || vida > definicion.vida || magia < 0 || magia > definicion.magia) {
            throw new IllegalArgumentException("vida o magia fuera de rango: " + vida + ", " + magia);
        }
        this.vida = vida;
        this.magia = magia;
    }

    public DefinicionCombatiente definicion() {
        return definicion;
    }

    public String nombre() {
        return nombre;
    }

    public Bando bando() {
        return definicion.bando;
    }

    public int vida() {
        return vida;
    }

    public int vidaMaxima() {
        return definicion.vida;
    }

    public int magia() {
        return magia;
    }

    public int magiaMaxima() {
        return definicion.magia;
    }

    public int ataque() {
        return definicion.ataque;
    }

    public int defensa() {
        return definicion.defensa;
    }

    public int poder() {
        return definicion.poder;
    }

    public int velocidad() {
        return definicion.velocidad;
    }

    public boolean vivo() {
        return vida > 0;
    }

    /** Resta vida (mínimo 0); devuelve el daño realmente hecho. */
    public int recibirDanio(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("daño negativo: " + cantidad);
        }
        int hecho = Math.min(cantidad, vida);
        vida -= hecho;
        if (vida == 0) {
            estados.clear();
        }
        return hecho;
    }

    /** Suma vida a un combatiente vivo (máximo la vida máxima); devuelve lo curado. */
    public int curar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("curación negativa: " + cantidad);
        }
        if (!vivo()) {
            return 0;
        }
        int hecho = Math.min(cantidad, vidaMaxima() - vida);
        vida += hecho;
        return hecho;
    }

    /** Gasta magia si alcanza; devuelve {@code false} sin cambiar nada si no alcanza. */
    public boolean gastarMagia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("coste negativo: " + cantidad);
        }
        if (cantidad > magia) {
            return false;
        }
        magia -= cantidad;
        return true;
    }

    public int recuperarMagia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("recuperación negativa: " + cantidad);
        }
        int hecho = Math.min(cantidad, magiaMaxima() - magia);
        magia += hecho;
        return hecho;
    }

    /** Aplica o renueva un estado; se queda con la duración mayor. Sin efecto si está caído. */
    public void aplicarEstado(String estado, int turnos) {
        if (turnos < 1) {
            throw new IllegalArgumentException("duración inválida: " + turnos);
        }
        if (!vivo()) {
            return;
        }
        Integer actual = estados.get(estado);
        estados.put(estado, actual == null ? turnos : Math.max(actual, turnos));
    }

    public boolean tieneEstado(String estado) {
        return estados.containsKey(estado);
    }

    public void quitarEstado(String estado) {
        estados.remove(estado);
    }

    /** Estado → turnos restantes (solo lectura). */
    public Map<String, Integer> estados() {
        return Collections.unmodifiableMap(estados);
    }

    /** Descuenta un turno a cada estado y quita los que se agotan. */
    public void terminarTurno() {
        estados.replaceAll((k, v) -> v - 1);
        estados.values().removeIf(v -> v <= 0);
    }

    @Override
    public String toString() {
        return nombre + " (" + vida + "/" + vidaMaxima() + ")";
    }
}
