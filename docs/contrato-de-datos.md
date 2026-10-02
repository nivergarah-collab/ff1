# Contrato de datos · motor de ff1

Describe cómo el motor recibe contenido, configuración, azar, tiempo y guardado, y el formato de cada documento. Es la base del futuro molde del motor (ver "Diseño adaptable" en `mision-mvp.md`). Se actualiza en cada hito que añada o cambie un tipo de dato.

Estado: H1 en curso (`combatientes` y `habilidades` v1). Los demás tipos de contenido se completan en sus hitos.

## Formato
- **JSON** (RFC 8259), en UTF-8, leído con `motor.datos.LectorJson`, escrito en Java puro sin librerías. Funciona igual en la JVM de pruebas y en Android, y es el formato natural de una API futura.
- Claves repetidas, texto sobrante o JSON mal formado se rechazan con `ErrorDeDatos`, que indica línea y columna.
- Los accesos tipados (`Nodo.entero`, `Nodo.texto`...) fallan con la ruta del campo, por ejemplo `enemigos[2].vida: se esperaba entero y hay texto`.
- Números: sin decimales y sin exponente se leen como enteros; si no, como decimales.

## Reglas comunes de todo documento
| Campo | Tipo | Regla |
|---|---|---|
| `tipo` | texto | Obligatorio. Debe coincidir con el tipo que pide el motor. |
| `version` | entero | Obligatorio, ≥ 1 y ≤ la versión máxima que el motor sabe leer. |

- Un documento inválido se rechaza con un mensaje claro; el juego no se cierra (lo muestra y sigue o vuelve atrás).
- Identificadores (`id`): texto en minúsculas con guiones, único dentro de su tipo. El texto visible va aparte (`nombre`, `texto`), para poder traducir.
- Nombres genéricos: "combatiente", "habilidad", "objeto", nunca nombres propios del juego en el código.

## Interfaces del motor (`com.example.ff1.motor`)
| Interfaz | Para qué | Implementaciones del MVP |
|---|---|---|
| `fuentes.FuenteContenido` | `cargar(recurso, tipo, versionMaxima)` devuelve el documento validado | `FuenteContenidoJson` sobre un `LectorTexto` |
| `fuentes.LectorTexto` | Lee el texto de `<recurso>.json` | `LectorMemoria` (pruebas), `LectorArchivos` (carpeta), lector de assets de Android (pendiente, capa Android) |
| `config.ProveedorConfiguracion` | Configuración vigente, reemplazable en caliente; conserva la anterior si la nueva no es válida; avisa a oyentes | Única |
| `fuentes.Azar` | `entero(limite)` en [0, limite) | `AzarSemilla` (juego, reproducible), `AzarSecuencia` (pruebas) |
| `fuentes.Tiempo` | Milisegundos reales; solo la capa Android los convierte en ticks | `TiempoManual` (pruebas); reloj del sistema (pendiente, capa Android) |
| `fuentes.Almacen` | Guardar, cargar, comprobar y borrar texto por ranura | `AlmacenMemoria` (pruebas); archivos internos de Android (pendiente, H7) |
| `config.Registro<T>` | Extensiones por clave (habilidad, efecto, condición de victoria, escena) | Uno por tipo de extensión |

Una fuente remota (API) o un editor serían otra implementación de `LectorTexto` o `FuenteContenido`, y otro llamador de `ProveedorConfiguracion.reemplazarDesde`, sin tocar el motor.

## Organización de un paquete de contenido
Un paquete es una carpeta (en el juego, `app/src/main/assets/contenido/`) con un archivo por recurso. Cambiar de juego es cambiar el paquete.

```
contenido/
├── configuracion.json   ← tipo "configuracion"
├── combatientes.json    ← héroes y enemigos (H1)
├── habilidades.json     ← (H1)
├── objetos.json         ← (H2)
├── mapas/<id>.json      ← (H3)
└── escenas/<id>.json    ← (H4)
```

## Tipos de documento

### `configuracion` · versión 1
Parámetros de balance y ritmo ajustables en caliente. Cada módulo declara los suyos en un `EsquemaConfiguracion` (nombre, rango, valor por defecto); lo que falte toma el valor por defecto.

