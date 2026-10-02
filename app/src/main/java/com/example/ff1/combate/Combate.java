package com.example.ff1.combate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.fuentes.Azar;

/**
 * Un combate entre héroes y enemigos: avanza la barra de tiempo, ejecuta la acción del
 * combatiente al que le toca, cierra su turno (estados) y detecta el final. Toda acción la
 * hace el primero de la cola de turnos; quien no puede actuar solo puede perder el turno.
 */
public final class Combate {

    public enum Estado {
        EN_CURSO, VICTORIA, DERROTA, HUIDA
    }

    private final Acciones acciones;
    private final Azar azar;
    private final BarraTiempo barra;
    private final List<Combatiente> heroes;
    private final List<Combatiente> enemigos;
    private final boolean huidaPermitida;
    private final ProveedorConfiguracion config;
    private final AvanceRapido avanceRapido;
    private final java.util.Map<Combatiente, Integer> turnosPropios = new java.util.IdentityHashMap<>();
    private Estado estado = Estado.EN_CURSO;

    public Combate(ProveedorConfiguracion config, Acciones acciones, Azar azar,
            List<Combatiente> heroes, List<Combatiente> enemigos, boolean huidaPermitida) {
        if (heroes.isEmpty() || enemigos.isEmpty()) {
            throw new IllegalArgumentException("se necesita al menos un héroe y un enemigo");
        }
        this.acciones = acciones;
        this.azar = azar;
        this.barra = new BarraTiempo(config);
        this.heroes = Collections.unmodifiableList(new ArrayList<>(heroes));
        this.enemigos = Collections.unmodifiableList(new ArrayList<>(enemigos));
        this.huidaPermitida = huidaPermitida;
        this.config = config;
        this.avanceRapido = new AvanceRapido(config);
        for (Combatiente h : heroes) {
            barra.inscribir(h);
        }
        for (Combatiente e : enemigos) {
            barra.inscribir(e);
        }
        actualizarEstado();
    }

    public Estado estado() {
        return estado;
    }

    public boolean terminado() {
        return estado != Estado.EN_CURSO;
    }

    public List<Combatiente> heroes() {
        return heroes;
    }

    public List<Combatiente> enemigos() {
        return enemigos;
    }

    public BarraTiempo barra() {
        return barra;
    }

    /** Avanza la barra; devuelve los que quedaron listos. Sin efecto si el combate terminó. */
    public List<Combatiente> avanzar(int ticks) {
        return terminado() ? Collections.<Combatiente>emptyList() : barra.avanzar(ticks);
    }

    public AvanceRapido avanceRapido() {
        return avanceRapido;
    }

    /**
     * Un paso de animación: avanza {@code combate.ticksPorPaso} ticks (multiplicados por el
     * avance rápido si está encendido), tick a tick, y se detiene en cuanto a alguien le toca
     * actuar. Así el orden de los turnos es el mismo con y sin avance rápido; mientras alguien
     * tiene el turno el tiempo espera. Devuelve los ticks avanzados.
     */
    public int avanzarPaso() {
        int maximo = avanceRapido.ticks(config.actual().entero(ConfiguracionCombate.TICKS_POR_PASO));
        int hechos = 0;
        while (hechos < maximo && turno() == null && !terminado()) {
            barra.avanzar(1);
            hechos++;
        }
        return hechos;
    }

    /** A quién le toca actuar, o {@code null}. */
    public Combatiente turno() {
        return terminado() ? null : barra.siguiente();
    }

    /** Avanza tick a tick hasta que a alguien le toque (como mucho {@code maxTicks}). */
    public Combatiente esperarTurno(int maxTicks) {
        for (int t = 0; t < maxTicks && turno() == null && !terminado(); t++) {
            barra.avanzar(1);
        }
        return turno();
    }

    public boolean puedeActuar() {
        Combatiente c = turno();
        return c != null && acciones.puedeActuar(c);
    }

