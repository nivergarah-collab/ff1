package com.example.ff1.progresion;

import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.inventario.Bonos;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.inventario.Equipo;

/**
 * Héroe del grupo entre combates: clase, nombre elegido, experiencia, nivel y vida y magia
 * actuales. En combate se usa un {@link Combatiente} creado con {@link #entrarEnCombate()}.
 */
public final class Heroe {

    private final DefinicionCombatiente clase;
    private String nombre;
    private final TablaProgresion tabla;
    private final Equipo equipo;
    private int experiencia;
    private int nivel = 1;
    private int vida;
    private int magia;

    public Heroe(DefinicionCombatiente clase, String nombre, TablaProgresion tabla) {
        this.clase = clase;
        this.nombre = nombre;
        this.tabla = tabla;
        this.equipo = new Equipo(clase.id);
        DefinicionCombatiente s = estadisticas();
        this.vida = s.vida;
        this.magia = s.magia;
    }

    public String nombre() {
        return nombre;
    }

    /** Cambia el nombre (lo elige el jugador al empezar); no puede quedar vacío. */
    public void renombrar(String nuevo) {
        if (nuevo == null || nuevo.trim().isEmpty()) {
            throw new IllegalArgumentException("el nombre no puede quedar vacío");
        }
        this.nombre = nuevo;
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

    /** Estadísticas máximas del nivel actual, con los bonos del equipo puesto. */
    public DefinicionCombatiente estadisticas() {
        return con(equipo.bonos());
    }

    /** Estadísticas máximas que tendría con {@code pieza} en {@code ranura} ({@code null} = vacía). */
    public DefinicionCombatiente estadisticasCon(String ranura, DefinicionObjeto pieza) {
        return con(equipo.bonosCon(ranura, pieza));
    }

    private DefinicionCombatiente con(Bonos b) {
        DefinicionCombatiente d = tabla.enNivel(clase, nivel);
        return new DefinicionCombatiente(d.id, d.nombre, d.bando,
                Math.min(99999, d.vida + b.vida), Math.min(9999, d.magia + b.magia),
                Math.min(999, d.ataque + b.ataque), Math.min(999, d.defensa + b.defensa),
                Math.min(999, d.poder + b.poder), Math.min(255, d.velocidad + b.velocidad),
                d.habilidades, d.experiencia, d.oro);
    }

    /** Equipo puesto (solo lectura: se cambia con {@link #equipar} y {@link #desequipar}). */
    public java.util.Map<String, DefinicionObjeto> equipo() {
        return equipo.puestas();
    }

    /** Pone una pieza y devuelve la que estaba en esa ranura (o {@code null}). */
    public DefinicionObjeto equipar(DefinicionObjeto pieza) {
        DefinicionObjeto anterior = equipo.equipar(pieza);
        ajustarAlMaximo();
        return anterior;
    }

    /** Quita la pieza de una ranura y la devuelve (o {@code null}). */
    public DefinicionObjeto desequipar(String ranura) {
        DefinicionObjeto quitada = equipo.quitar(ranura);
        ajustarAlMaximo();
        return quitada;
    }

    private void ajustarAlMaximo() {
        DefinicionCombatiente s = estadisticas();
        vida = Math.min(vida, s.vida);
        magia = Math.min(magia, s.magia);
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

    /**
     * Pone al héroe como estaba al guardar: experiencia (de ella sale el nivel), vida y magia. Hay que
     * llamarlo con el equipo ya puesto, porque los máximos dependen de él; lanza
     * {@link IllegalArgumentException} si vida o magia pasan del máximo.
     */
    public void restaurarEstado(int experiencia, int vida, int magia) {
        if (experiencia < 0) {
            throw new IllegalArgumentException("experiencia negativa: " + experiencia);
        }
        this.experiencia = experiencia;
        this.nivel = tabla.nivelCon(experiencia);
        DefinicionCombatiente s = estadisticas();
        if (vida < 0 || vida > s.vida || magia < 0 || magia > s.magia) {
            throw new IllegalArgumentException("vida o magia fuera de rango para " + nombre);
        }
        this.vida = vida;
        this.magia = magia;
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
