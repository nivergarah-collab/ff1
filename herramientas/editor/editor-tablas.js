'use strict';
// Interfaz de los editores de tabla (H11): lista de filas a la izquierda y formulario de la fila elegida
// a la derecha. Toda la lógica (rutas, validación, duplicar, ids rotos) está en tablas.js.
(function (raiz) {
  const T = raiz.Tablas;
  const E = {};

  const el = raiz.Dom.el;

  // Selección por archivo: { tabla, fila }
  const seleccion = {};

  /**
   * Dibuja el editor de un archivo.
   * @param cont      contenedor DOM (se vacía)
   * @param o.archivo ruta del archivo (clave de la selección)
   * @param o.nombres nombres de tabla de ese archivo, en orden (uno, salvo servicios.json)
   * @param o.doc     documento (compartido y editado en el sitio)
   * @param o.refs    () => referencias vigentes (T.referencias)
   * @param o.todos   () => { nombreTabla: documento } para buscar usos de un id
   * @param o.onCambio () => void: avisa de cualquier cambio (para marcar «sin guardar»)
   */
  E.dibujar = function (cont, o) {
    const sel = seleccion[o.archivo] || (seleccion[o.archivo] = { tabla: o.nombres[0], fila: 0 });
    if (!o.nombres.includes(sel.tabla)) sel.tabla = o.nombres[0];
    let errores = [];

    const tabla = () => T.TABLAS[sel.tabla];
    const filas = () => T.filas(o.doc, tabla());

    function revalidar() {
      errores = T.validarTabla(o.doc, tabla(), o.refs());
    }

    function etiquetaFila(f, i) {
      const t = tabla();
      const clave = f && typeof f[t.clave] === 'string' && f[t.clave] ? f[t.clave] : '(sin ' + (t.clave === 'id' ? 'id' : t.clave) + ')';
      const nombre = f && typeof f.nombre === 'string' && f.nombre ? f.nombre : '';
      return { principal: nombre || clave, secundario: nombre ? clave : '' };
    }

    // ---- Pintado de errores sin volver a dibujar (conserva el foco) ----

    function pintarErrores() {
      revalidar();
      const deFila = errores.filter((e) => e.fila === sel.fila);
      for (const nodo of cont.querySelectorAll('[data-campo]')) {
        const campo = nodo.getAttribute('data-campo');
        const ms = deFila.filter((e) => e.campo === campo || e.campo.startsWith(campo + '[')).map((e) => e.mensaje);
        const aviso = nodo.querySelector(':scope > .error');
        if (aviso) aviso.textContent = ms.join(' ');
        const entrada = nodo.querySelector(':scope input, :scope select');
        if (entrada) entrada.setAttribute('aria-invalid', ms.length ? 'true' : 'false');
      }
      const lista = cont.querySelector('.filas');
      if (lista) {
        lista.querySelectorAll('button[data-fila]').forEach((b) => {
          const i = Number(b.getAttribute('data-fila'));
          b.classList.toggle('con-error', errores.some((e) => e.fila === i));
        });
      }
      const resumen = cont.querySelector('.resumen-errores');
      if (resumen) {
        const n = errores.length;
        resumen.textContent = n ? n + (n === 1 ? ' problema por corregir en esta tabla.' : ' problemas por corregir en esta tabla.') : 'Sin problemas en esta tabla.';
        resumen.className = 'resumen-errores ' + (n ? 'mal' : 'bien');
      }
    }

    function cambio() {
      o.onCambio();
      pintarErrores();
      // El nombre visible de la fila en la lista puede haber cambiado.
      const b = cont.querySelector('button[data-fila="' + sel.fila + '"]');
      if (b) {
        const e = etiquetaFila(filas()[sel.fila], sel.fila);
        b.querySelector('.principal').textContent = e.principal;
        b.querySelector('.secundario').textContent = e.secundario;
      }
    }

    // ---- Campos ----

    function campoBase(c, prefijo, hijo) {
      const etiqueta = el('label', {}, c.titulo + (c.obligatorio ? ' *' : ''));
      const caja = el('div', { clase: 'campo', 'data-campo': prefijo + c.ruta }, etiqueta, hijo);
      if (c.ayuda) caja.append(el('span', { clase: 'ayuda', texto: c.ayuda }));
      if (c.tipo === 'entero' || c.tipo === 'opcion') {
        const defecto = c.defecto !== undefined ? 'Por defecto ' + c.defecto + '.' : '';
        const rango = c.tipo === 'entero' ? 'Rango ' + c.min + '–' + c.max + '. ' : '';
        caja.append(el('span', { clase: 'ayuda', texto: rango + defecto }));
      }
      caja.append(el('span', { clase: 'error', role: 'alert' }));
      return caja;
    }

    function campoTexto(c, fila, prefijo) {
      const v = T.obtener(fila, c.ruta);
      const i = el('input', { type: 'text', value: v === undefined ? '' : String(v), autocomplete: 'off', spellcheck: 'false' });
      i.addEventListener('input', () => { T.poner(fila, c.ruta, i.value); cambio(); });
      return campoBase(c, prefijo, i);
    }

    function campoEntero(c, fila, prefijo) {
      const v = T.obtener(fila, c.ruta);
      const i = el('input', { type: 'number', step: '1', min: c.min, max: c.max, value: v === undefined ? '' : String(v) });
      if (typeof v !== 'number' && v !== undefined) i.value = String(v);
      i.addEventListener('input', () => {
        const n = i.value.trim() === '' ? undefined : Number(i.value);
        T.poner(fila, c.ruta, n);
        cambio();
      });
      return campoBase(c, prefijo, i);
    }

    function selector(opciones, actual, vacio, alElegir, ref) {
      const s = el('select', {});
      s.append(el('option', { value: '', texto: vacio }));
      for (const op of opciones) s.append(el('option', { value: op, texto: op }));
      if (actual !== undefined && actual !== '' && !opciones.includes(actual)) {
        s.append(el('option', { value: actual, texto: '⚠ ' + actual + (ref ? ' (no existe)' : ' (no válido)') }));
      }
      s.value = actual === undefined ? '' : actual;
      s.addEventListener('change', () => alElegir(s.value));
      return s;
    }

    function campoOpcion(c, fila, prefijo) {
      const v = T.obtener(fila, c.ruta);
      const s = selector(c.opciones, v, c.obligatorio ? '(elige)' : '(ninguno)', (x) => { T.poner(fila, c.ruta, x === '' ? undefined : x); cambio(); }, false);
      return campoBase(c, prefijo, s);
    }

    function campoRef(c, fila, prefijo) {
      const v = T.obtener(fila, c.ruta);
      const s = selector(o.refs()[c.ref] || [], v, c.obligatorio ? '(elige)' : '(ninguno)', (x) => { T.poner(fila, c.ruta, x === '' ? undefined : x); cambio(); }, true);
      return campoBase(c, prefijo, s);
    }

    function campoRefs(c, fila, prefijo) {
      const caja = el('div', { clase: 'lista-refs' });
      const dibujar = () => {
        caja.replaceChildren();
        const v = T.obtener(fila, c.ruta) || [];
        v.forEach((id, k) => {
          const s = selector(o.refs()[c.ref] || [], id, '(elige)', (x) => {
            const lista = (T.obtener(fila, c.ruta) || []).slice();
            lista[k] = x;
            T.poner(fila, c.ruta, lista);
            cambio();
          }, true);
          caja.append(el('div', { clase: 'item' }, s, el('button', { type: 'button', texto: 'Quitar', onclick: () => {
            const lista = (T.obtener(fila, c.ruta) || []).slice();
            lista.splice(k, 1);
            T.poner(fila, c.ruta, lista.length ? lista : undefined);
            dibujar(); cambio();
          } })));
        });
        const maximo = c.max || 999;
        const boton = el('button', { type: 'button', texto: 'Añadir', onclick: () => {
          const lista = (T.obtener(fila, c.ruta) || []).slice();
          lista.push('');
          T.poner(fila, c.ruta, lista);
          dibujar(); cambio();
        } });
        if (v.length >= maximo) boton.disabled = true;
        caja.append(boton);
      };
      dibujar();
      return campoBase(c, prefijo, caja);
    }

    function campoSubfilas(c, fila, prefijo) {
      const caja = el('div', { clase: 'subfilas' });
      const dibujar = () => {
        caja.replaceChildren();
        const subs = T.obtener(fila, c.ruta) || [];
        subs.forEach((sub, k) => {
          const tarjeta = el('div', { clase: 'subfila' });
          for (const cc of c.columnas) tarjeta.append(campo(cc, sub, prefijo + c.ruta + '[' + k + '].'));
          tarjeta.append(el('button', { type: 'button', texto: 'Quitar', onclick: () => {
            const lista = (T.obtener(fila, c.ruta) || []).slice();
            lista.splice(k, 1);
            fila[c.ruta] = lista;
            dibujar(); cambio();
          } }));
          caja.append(tarjeta);
        });
        caja.append(el('button', { type: 'button', texto: 'Añadir', onclick: () => {
          const lista = (T.obtener(fila, c.ruta) || []).slice();
          lista.push(c.plantilla());
          fila[c.ruta] = lista;
          dibujar(); cambio();
        } }));
      };
      dibujar();
      const caja2 = el('div', { clase: 'campo ancho', 'data-campo': prefijo + c.ruta }, el('label', { texto: c.titulo }), caja);
      if (c.minimo) caja2.append(el('span', { clase: 'ayuda', texto: 'Al menos ' + c.minimo + '.' }));
      caja2.append(el('span', { clase: 'error', role: 'alert' }));
      return caja2;
    }

    function campo(c, fila, prefijo) {
      switch (c.tipo) {
        case 'entero': return campoEntero(c, fila, prefijo);
        case 'opcion': return campoOpcion(c, fila, prefijo);
        case 'ref': return campoRef(c, fila, prefijo);
        case 'refs': return campoRefs(c, fila, prefijo);
        case 'subfilas': return campoSubfilas(c, fila, prefijo);
        default: return campoTexto(c, fila, prefijo);
      }
    }

    // ---- Acciones ----

    function agregar() {
      T.agregar(o.doc, tabla());
      sel.fila = filas().length - 1;
      o.onCambio(); render();
    }

    function duplicar() {
      if (!filas()[sel.fila]) return;
      T.duplicar(o.doc, tabla(), sel.fila);
      sel.fila += 1;
      o.onCambio(); render();
    }

    function borrar() {
      const f = filas()[sel.fila];
      if (!f) return;
      const clave = f[tabla().clave];
      let aviso = '¿Borrar «' + etiquetaFila(f, sel.fila).principal + '»?';
      if (clave && sel.tabla !== 'botin' && sel.tabla !== 'encuentros') {
        const ref = { combatientes: 'combatientes', habilidades: 'habilidades', objetos: 'objetos' }[sel.tabla];
        if (ref) {
          const usos = T.usosDe(clave, ref, o.todos());
          if (usos.length) {
            aviso += '\n\nSe usa en ' + usos.length + ' sitio(s): ' + usos.slice(0, 6).map((u) => u.tabla + ' #' + (u.fila + 1) + ' (' + u.campo + ')').join(', ') +
              (usos.length > 6 ? '…' : '') + '.\nQuedarán ids rotos que habrá que corregir antes de guardar.';
          }
        }
      }
      if (!window.confirm(aviso)) return;
      T.borrar(o.doc, tabla(), sel.fila);
      sel.fila = Math.max(0, Math.min(sel.fila, filas().length - 1));
      o.onCambio(); render();
    }

    // ---- Dibujo general ----

    function render() {
      cont.replaceChildren();
      if (o.nombres.length > 1) {
        const pestanas = el('div', { clase: 'pestanas', role: 'tablist' });
        for (const n of o.nombres) {
          pestanas.append(el('button', { type: 'button', role: 'tab', 'data-tabla': n, 'aria-selected': String(n === sel.tabla), texto: T.TABLAS[n].titulo,
            onclick: () => { sel.tabla = n; sel.fila = 0; render(); } }));
        }
        cont.append(pestanas);
      }
      revalidar();
      const t = tabla();
      const lista = filas();
      if (sel.fila >= lista.length) sel.fila = Math.max(0, lista.length - 1);

      const btnDuplicar = el('button', { type: 'button', texto: 'Duplicar', onclick: duplicar });
      const btnBorrar = el('button', { type: 'button', texto: 'Borrar', onclick: borrar });
      btnDuplicar.disabled = btnBorrar.disabled = !lista.length;
      const acciones = el('div', { clase: 'acciones-tabla' },
        el('button', { type: 'button', clase: 'primario', texto: 'Añadir ' + t.singular, onclick: agregar }), btnDuplicar, btnBorrar);

      const izq = el('div', { clase: 'panel-filas' }, el('h3', { texto: t.titulo + ' (' + lista.length + ')' }), acciones);
      const ul = el('ul', { clase: 'filas' });
      lista.forEach((f, i) => {
        const e = etiquetaFila(f, i);
        ul.append(el('li', {}, el('button', { type: 'button', 'data-fila': String(i), 'aria-current': String(i === sel.fila),
          clase: errores.some((x) => x.fila === i) ? 'con-error' : '',
          onclick: () => { sel.fila = i; render(); } },
        el('span', { clase: 'principal', texto: e.principal }), el('span', { clase: 'secundario', texto: e.secundario }))));
      });
      if (!lista.length) ul.append(el('li', { clase: 'vacio', texto: 'No hay filas todavía.' }));
      izq.append(ul);

      const der = el('div', { clase: 'panel-fila' });
      der.append(el('p', { clase: 'resumen-errores' }));
      der.append(el('p', { clase: 'nota', texto: 'Los campos con * son obligatorios. Los cambios se guardan con el botón Guardar de arriba.' }));
      const fila = lista[sel.fila];
      if (fila) {
        const rejilla = el('div', { clase: 'rejilla-campos' });
        for (const c of t.columnas) rejilla.append(campo(c, fila, ''));
        der.append(rejilla);
      } else {
        der.append(el('p', { clase: 'vacio', texto: 'Pulsa «Añadir ' + t.singular + '» para crear la primera fila.' }));
      }
      cont.append(el('div', { clase: 'dos-paneles' }, izq, der));
      pintarErrores();
    }

    render();
    return { errores: () => { revalidar(); return errores; } };
  };

  /** Errores de todas las tablas de un archivo (para decidir si se puede guardar). */
  E.erroresDe = function (nombres, doc, refs) {
    const salida = [];
    for (const n of nombres) {
      for (const e of T.validarTabla(doc, T.TABLAS[n], refs)) salida.push(Object.assign({ tabla: n }, e));
    }
    return salida;
  };

  raiz.EditorTablas = E;
})(window);