    public ResultadoAccion atacar(Combatiente objetivo) {
        return ejecutar(() -> acciones.atacar(actor(), objetivo));
    }

    public ResultadoAccion usarHabilidad(Habilidad h, Combatiente objetivo) {
        return ejecutar(() -> acciones.usarHabilidad(actor(), h, objetivo));
    }

    public ResultadoAccion usarObjeto(Habilidad efecto, Combatiente objetivo) {
        return ejecutar(() -> acciones.usarObjeto(actor(), efecto, objetivo));
    }

    public ResultadoAccion huir() {
        List<Combatiente> rivales = actor().bando() == Bando.HEROE ? enemigos : heroes;
        return ejecutar(() -> acciones.huir(actor(), rivales, huidaPermitida));
    }

    /** El que tiene el turno lo pierde (por ejemplo, dormido). */
    public ResultadoAccion perderTurno() {
        Combatiente actor = actor();
        return cerrarTurno(actor, acciones.perderTurno(actor));
    }

    /**
     * Turno automático (enemigos, o héroes en una prueba): si no puede actuar pierde el
     * turno; si no, ataca a un rival vivo elegido al azar.
     */
    public ResultadoAccion turnoAutomatico() {
        Combatiente actor = actor();
        if (!acciones.puedeActuar(actor)) {
            return perderTurno();
        }
        List<Combatiente> vivos = vivos(actor.bando() == Bando.HEROE ? enemigos : heroes);
        Combatiente objetivo = vivos.get(azar.entero(vivos.size()));
        DefinicionCombatiente d = actor.definicion();
        if (d.golpeCada > 0 && d.golpeFuerte != null) {
            int n = turnosPropios.merge(actor, 1, Integer::sum);
            if (n % d.golpeCada == 0) {
                return usarHabilidad(d.golpeFuerte, objetivo);
            }
        }
        return atacar(objetivo);
    }

    /** Experiencia y oro de los enemigos caídos; solo tras la victoria. */
    public Recompensa recompensa() {
        if (estado != Estado.VICTORIA) {
            throw new IllegalStateException("no hay recompensa en estado " + estado);
        }
        return Recompensa.de(enemigos);
    }

    // --- Interno --------------------------------------------------------------------------

    private Combatiente actor() {
        Combatiente c = turno();
        if (c == null) {
            throw new IllegalStateException(terminado() ? "el combate terminó" : "nadie tiene el turno");
        }
        return c;
    }

    private ResultadoAccion ejecutar(Supplier<ResultadoAccion> accion) {
        Combatiente actor = actor();
        if (!acciones.puedeActuar(actor)) {
            throw new IllegalStateException(actor + " no puede actuar: debe perder el turno");
        }
        ResultadoAccion r = accion.get();
        if (r.fallo == ResultadoAccion.Fallo.SIN_MAGIA || r.fallo == ResultadoAccion.Fallo.OBJETIVO_CAIDO
                || r.fallo == ResultadoAccion.Fallo.HUIDA_PROHIBIDA) {
            return r; // acción no válida: conserva el turno para elegir otra
        }
        if (r.accion == ResultadoAccion.Accion.HUIR && r.exito()) {
            barra.consumirTurno(actor);
            estado = Estado.HUIDA;
            return r;
        }
        return cerrarTurno(actor, r);
    }

    private ResultadoAccion cerrarTurno(Combatiente actor, ResultadoAccion r) {
        barra.consumirTurno(actor);
        acciones.terminarTurno(actor);
        actualizarEstado();
        return r;
    }

    private void actualizarEstado() {
        if (vivos(heroes).isEmpty()) {
            estado = Estado.DERROTA;
        } else if (vivos(enemigos).isEmpty()) {
            estado = Estado.VICTORIA;
        }
    }

    private static List<Combatiente> vivos(List<Combatiente> lista) {
        List<Combatiente> v = new ArrayList<>();
        for (Combatiente c : lista) {
            if (c.vivo()) {
                v.add(c);
            }
        }
        return v;
    }
}
