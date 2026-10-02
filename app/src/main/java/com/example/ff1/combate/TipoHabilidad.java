package com.example.ff1.combate;

/**
 * Regla que resuelve un tipo de habilidad (la clave {@code tipo} de los datos). Se registra
 * en un {@code Registro<TipoHabilidad>}; añadir un tipo nuevo no toca el núcleo del combate.
 */
public interface TipoHabilidad {

    /**
     * Aplica el efecto principal de {@code habilidad} sobre {@code objetivo} (vivo y válido).
     *
     * @param poderActor potencia mágica de quien la usa (0 para un objeto)
     * @return la cantidad de vida cambiada (daño o curación), 0 si no cambia la vida
     */
    int resolver(Acciones acciones, int poderActor, Habilidad habilidad, Combatiente objetivo);

    /**
     * Si la regla actúa sobre combatientes caídos (por ejemplo, revivir). Los demás tipos no
     * pueden dirigirse a un caído. Un tipo que actúa sobre caídos solo se usa fuera de combate.
     */
    default boolean actuaSobreCaidos() {
        return false;
    }
}
