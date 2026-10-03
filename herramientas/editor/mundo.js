'use strict';
// Mapas (solo lectura) y escenas (edición de textos) del editor (H11). Funciones puras, sin DOM,
// con las reglas de docs/contrato-de-datos.md (`mapa` y `escena`). Navegador: window.Mundo; Node: require.
(function (raiz) {
  const M = {};
  const esObjeto = (v) => v !== null && typeof v === 'object' && !Array.isArray(v);

  // ---- Mapas ----

  /**
   * Cuadrícula lista para dibujar: [{x, y, simbolo, nombre, color, pasable, zona}] por fila, más las marcas
   * (inicio, salidas, lugares) y los problemas que se ven sin el motor (símbolo fuera de la leyenda, filas desiguales).
   */
  M.cuadricula = function (doc) {
    const salida = { filas: [], marcas: [], problemas: [], ancho: 0, alto: 0 };
    if (!esObjeto(doc) || !Array.isArray(doc.filas)) { salida.problemas.push('El mapa no tiene filas.'); return salida; }
    const leyenda = esObjeto(doc.leyenda) ? doc.leyenda : {};
    salida.alto = doc.filas.length;
    salida.ancho = doc.filas.reduce((m, f) => Math.max(m, String(f).length), 0);
    doc.filas.forEach((texto, y) => {
      const t = String(texto);
      if (t.length !== salida.ancho) salida.problemas.push('La fila ' + (y + 1) + ' mide ' + t.length + ' y debería medir ' + salida.ancho + '.');
      const fila = [];
      for (let x = 0; x < salida.ancho; x++) {
        const simbolo = t[x] === undefined ? '' : t[x];
        const def = leyenda[simbolo];
        if (simbolo !== '' && !def) salida.problemas.push('Fila ' + (y + 1) + ', columna ' + (x + 1) + ': el símbolo «' + simbolo + '» no está en la leyenda.');
        fila.push({
          x, y, simbolo, nombre: def && def.nombre ? def.nombre : '', color: def && /^#[0-9A-Fa-f]{6}$/.test(def.color || '') ? def.color : null,
          pasable: !!(def && def.pasable), zona: def && def.zona ? def.zona : null,
        });
      }
      salida.filas.push(fila);
    });
    const marcar = (tipo, p, texto) => {
      if (p && Number.isInteger(p.x) && Number.isInteger(p.y)) salida.marcas.push({ tipo, x: p.x, y: p.y, texto });
    };
    marcar('inicio', doc.inicio, 'Inicio del grupo');
    for (const s of Array.isArray(doc.salidas) ? doc.salidas : []) {
      marcar('salida', s, 'Salida a ' + s.mapa + ' (' + (s.destino ? s.destino.x + ',' + s.destino.y : '?') + ')' + (s.requiere ? ' · requiere ' + s.requiere : ''));
    }
    for (const l of Array.isArray(doc.lugares) ? doc.lugares : []) marcar('lugar', l, l.tipo + ': ' + l.ref);
    return salida;
  };

  /** Leyenda ordenada: [{simbolo, nombre, color, pasable, zona, usos}] con cuántas casillas usan cada símbolo. */
  M.leyenda = function (doc) {
    const usos = {};
    for (const f of Array.isArray(doc.filas) ? doc.filas : []) for (const c of String(f)) usos[c] = (usos[c] || 0) + 1;
    const l = esObjeto(doc.leyenda) ? doc.leyenda : {};
    return Object.keys(l).map((simbolo) => ({
      simbolo, nombre: l[simbolo].nombre || '', color: l[simbolo].color || null, pasable: !!l[simbolo].pasable, zona: l[simbolo].zona || null, usos: usos[simbolo] || 0,
    }));
  };

  // ---- Escenas ----

  const MARCADOR = /\{heroe:([a-z0-9-]+)\}/g;

  /** Marcadores {heroe:clase} de un texto (ids de clase), o null si hay una llave mal formada. */
  M.marcadores = function (texto) {
    const resto = String(texto).replace(MARCADOR, '');
    if (resto.includes('{') || resto.includes('}')) return null;
    return Array.from(String(texto).matchAll(MARCADOR), (m) => m[1]);
  };

  /** Errores de un documento `escena` con las reglas del contrato: [{linea, campo, mensaje}] (linea −1 = documento). */
  M.validarEscena = function (doc, idArchivo) {
    const e = [];
    const g = (linea, campo, mensaje) => e.push({ linea, campo, mensaje });
    if (!esObjeto(doc)) return [{ linea: -1, campo: '', mensaje: 'El documento debe ser un objeto.' }];
    if (doc.tipo !== 'escena') g(-1, 'tipo', 'Debe ser "escena".');
    if (doc.version !== 1) g(-1, 'version', 'Versión no soportada (debe ser 1).');
    if (doc.id !== idArchivo) g(-1, 'id', 'Debe ser igual al nombre del archivo ("' + idArchivo + '").');
    if (!Array.isArray(doc.lineas) || doc.lineas.length < 1 || doc.lineas.length > 300) {
      g(-1, 'lineas', 'Debe tener entre 1 y 300 líneas.');
      return e;
    }
    doc.lineas.forEach((l, i) => {
      if (!esObjeto(l)) { g(i, '', 'La línea debe ser un objeto.'); return; }
      const t = l.texto;
      if (typeof t !== 'string' || t.length < 1) g(i, 'texto', 'El texto es obligatorio.');
      else {
        if (t.length > 300) g(i, 'texto', 'Máximo 300 caracteres (tiene ' + t.length + ').');
        if (M.marcadores(t) === null) g(i, 'texto', 'Marcador mal formado: se espera {heroe:<clase>}.');
      }
      if (l.quien !== undefined) {
        if (typeof l.quien !== 'string') g(i, 'quien', 'Debe ser texto.');
        else {
          if (l.quien.length > 40) g(i, 'quien', 'Máximo 40 caracteres.');
          if (M.marcadores(l.quien) === null) g(i, 'quien', 'Marcador mal formado: se espera {heroe:<clase>}.');
        }
      }
    });
    return e;
  };

  /** Cambia el texto o el hablante de una línea; un hablante vacío quita el campo (narrador). */
  M.ponerLinea = function (doc, i, campo, valor) {
    if (campo !== 'texto' && campo !== 'quien') throw new Error('Campo no editable: ' + campo);
    const l = doc.lineas[i];
    if (!l) throw new Error('Línea inexistente: ' + i);
    if (campo === 'quien' && valor === '') delete l.quien;
    else l[campo] = valor;
  };

  M.agregarLinea = function (doc, despuesDe) {
    const l = { texto: '' };
    const pos = despuesDe === undefined ? doc.lineas.length : despuesDe + 1;
    doc.lineas.splice(pos, 0, l);
    return pos;
  };

  M.borrarLinea = function (doc, i) {
    if (i < 0 || i >= doc.lineas.length) throw new Error('Línea inexistente: ' + i);
    return doc.lineas.splice(i, 1)[0];
  };

  M.moverLinea = function (doc, i, delta) {
    const j = i + delta;
    if (i < 0 || i >= doc.lineas.length || j < 0 || j >= doc.lineas.length) return i;
    const [l] = doc.lineas.splice(i, 1);
    doc.lineas.splice(j, 0, l);
    return j;
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = M;
  else raiz.Mundo = M;
})(typeof window !== 'undefined' ? window : globalThis);
