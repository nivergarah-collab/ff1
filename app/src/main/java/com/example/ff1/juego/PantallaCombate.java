package com.example.ff1.juego;

import com.example.ff1.combate.Bando;
import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.combate.Habilidad;
import com.example.ff1.combate.ResultadoAccion;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.progresion.Heroe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Combate con barra de tiempo: el tiempo corre hasta que a alguien le toca; los enemigos actúan
 * solos y, en el turno de un héroe, se abre el menú (Atacar, Magia, Objeto, Huir) y se elige el
 * objetivo. Muestra vida, magia y barra de tiempo, respeta el avance rápido del juego y, al
 * terminar, reparte las recompensas y vuelve a la pantalla de origen (o al título si cayó el grupo).
 */
public final class PantallaCombate implements Pantalla {

    public enum Fase {
        MENSAJE, ESPERA, MENU, MAGIA, OBJETO, OBJETIVO, FIN
    }

    static final List<String> MENU = Arrays.asList("Atacar", "Magia", "Objeto", "Huir");
    private static final int VISIBLES = 4;

    private final Juego juego;
    private final Partida partida;
    private final Combate combate;
    private final Pantalla regreso;
    private Fase fase = Fase.MENSAJE;
    private int cursor;
    private int cursorMenu;
    private int restante;
    private String mensaje = "¡Aparecen enemigos!";
    private Combatiente actor;
    private Habilidad elegida;
    private String objetoElegido;
    private List<Combatiente> candidatos = new ArrayList<>();
    private Partida.Desenlace desenlace;
    private final List<String> lineasFin = new ArrayList<>();

    PantallaCombate(Juego juego, Partida partida, Combate combate, Pantalla regreso) {
        this.juego = juego;
        this.partida = partida;
        this.combate = combate;
        this.regreso = regreso;
        this.restante = msMensaje();
    }

    /** Tiempo que se ve cada mensaje; sale de la configuración vigente (Menú → Ajustes). */
    private int msMensaje() {
        return partida.config().actual().entero(ConfiguracionJuego.MS_MENSAJE);
    }

    public Fase fase() {
        return fase;
    }

    public Combate combate() {
        return combate;
    }

    public Combatiente actor() {
        return actor;
    }

    public String mensaje() {
        return mensaje;
    }

    public Partida.Desenlace desenlace() {
        return desenlace;
    }

    public int cursor() {
        return cursor;
    }

    @Override
    public void avanzar(int ms) {
        combate.avanceRapido().activar(juego.rapido());
        if (fase == Fase.MENSAJE) {
            restante -= ms * combate.avanceRapido().multiplicador();
            if (restante <= 0) {
                fase = Fase.ESPERA;
            }
        } else if (fase == Fase.ESPERA) {
            siguienteTurno();
        }
    }

    private void siguienteTurno() {
        if (combate.terminado()) {
            terminar();
            return;
        }
        Combatiente t = combate.turno();
        if (t == null) {
            combate.avanzarPaso();
            t = combate.turno();
            if (t == null) {
                return;
            }
        }
        if (t.bando() == Bando.ENEMIGO || !combate.puedeActuar()) {
            mostrar(Mensajes.de(combate.turnoAutomatico()));
        } else {
            actor = t;
            fase = Fase.MENU;
            cursor = 0;
        }
    }

    private void mostrar(String texto) {
        mensaje = texto;
        restante = msMensaje();
        fase = Fase.MENSAJE;
    }

