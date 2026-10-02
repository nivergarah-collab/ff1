package com.example.ff1.mundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.combate.Bando;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Grupos de enemigos posibles por zona (documento {@code "encuentros"}). Las casillas de un
 * mapa nombran su zona; al saltar un encuentro se elige un grupo de esa zona según su peso.
 */
public final class TablaEncuentros {

    public static final String TIPO = "encuentros";
    public static final int VERSION = 1;

    /** Un grupo de enemigos y su peso relativo dentro de la zona. */
    public static final class Grupo {
        public final List<String> enemigos;
        public final int peso;

        Grupo(List<String> enemigos, int peso) {
            this.enemigos = enemigos;
            this.peso = peso;
        }
    }

    private final Map<String, List<Grupo>> porZona;

    private TablaEncuentros(Map<String, List<Grupo>> porZona) {
        this.porZona = Collections.unmodifiableMap(porZona);
    }

    public static TablaEncuentros cargar(FuenteContenido fuente, CatalogoCombate combate) {
        return desde(fuente.cargar(TIPO, TIPO, VERSION), combate);
    }

    public static TablaEncuentros desde(Nodo doc, CatalogoCombate combate) {
        Documentos.exigir(doc, TIPO, VERSION);
        Map<String, List<Grupo>> mapa = new LinkedHashMap<>();
        for (Nodo z : doc.lista("zonas")) {
            String zona = z.texto("zona");
            if (mapa.containsKey(zona)) {
                throw new ErrorDeDatos(z.ruta() + ".zona: \"" + zona + "\" repetida");
            }
            List<Grupo> grupos = new ArrayList<>();
            for (Nodo g : z.lista("grupos")) {
                List<Nodo> ne = g.lista("enemigos");
                if (ne.isEmpty() || ne.size() > 6) {
                    throw new ErrorDeDatos(g.ruta() + ".enemigos: debe tener de 1 a 6 enemigos");
                }
                List<String> ids = new ArrayList<>();
                for (Nodo e : ne) {
                    String id = e.comoTexto();
                    if (combate.combatiente(id).bando != Bando.ENEMIGO) {
                        throw new ErrorDeDatos(e.ruta() + ": \"" + id + "\" no es un enemigo");
                    }
                    ids.add(id);
                }
                int peso = g.enteroO("peso", 1);
                if (peso < 1 || peso > 100) {
                    throw new ErrorDeDatos(g.ruta() + ".peso: " + peso + " fuera del rango [1, 100]");
                }
                grupos.add(new Grupo(Collections.unmodifiableList(ids), peso));
            }
            if (grupos.isEmpty()) {
                throw new ErrorDeDatos(z.ruta() + ".grupos: la zona no tiene grupos");
            }
            mapa.put(zona, Collections.unmodifiableList(grupos));
        }
        return new TablaEncuentros(mapa);
    }

    public boolean tiene(String zona) {
        return porZona.containsKey(zona);
    }

    /** Elige un grupo de la zona según los pesos; devuelve los ids de sus enemigos. */
    public List<String> elegir(String zona, Azar azar) {
        List<Grupo> grupos = porZona.get(zona);
        if (grupos == null) {
            throw new IllegalArgumentException("zona de encuentros desconocida: " + zona);
        }
        int total = 0;
        for (Grupo g : grupos) {
            total += g.peso;
        }
        int tirada = azar.entero(total);
        for (Grupo g : grupos) {
            tirada -= g.peso;
            if (tirada < 0) {
                return g.enemigos;
            }
        }
        throw new IllegalStateException("tirada fuera de los pesos");
    }

    /** Comprueba que cada zona usada por el mapa tenga grupos en esta tabla. */
    public void validarMapa(Mapa mapa) {
        for (int y = 0; y < mapa.alto; y++) {
            for (int x = 0; x < mapa.ancho; x++) {
                String zona = mapa.casilla(x, y).zona;
                if (zona != null && !tiene(zona)) {
                    throw new ErrorDeDatos("mapas/" + mapa.id + ": la zona \"" + zona
                            + "\" no está en " + TIPO);
                }
            }
        }
    }
}
