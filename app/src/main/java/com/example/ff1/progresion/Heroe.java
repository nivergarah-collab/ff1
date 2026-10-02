package com.example.ff1.progresion;

import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.DefinicionCombatiente;

/**
 * Héroe del grupo entre combates: clase, nombre elegido, experiencia, nivel y vida y magia
 * actuales. En combate se usa un {@link Combatiente} creado con {@link #entrarEnCombate()}.
 */
public final class Heroe {

    private final DefinicionCombatiente clase;
    private final String nombre;
    private final TablaProgresion tabla;
    private int experiencia;
    private int nivel = 1;
    private int vida;
    private int magia;

    public Heroe(DefinicionCombatiente clase, String nombre, TablaProgresion tabla) {
        this.clase = clase;
        this.nombre = nombre;
        this.tabla = tabla;
        DefinicionCombatiente s = estadisticas();
        this.vida = s.vida;
        this.magia = s.magia;
    }

    public String nombre() {
        return nombre;
    }

    public DefinicionCombatiente clase() {
        return clase;
    }

    public int nivel() {
        return nivel;
    }

    public int experiencia() {
        return experiencia;
    }

    public int vida() {
        return vida;
    }

    public int magia() {
        return magia;
    }

    /** Experiencia que falta para el siguiente nivel (0 en el máximo). */
    public int experienciaFaltante() {
        return nivel >= tabla.nivelMaximo() ? 0 : tabla.experienciaPara(nivel + 1) - experiencia;
    }

    /** Estadísticas máximas del nivel actual. */
    public DefinicionCombatiente estadisticas() {
        return tabla.enNivel(clase, nivel);
    }

    /**
     * Suma experiencia y sube los niveles que correspondan; cada nivel ganado suma a la
     * vida y la magia actuales lo que crece su máximo. Devuelve los niveles subidos.
     */
    public int ganarExperiencia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("experiencia negativa: " + cantidad);
        }
        DefinicionCombatiente antes = estadisticas();
        experiencia = (int) Math.min(Integer.MAX_VALUE, (long) experiencia + cantidad);
        int nuevo = tabla.nivelCon(experiencia);
        int subidos = nuevo - nivel;
        nivel = nuevo;
        if (subidos > 0 && vida > 0) {
            DefinicionCombatiente despues = estadisticas();
            vida = Math.min(despues.vida, vida + despues.vida - antes.vida);
            magia = Math.min(despues.magia, magia + despues.magia - antes.magia);
        }
        return subidos;
    }

    /** Vida y magia al máximo (posada). */
    public void restaurar() {
        DefinicionCombatiente s = estadisticas();
        vida = s.vida;
        magia = s.magia;
    }

    public Combatiente entrarEnCombate() {
        return new Combatiente(estadisticas(), nombre, vida, magia);
    }

    /** Guarda la vida y la magia con que terminó el combate. */
    public void salirDeCombate(Combatiente c) {
        vida = c.vida();
        magia = c.magia();
    }
}
