'use strict';
// Interfaz de mapas (solo lectura) y escenas (textos editables) del editor (H11).
(function (raiz) {
  const M = raiz.Mundo;
  const E = {};

  function el(etiqueta, atributos, ...hijos) {
    const nodo = document.createElement(etiqueta);
    for (const [k, v] of Object.entries(atributos || {})) {
      if (k === 'texto') nodo.textContent = v;
      else if (k === 'clase') nodo.className = v;
      else if (k.startsWith('on')) nodo.addEventListener(k.slice(2), v);
      else nodo.setAttribute(k, v);
    }
    for (const h of hijos) nodo.append(h);
    return nodo;
  }

  const SIGNO = { inicio: '★', salida: '➜', lugar: '◆' };

  /** Mapa por casillas en solo lectura, con leyenda, marcas y problemas visibles. */
  E.dibujarMapa = function (cont, doc, ruta) {
    cont.replaceChildren();
    const c = M.cuadricula(doc);
    cont.append(el('div', { clase: 'encabezado' }, el('h2', { texto: ruta }), el('span', { clase: 'insignia', texto: 'Solo lectura · mapa' })));
    cont.append(el('p', { clase: 'nota', texto: 'Vista del mapa por casillas (' + c.ancho + ' × ' + c.alto + '). Pasa el cursor por una casilla para ver qué es. ★ inicio del grupo, ➜ salida a otro mapa, ◆ lugar (tienda, posada, vecino, jefe). Los mapas se editan por ahora a mano en su JSON.' }));
    if (c.problemas.length) {
      const ul = el('ul');
      for (const p of c.problemas.slice(0, 8)) ul.append(el('li', { texto: p }));
      cont.append(el('div', { clase: 'aviso error' }, el('strong', { texto: 'Problemas del mapa:' }), ul));
    }
    const rejilla = el('div', { clase: 'mapa-rejilla', style: 'grid-template-columns: repeat(' + Math.max(1, c.ancho) + ', 22px);' });
    const marcasEn = {};
    for (const m of c.marcas) (marcasEn[m.x + ',' + m.y] = marcasEn[m.x + ',' + m.y] || []).push(m);
    for (const fila of c.filas) {
      for (const q of fila) {
        const ms = marcasEn[q.x + ',' + q.y] || [];
        const titulo = '(' + q.x + ', ' + q.y + ') ' + (q.nombre || (q.simbolo ? '«' + q.simbolo + '» sin leyenda' : 'fuera de las filas')) +
          (q.pasable ? '' : ' · no pasable') + (q.zona ? ' · zona ' + q.zona : '') + ms.map((m) => '\n' + m.texto).join('');
        const celda = el('div', { clase: 'casilla' + (q.pasable ? '' : ' bloqueada'), title: titulo, texto: ms.length ? SIGNO[ms[0].tipo] : q.simbolo });
        if (q.color) celda.style.background = q.color;
        rejilla.append(celda);
      }
    }
    cont.append(el('div', { clase: 'mapa-contenedor' }, rejilla));

    const t = el('table', { clase: 'tabla-balance' }, el('thead', {}, el('tr', {}, ...['Símbolo', 'Nombre', 'Pasable', 'Zona de encuentros', 'Casillas'].map((h) => el('th', { texto: h })))), el('tbody', {}));
    for (const l of M.leyenda(doc)) {
      const sw = el('span', { clase: 'muestra' });
      if (l.color) sw.style.background = l.color;
      t.tBodies[0].append(el('tr', {}, el('td', {}, sw, document.createTextNode(' ' + l.simbolo)), el('td', { texto: l.nombre }),
        el('td', { texto: l.pasable ? 'sí' : 'no' }), el('td', { texto: l.zona || '—' }), el('td', { texto: String(l.usos) })));
    }
    cont.append(el('h3', { texto: 'Leyenda' }), t);
    if (c.marcas.length) {
      const ul = el('ul', { clase: 'marcas' });
      for (const m of c.marcas) ul.append(el('li', { texto: SIGNO[m.tipo] + ' (' + m.x + ', ' + m.y + ') ' + m.texto }));
      cont.append(el('h3', { texto: 'Marcas del mapa' }), ul);
    }
  };

  /**
   * Editor de textos de una escena.
   * @param o.doc      documento `escena` (se edita en el sitio)
   * @param o.id       nombre del archivo sin extensión
   * @param o.onCambio () => void
   */
  E.dibujarEscena = function (cont, o) {
    cont.replaceChildren();
    const lineasEl = el('div', { clase: 'lineas-escena' });
    const resumen = el('p', { clase: 'resumen-errores' });

    function errores() { return M.validarEscena(o.doc, o.id); }

    function pintarErrores() {
      const es = errores();
      for (const caja of lineasEl.querySelectorAll('[data-linea]')) {
        const i = Number(caja.getAttribute('data-linea'));
        for (const campo of ['texto', 'quien']) {
          const ms = es.filter((e) => e.linea === i && e.campo === campo).map((e) => e.mensaje);
          const nodo = caja.querySelector('[data-campo="' + campo + '"]');
          nodo.querySelector('.error').textContent = ms.join(' ');
          nodo.querySelector('input, textarea').setAttribute('aria-invalid', ms.length ? 'true' : 'false');
        }
        const t = o.doc.lineas[i] && typeof o.doc.lineas[i].texto === 'string' ? o.doc.lineas[i].texto.length : 0;
        caja.querySelector('.contador').textContent = t + ' / 300';
      }
      const globales = es.filter((e) => e.linea === -1);
      resumen.textContent = es.length ? es.length + (es.length === 1 ? ' problema por corregir' : ' problemas por corregir') +
        (globales.length ? ': ' + globales.map((g) => g.campo + ' ' + g.mensaje).join(' ') : '.') : 'Sin problemas en esta escena.';
      resumen.className = 'resumen-errores ' + (es.length ? 'mal' : 'bien');
    }

    function cambio() { o.onCambio(); pintarErrores(); }

    function dibujarLineas() {
      lineasEl.replaceChildren();
      o.doc.lineas.forEach((l, i) => {
        const quien = el('input', { type: 'text', value: typeof l.quien === 'string' ? l.quien : '', placeholder: 'Narrador (vacío)', maxlength: '80', autocomplete: 'off' });
        quien.addEventListener('input', () => { M.ponerLinea(o.doc, i, 'quien', quien.value); cambio(); });
        const texto = el('textarea', { rows: '3' });
        texto.value = typeof l.texto === 'string' ? l.texto : '';
        texto.addEventListener('input', () => { M.ponerLinea(o.doc, i, 'texto', texto.value); cambio(); });
        lineasEl.append(el('div', { clase: 'linea-escena', 'data-linea': String(i) },
          el('div', { clase: 'numero', texto: String(i + 1) }),
          el('div', { clase: 'campos-linea' },
            el('div', { clase: 'campo', 'data-campo': 'quien' }, el('label', { texto: 'Quién habla' }), quien, el('span', { clase: 'error', role: 'alert' })),
            el('div', { clase: 'campo', 'data-campo': 'texto' }, el('label', { texto: 'Texto' }), texto, el('span', { clase: 'contador ayuda' }), el('span', { clase: 'error', role: 'alert' }))),
          el('div', { clase: 'botones-linea' },
            el('button', { type: 'button', texto: '↑', title: 'Subir', onclick: () => { M.moverLinea(o.doc, i, -1); o.onCambio(); dibujarLineas(); pintarErrores(); } }),
            el('button', { type: 'button', texto: '↓', title: 'Bajar', onclick: () => { M.moverLinea(o.doc, i, 1); o.onCambio(); dibujarLineas(); pintarErrores(); } }),
            el('button', { type: 'button', texto: 'Insertar debajo', onclick: () => { M.agregarLinea(o.doc, i); o.onCambio(); dibujarLineas(); pintarErrores(); } }),
            el('button', { type: 'button', texto: 'Borrar', onclick: () => {
              if (o.doc.lineas.length <= 1) { window.alert('Una escena necesita al menos una línea.'); return; }
              if (!window.confirm('¿Borrar la línea ' + (i + 1) + '?')) return;
              M.borrarLinea(o.doc, i); o.onCambio(); dibujarLineas(); pintarErrores();
            } }))));
      });
      // La primera línea no sube y la última no baja.
      lineasEl.querySelectorAll('button[title="Subir"]').forEach((b, k) => { b.disabled = k === 0; });
      lineasEl.querySelectorAll('button[title="Bajar"]').forEach((b, k, todos) => { b.disabled = k === todos.length - 1; });
    }

    cont.append(el('div', { clase: 'encabezado' }, el('h2', { texto: 'escenas/' + o.id + '.json' }),
      el('span', { clase: 'pendiente', texto: 'Cambios sin guardar', hidden: '' })));
    cont.append(el('p', { clase: 'nota', texto: 'Cada línea se muestra con un Aceptar. «Quién habla» vacío es el narrador. Para usar el nombre que el jugador puso a un héroe, escribe {heroe:clase} (por ejemplo {heroe:guardian}). Máximo 300 caracteres por línea.' }),
      resumen, lineasEl,
      el('p', {}, el('button', { type: 'button', clase: 'primario', texto: 'Añadir línea al final', onclick: () => { M.agregarLinea(o.doc); o.onCambio(); dibujarLineas(); pintarErrores(); } })));
    dibujarLineas();
    pintarErrores();
  };

  raiz.EditorMundo = E;
})(window);
