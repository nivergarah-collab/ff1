package com.example.ff1.mundo;

import java.util.ArrayList;
import java.util.List;

import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.motor.config.Configuracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.fuentes.Azar;

/**
 * Decide cuándo salta un encuentro aleatorio. Lleva una cuenta atrás de pasos, tirada entre
 * {@code mundo.pasosMinimos} y {@code mundo.pasosMaximos}, que solo baja al pisar casillas con
 * zona. Al llegar a cero devuelve los enemigos de un grupo de esa zona y vuelve a tirar.
 */
public final class Encuentros {

    private final ProveedorConfiguracion config;
    private final TablaEncuentros tabla;
    private final Azar azar;
    private int restantes;

    public Encuentros(ProveedorConfiguracion config, TablaEncuentros tabla, Azar azar) {
        this.config = config;
        this.tabla = tabla;
        this.azar = azar;
        reiniciar();
    }

    /** Vuelve a tirar la cuenta atrás (por ejemplo al entrar en otro mapa). */
    public void reiniciar() {
        Configuracion c = config.actual();
        int min = c.entero(ConfiguracionMundo.PASOS_MINIMOS);
        int max = Math.max(min, c.entero(ConfiguracionMundo.PASOS_MAXIMOS));
        restantes = min + azar.entero(max - min + 1);
    }

    /** Pasos sobre casillas con zona que faltan para el próximo encuentro. */
    public int restantes() {
        return restantes;
    }

    /**
     * Se llama tras cada paso que movió al grupo. Devuelve los ids de los enemigos si salta un
     * encuentro, o {@code null} si no.
     */
    public List<String> trasPaso(Mapa.Casilla casilla) {
        if (casilla.zona == null) {
            return null;
        }
        restantes--;
        if (restantes > 0) {
            return null;
        }
        List<String> enemigos = tabla.elegir(casilla.zona, azar);
        reiniciar();
        return enemigos;
    }

    /** Paso del explorador y, si se movió, comprobación de encuentro sobre la casilla nueva. */
    public List<String> mover(Explorador explorador, Direccion d) {
        if (!explorador.mover(d)) {
            return null;
        }
        return trasPaso(explorador.mapa().casilla(explorador.x(), explorador.y()));
    }

    /** Crea los combatientes de un encuentro, listos para un {@code Combate}. */
    public static List<Combatiente> crearEnemigos(List<String> ids, CatalogoCombate catalogo) {
        List<Combatiente> enemigos = new ArrayList<>();
        for (String id : ids) {
            enemigos.add(new Combatiente(catalogo.combatiente(id)));
        }
        return enemigos;
    }
}
