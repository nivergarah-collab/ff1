# Historia · Crónica de la Cantera

Historia original del MVP. Sigue el ritmo de `docs/investigacion-ritmo.md` (apertura corta, pueblo, exterior, mazmorra con jefe, cierre) sin tomar trama, nombres, lugares ni diálogos de ningún juego existente. Los nombres de héroes y enemigos ya existentes vienen de `inicio.json` y `combatientes.json`; el resto se crea con el contenido de H4–H6.

## Idea en una frase
Cuatro vecinos de Pozaluz bajan a la cantera vieja para averiguar por qué la fuente del pueblo dejó de correr y despiertan, sin querer, a quien la estaba bebiendo.

## Mundo
- **Pozaluz:** pueblo pequeño al borde de una pradera, que vive de una única fuente de agua clara. Hace nueve días la fuente quedó en un hilo y las ovejas dejaron de beber.
- **El campo de la Cantera:** pradera y matorral entre el pueblo y la cantera (el mapa `campo` de H3). Lo recorren musgosos, alas de hollín y, en el matorral, lagartos de cantera, atraídos por la humedad que falta.
- **La Cantera Hundida:** cantera abandonada de dos niveles, cerrada con una puerta de hierro. Hace nueve días un derrumbe abrió la galería baja.
- **El Soterrado:** gran figura de piedra fría que dormía bajo la galería baja y que, al despertar, empezó a absorber el agua del subsuelo. No es malvado: tiene sed y no entiende el daño que hace.

## Personajes
- **Bruna** (guardián): carga con la responsabilidad; es la hija de quien cerró la cantera hace años y guarda la llave.
- **Tadeo** (arcanista): curioso y poco prudente; sospecha que el agua "se está yendo hacia abajo" y no hacia ningún otro sitio.
- **Ilke** (rastreador): callada, sigue huellas y es la primera en notar que las del campo van todas hacia la cantera.
- **Mirta** (herbolaria): cuida a las ovejas del pueblo y a los demás; es la que insiste en no hacerle daño al Soterrado si se puede evitar.
- **Ofelia** (anciana de Pozaluz): da el objetivo, sabe de la puerta de la cantera y entrega la llave a Bruna.
- **Casilda** (posadera) y **Lupe** (tendera): ofrecen descanso y suministros y dan pistas de ritmo (H5).

## Etapas y escenas (cada una es una escena de texto de H4 o un lugar de H5–H6)
1. **Apertura** (escena `apertura`, 2 min): amanece en Pozaluz; el grupo se junta junto a la fuente casi seca. Mirta cuenta que las ovejas no beben; Ilke muestra las huellas; Ofelia pide a Bruna la llave de su familia. Objetivo claro: llegar a la Cantera Hundida.
2. **Pozaluz** (H5, 5 min): tienda, posada y vecinos que cuentan cómo cambió el pueblo en nueve días. Antes de salir, Lupe vende el primer equipo y Casilda cura por poco.
3. **El campo** (H3, ya jugable, 10–15 min): encuentros aleatorios y subida de nivel; en el sendero no hay combates. El grupo llega a la puerta de hierro, que se abre con la **Llave de cantera** (objeto clave).
4. **La Cantera Hundida** (H6, 10–15 min): dos niveles con un cofre opcional por nivel. En el nivel bajo aparecen criaturas con estados alterados (veneno, sueño) que obligan a usar objetos y magia.
5. **El Soterrado** (H6, jefe): el grupo lo encuentra bebiendo de una grieta. Antes del combate, una escena breve donde Mirta intenta calmarlo y Tadeo comprende qué es; el combate es inevitable. Tiene mucha vida y un golpe fuerte cada pocos turnos; se vence con el grupo en nivel 3 o 4 usando curación.
6. **Cierre** (escena `cierre`, 2 min): al caer, el Soterrado se deshace en arena fina y la grieta suelta el agua que guardaba; vuelve a correr la fuente. De regreso, Ofelia ofrece la última frase y queda la puerta abierta a nuevas historias (más allá del MVP).

## Tono y reglas de escritura
- Cálido y sencillo, con humor seco en los diálogos; frases cortas que caben en la ventana del juego (unos 30 caracteres por línea, ver `Estilo.partir`).
- Nada de nombres, lugares, objetos con nombre propio ni frases de otras franquicias.
- Los textos viven en datos (`escenas/<id>.json`), no en el código; los nombres de los héroes los pone el jugador y las escenas los usan por clase o por marcador, no por el nombre de ejemplo.
