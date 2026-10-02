package com.example.ff1.combate;

import java.util.List;

/** Experiencia y oro ganados al vencer; el reparto y el botín llegan en H2. */
public final class Recompensa {

    public final int experiencia;
    public final int oro;

    public Recompensa(int experiencia, int oro) {
        this.experiencia = experiencia;
        this.oro = oro;
    }

    /** Suma lo que da cada enemigo caído de la lista. */
    public static Recompensa de(List<Combatiente> enemigos) {
        int xp = 0;
        int oro = 0;
        for (Combatiente e : enemigos) {
            if (!e.vivo()) {
                xp += e.definicion().experiencia;
                oro += e.definicion().oro;
            }
        }
        return new Recompensa(xp, oro);
    }
}