    @Override
    public void pulsar(Boton b) {
        int paso = b == Boton.ABAJO || b == Boton.DERECHA ? 1 : b == Boton.ARRIBA || b == Boton.IZQUIERDA ? -1 : 0;
        switch (fase) {
            case MENSAJE:
                if (b == Boton.ACEPTAR) {
                    restante = 0;
                }
                break;
            case MENU:
                if (paso != 0 && (b == Boton.ARRIBA || b == Boton.ABAJO)) {
                    cursor = Menu.mover(cursor, paso, menuHabilitado());
                } else if (b == Boton.ACEPTAR && menuHabilitado().get(cursor)) {
                    elegirDelMenu();
                }
                break;
            case MAGIA:
            case OBJETO:
                List<Boolean> hab = fase == Fase.MAGIA ? magiaHabilitada() : todas(objetos().size());
                if (paso != 0 && (b == Boton.ARRIBA || b == Boton.ABAJO)) {
                    cursor = Menu.mover(cursor, paso, hab);
                } else if (b == Boton.CANCELAR) {
                    volverAlMenu();
                } else if (b == Boton.ACEPTAR && !hab.isEmpty() && hab.get(cursor)) {
                    if (fase == Fase.MAGIA) {
                        Habilidad h = magias().get(cursor);
                        elegirObjetivo(h, null, h.objetivo);
                    } else {
                        String id = objetos().get(cursor);
                        Habilidad efecto = partida.objetos().objeto(id).efecto;
                        elegirObjetivo(efecto, id, efecto.objetivo);
                    }
                }
                break;
            case OBJETIVO:
                if (paso != 0) {
                    cursor = Math.floorMod(cursor + paso, candidatos.size());
                } else if (b == Boton.CANCELAR) {
                    volverAlMenu();
                } else if (b == Boton.ACEPTAR) {
                    ejecutar(candidatos.get(cursor));
                }
                break;
            case FIN:
                if (b == Boton.ACEPTAR) {
                    if (desenlace.estado == Combate.Estado.DERROTA) {
                        juego.volverAlTitulo();
                    } else {
                        juego.irA(regreso);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void elegirDelMenu() {
        cursorMenu = cursor;
        switch (cursor) {
            case 0:
                elegirObjetivo(null, null, Habilidad.Objetivo.ENEMIGO);
                break;
            case 1:
                fase = Fase.MAGIA;
                cursor = Math.max(0, Menu.mover(-1 + magias().size(), 1, magiaHabilitada()));
                break;
            case 2:
                fase = Fase.OBJETO;
                cursor = 0;
                break;
            default:
                mostrar(Mensajes.de(combate.huir()));
                break;
        }
    }

    private void volverAlMenu() {
        fase = Fase.MENU;
        cursor = cursorMenu;
    }

    private void elegirObjetivo(Habilidad h, String objetoId, Habilidad.Objetivo objetivo) {
        elegida = h;
        objetoElegido = objetoId;
        if (objetivo == Habilidad.Objetivo.SI_MISMO) {
            ejecutar(actor);
            return;
        }
        candidatos = vivos(objetivo == Habilidad.Objetivo.ENEMIGO ? combate.enemigos() : combate.heroes());
        if (!candidatos.isEmpty()) {
            fase = Fase.OBJETIVO;
            cursor = 0;
        }
    }

    private void ejecutar(Combatiente objetivo) {
        ResultadoAccion r;
        if (elegida == null) {
            r = combate.atacar(objetivo);
        } else if (objetoElegido == null) {
            r = combate.usarHabilidad(elegida, objetivo);
        } else {
            r = combate.usarObjeto(elegida, objetivo);
            if (r.exito()) {
                partida.inventario().quitar(objetoElegido, 1);
            }
        }
        mostrar(Mensajes.de(r));
    }

    private void terminar() {
        desenlace = partida.terminarCombate(combate);
        lineasFin.clear();
        switch (desenlace.estado) {
            case VICTORIA:
                lineasFin.add("¡Victoria!");
                lineasFin.add("Experiencia " + desenlace.recompensa.experiencia + "   Oro " + desenlace.recompensa.oro);
                for (Map.Entry<Heroe, Integer> n : desenlace.niveles.entrySet()) {
                    if (n.getValue() > 0) {
                        lineasFin.add(n.getKey().nombre() + " sube a nivel " + n.getKey().nivel() + ".");
                    }
                }
                for (Map.Entry<String, Integer> o : desenlace.botin.entrySet()) {
                    lineasFin.add("Obtienen " + partida.objetos().objeto(o.getKey()).nombre + " x" + o.getValue() + ".");
                }
                break;
            case HUIDA:
                lineasFin.add("El grupo escapó.");
                break;
            default:
                lineasFin.add("El grupo ha caído...");
                lineasFin.add("Vuelve al título.");
                break;
        }
        fase = Fase.FIN;
    }

    // --- Datos del turno -------------------------------------------------------------------

    List<Boolean> menuHabilitado() {
        return Arrays.asList(true, !magias().isEmpty(), !objetos().isEmpty(), true);
    }

    private List<Habilidad> magias() {
        List<Habilidad> r = new ArrayList<>();
        if (actor != null) {
            for (String id : actor.definicion().habilidades) {
                r.add(partida.catalogo().habilidad(id));
            }
        }
        return r;
    }

    private List<Boolean> magiaHabilitada() {
        List<Boolean> r = new ArrayList<>();
        for (Habilidad h : magias()) {
            r.add(actor.magia() >= h.coste);
        }
        return r;
    }

    /** Ids de consumibles del inventario, en orden. */
    private List<String> objetos() {
        List<String> r = new ArrayList<>();
        for (Map.Entry<String, Integer> e : partida.inventario().contenido().entrySet()) {
            DefinicionObjeto d = partida.objetos().objeto(e.getKey());
            if (e.getValue() > 0 && d.categoria == DefinicionObjeto.Categoria.CONSUMIBLE && d.efecto != null) {
                r.add(e.getKey());
            }
        }
        return r;
    }

    private static List<Boolean> todas(int n) {
        List<Boolean> r = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            r.add(true);
        }
        return r;
    }

    private static List<Combatiente> vivos(List<Combatiente> lista) {
        List<Combatiente> r = new ArrayList<>();
        for (Combatiente c : lista) {
            if (c.vivo()) {
                r.add(c);
            }
        }
        return r;
    }

    // --- Dibujo ----------------------------------------------------------------------------

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Combate", Estilo.LETRA, Estilo.TEXTO);
        if (juego.rapido()) {
            e.texto(Escena.ANCHO - 8, 6, "Avance rápido x" + combate.avanceRapido().multiplicador(), Estilo.LETRA,
                    Estilo.RESALTE, Escena.Alineacion.DERECHA);
        }
        Estilo.ventana(e, 4, 28, Escena.ANCHO - 8, 160);
        List<Combatiente> enemigos = combate.enemigos();
        for (int i = 0; i < enemigos.size(); i++) {
            Combatiente c = enemigos.get(i);
            int y = 36 + i * 24;
            senalar(e, c, 10, y);
            e.texto(24, y, c.nombre(), Estilo.LETRA, c.vivo() ? Estilo.TEXTO : Estilo.APAGADO);
            Estilo.barra(e, 210, y + 4, 136, 8, c.vida(), c.vidaMaxima(), Estilo.VIDA_BAJA);
        }

        int cargaLlena = partida.config().actual().entero(ConfiguracionCombate.CARGA_LLENA);
        Estilo.ventana(e, 4, 192, Escena.ANCHO - 8, 122);
        List<Combatiente> heroes = combate.heroes();
        for (int i = 0; i < heroes.size(); i++) {
            Combatiente c = heroes.get(i);
            int y = 198 + i * 28;
            int color = c == actor && fase != Fase.ESPERA && fase != Fase.MENSAJE ? Estilo.RESALTE
                    : c.vivo() ? Estilo.TEXTO : Estilo.APAGADO;
            senalar(e, c, 10, y);
            e.texto(24, y, c.nombre(), 14, color);
            e.texto(120, y, "PV " + c.vida() + "/" + c.vidaMaxima(), 14, color);
            e.texto(260, y, "PM " + c.magia(), 14, color);
            boolean baja = c.vida() * 4 <= c.vidaMaxima();
            Estilo.barra(e, 24, y + 17, 150, 5, c.vida(), c.vidaMaxima(), baja ? Estilo.VIDA_BAJA : Estilo.VIDA);
            int carga = c.vivo() ? combate.barra().carga(c) : 0;
            Estilo.barra(e, 200, y + 17, 140, 5, carga, cargaLlena, carga >= cargaLlena ? Estilo.RESALTE : Estilo.TIEMPO);
        }

        Estilo.ventana(e, 4, 318, Escena.ANCHO - 8, Estilo.ALTO_UTIL - 322);
        dibujarPanel(e, 14, 326);
    }

    private void senalar(Escena e, Combatiente c, int x, int y) {
        if (fase == Fase.OBJETIVO && candidatos.get(cursor) == c) {
            e.texto(x, y, ">", Estilo.LETRA, Estilo.RESALTE);
        }
    }

    private void dibujarPanel(Escena e, int x, int y) {
        switch (fase) {
            case MENU:
                e.texto(x, y, "Turno de " + actor.nombre(), Estilo.LETRA, Estilo.RESALTE);
                Estilo.menu(e, x, y + Estilo.LINEA, MENU, cursor, menuHabilitado());
                break;
            case MAGIA:
                lista(e, x, y, "Magia (PM " + actor.magia() + ")", nombresMagia(), magiaHabilitada());
                break;
            case OBJETO:
                lista(e, x, y, "Objetos", nombresObjetos(), null);
                break;
            case OBJETIVO:
                e.texto(x, y, "Elige el objetivo", Estilo.LETRA, Estilo.RESALTE);
                e.texto(x, y + Estilo.LINEA, candidatos.get(cursor).nombre(), Estilo.LETRA, Estilo.TEXTO);
                e.texto(x, y + 3 * Estilo.LINEA, "Cancelar: volver", 14, Estilo.APAGADO);
                break;
            case FIN:
                for (int i = 0; i < lineasFin.size() && i < 5; i++) {
                    e.texto(x, y + i * Estilo.LINEA, lineasFin.get(i), Estilo.LETRA, i == 0 ? Estilo.RESALTE : Estilo.TEXTO);
                }
                break;
            default:
                List<String> lineas = Estilo.partir(mensaje, 38);
                for (int i = 0; i < lineas.size() && i < 5; i++) {
                    e.texto(x, y + i * Estilo.LINEA, lineas.get(i), Estilo.LETRA, Estilo.TEXTO);
                }
                break;
        }
    }

    /** Título y una ventana de {@link #VISIBLES} opciones que sigue al cursor. */
    private void lista(Escena e, int x, int y, String titulo, List<String> opciones, List<Boolean> hab) {
        e.texto(x, y, titulo, Estilo.LETRA, Estilo.RESALTE);
        int desde = Math.max(0, Math.min(cursor - VISIBLES + 1, opciones.size() - VISIBLES));
        int hasta = Math.min(opciones.size(), desde + VISIBLES);
        Estilo.menu(e, x, y + Estilo.LINEA, opciones.subList(desde, hasta), cursor - desde,
                hab == null ? null : hab.subList(desde, hasta));
    }

    private List<String> nombresMagia() {
        List<String> r = new ArrayList<>();
        for (Habilidad h : magias()) {
            r.add(h.nombre + "  " + h.coste + " PM");
        }
        return r;
    }

    private List<String> nombresObjetos() {
        List<String> r = new ArrayList<>();
        for (String id : objetos()) {
            r.add(partida.objetos().objeto(id).nombre + "  x" + partida.inventario().cantidad(id));
        }
        return r;
    }
}
