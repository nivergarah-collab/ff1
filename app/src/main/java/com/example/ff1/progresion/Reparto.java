package com.example.ff1.progresion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.combate.Recompensa;

/** Reparte la experiencia de una victoria: cada héroe en pie recibe la experiencia completa. */
public final class Reparto {

    private Reparto() {
    }

    /** Devuelve, por héroe que la recibió, los niveles que subió (0 si ninguno). */
    public static Map<Heroe, Integer> experiencia(List<Heroe> grupo, Recompensa recompensa) {
        Map<Heroe, Integer> subidas = new LinkedHashMap<>();
        for (Heroe h : grupo) {
            if (h.vida() > 0) {
                subidas.put(h, h.ganarExperiencia(recompensa.experiencia));
            }
        }
        return subidas;
    }
}
