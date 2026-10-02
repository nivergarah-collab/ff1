package com.example.ff1.combate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.motor.config.Configuracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;

/**
 * Barra de tiempo (ATB) por ticks simulados, sin reloj real. En cada tick, cada combatiente
 * vivo que no está esperando turno suma {@code max(1, velocidad × velocidadBarra / 10)};
 * al llegar a la carga llena entra en la cola de turnos y deja de cargar hasta actuar.
 * Si varios se llenan en el mismo tick, va primero el que más se pasó y, a igualdad,
 * el que se inscribió antes. Los parámetros se leen de la configuración vigente en cada tick.
 */
public final class BarraTiempo {

    private final ProveedorConfiguracion config;
    /** Combatiente → carga actual, en orden de inscripción. */
    private final Map<Combatiente, Integer> cargas = new LinkedHashMap<>();
    private final List<Combatiente> cola = new ArrayList<>();

    public BarraTiempo(ProveedorConfiguracion config) {
        this.config = config;
    }

    public void inscribir(Combatiente c) {
        inscribir(c, 0);
    }

    /** Inscribe con una carga inicial (por ejemplo, para un ataque por sorpresa). */
    public void inscribir(Combatiente c, int cargaInicial) {
        if (cargas.containsKey(c)) {
            throw new IllegalStateException("ya inscrito: " + c);
        }
        if (cargaInicial < 0) {
            throw new IllegalArgumentException("carga negativa: " + cargaInicial);
        }
        cargas.put(c, 0);
        if (cargaInicial > 0) {
            cargar(Collections.singletonList(c), cargaInicial, config.actual());
        }
    }

    public int carga(Combatiente c) {
        Integer v = cargas.get(c);
        if (v == null) {
            throw new IllegalArgumentException("no inscrito: " + c);
        }
        return v;
    }

    /** Lo que suma este combatiente en un tick con la configuración vigente. */
    public int incremento(Combatiente c) {
        int velocidadBarra = config.actual().entero(ConfiguracionCombate.VELOCIDAD_BARRA);
        return Math.max(1, c.velocidad() * velocidadBarra / 10);
    }

    /** Avanza {@code ticks} ticks; devuelve, en orden, los que quedaron listos para actuar. */
    public List<Combatiente> avanzar(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks negativos: " + ticks);
        }
        List<Combatiente> nuevos = new ArrayList<>();
        for (int t = 0; t < ticks; t++) {
            Configuracion actual = config.actual();
            List<Combatiente> cargando = new ArrayList<>();
            for (Combatiente c : cargas.keySet()) {
                if (!c.vivo()) {
                    cargas.put(c, 0);
                    cola.remove(c);
                } else if (!cola.contains(c)) {
                    cargando.add(c);
                }
            }
            nuevos.addAll(cargar(cargando, -1, actual));
        }
        return nuevos;
    }

    /** Suma la carga ({@code cantidad} fija, o el incremento si es -1) y encola a los llenos. */
    private List<Combatiente> cargar(List<Combatiente> quienes, int cantidad, Configuracion actual) {
        int llena = actual.entero(ConfiguracionCombate.CARGA_LLENA);
        final Map<Combatiente, Integer> sinTope = new LinkedHashMap<>();
        List<Combatiente> llenos = new ArrayList<>();
        for (Combatiente c : quienes) {
            int nueva = cargas.get(c) + (cantidad < 0 ? incremento(c) : cantidad);
            if (nueva >= llena) {
                sinTope.put(c, nueva);
                llenos.add(c);
                nueva = llena;
            }
            cargas.put(c, nueva);
        }
        // Orden estable: el que más se pasó primero; a igualdad, orden de inscripción.
        llenos.sort((a, b) -> Integer.compare(sinTope.get(b), sinTope.get(a)));
        cola.addAll(llenos);
        return llenos;
    }

    /** Primer combatiente vivo de la cola, o {@code null} si nadie está listo. */
    public Combatiente siguiente() {
        cola.removeIf(c -> !c.vivo());
        return cola.isEmpty() ? null : cola.get(0);
    }

    public boolean listo(Combatiente c) {
        return cola.contains(c) && c.vivo();
    }

    /** El combatiente actuó: sale de la cola y su barra vuelve a cero. */
    public void consumirTurno(Combatiente c) {
        if (!cola.remove(c)) {
            throw new IllegalStateException("no estaba listo: " + c);
        }
        cargas.put(c, 0);
    }

    /** Cola de turnos actual (solo lectura). */
    public List<Combatiente> cola() {
        return Collections.unmodifiableList(cola);
    }
}