```json
{
  "tipo": "configuracion",
  "version": 1,
  "valores": {
    "combate.velocidadBarra": 10,
    "combate.avanceRapido": 2
  }
}
```

| Campo | Tipo | Regla |
|---|---|---|
| `valores` | objeto | Opcional. Claves con la forma `<módulo>.<parámetro>`. Una clave no declarada, un decimal en un parámetro entero o un valor fuera de rango rechazan el documento entero. |

Parámetros declarados: se listan aquí a medida que cada hito los añade.

| Parámetro | Tipo | Rango | Defecto | Hito |
|---|---|---|---|---|
| `combate.velocidadBarra` | entero | 1–100 | 10 | H1 |
| `combate.cargaLlena` | entero | 100–100000 | 1000 | H1 |

Barra de tiempo: en cada tick, cada combatiente vivo que no espera turno suma `max(1, velocidad × velocidadBarra / 10)`; con la carga llena entra en la cola de turnos. Empates en un mismo tick: primero el que más se pasó y, a igualdad, el inscrito antes.

### `habilidades` · versión 1
```json
{ "tipo": "habilidades", "version": 1, "lista": [
  { "id": "chispa", "nombre": "Chispa", "tipo": "danio", "coste": 4, "poder": 14, "objetivo": "enemigo" },
  { "id": "dardo-amargo", "nombre": "Dardo amargo", "tipo": "alteracion", "coste": 3, "poder": 4,
    "estado": { "id": "veneno", "duracion": 3 } }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id` | texto | Obligatorio, minúsculas con guiones, único. |
| `nombre` | texto | Obligatorio. Texto visible. |
| `tipo` | texto | Obligatorio. Clave registrada en el registro de tipos de habilidad del motor (por ahora `danio`, `curacion`, `alteracion`; su regla se define en H1, acciones). |
| `coste` | entero | 0–999, defecto 0. Magia que gasta. |
| `poder` | entero | 0–9999, defecto 0. |
| `objetivo` | texto | `enemigo` (defecto), `aliado` o `si-mismo`. |
| `estado` | objeto | Opcional: `id` (registrado en el registro de estados; por ahora `veneno`, `sueno`, `proteccion`) y `duracion` (1–99 turnos, obligatorio). |

### `combatientes` · versión 1
Clases de héroe y tipos de enemigo. El nombre del héroe lo elige el jugador; `nombre` es el de la clase.
```json
{ "tipo": "combatientes", "version": 1, "lista": [
  { "id": "arcanista", "nombre": "Arcanista", "bando": "heroe", "vida": 28, "magia": 24,
    "ataque": 5, "defensa": 4, "poder": 11, "velocidad": 9, "habilidades": ["chispa"] },
  { "id": "musgoso", "nombre": "Musgoso", "bando": "enemigo", "vida": 14, "ataque": 6,
    "defensa": 2, "velocidad": 6, "experiencia": 4, "oro": 3 }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id`, `nombre` | texto | Como en `habilidades`. |
| `bando` | texto | `heroe` o `enemigo`. |
| `vida` | entero | Obligatorio, 1–99999. |
| `magia` | entero | 0–9999, defecto 0. |
| `ataque`, `defensa` | entero | Obligatorios, 0–999. |
| `poder` | entero | 0–999, defecto 0. Potencia mágica. |
| `velocidad` | entero | Obligatorio, 1–255. Ritmo de la barra de tiempo. |
| `habilidades` | lista de texto | Opcional. Cada `id` debe existir en `habilidades`. |
| `experiencia`, `oro` | entero | 0–999999, defecto 0. Recompensa al vencerlo. |

### Guardado de partida
Pendiente (H7). Será un documento JSON con `tipo` `"partida"` y `version`, escrito con `EscritorJson` y guardado en un `Almacen`. Incluirá la semilla del azar.

### Tipos de contenido de juego
Pendientes: `objetos` (H2), `mapa` (H3), `escena` (H4), `tienda` (H5). Cada uno se documenta aquí con su versión, sus campos y un ejemplo al implementarlo.
