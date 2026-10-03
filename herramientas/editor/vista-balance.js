'use strict';
// Interfaz de la vista de balance (H11): tablas de experiencia y enfrentamientos calculadas con
// balance.js (las fórmulas del motor). Se recalcula al cambiar los niveles o la configuración.
(function (raiz) {
  const B = raiz.Balance;
  const V = {};

  const el = raiz.Dom.el;

  const num = (x) => (Number.isInteger(x) ? String(x) : x.toFixed(1));
  const rango = (r) => (r.min === r.max ? String(r.min) : r.min + '–' + r.max) + ' (≈' + num(r.medio) + ')';
  const golpes = (g) => (Number.isFinite(g) ? String(g) : '—');

  /** Convierte «1, 5 y 10» en [1, 5, 10] (enteros ≥ 1, sin repetir, hasta 6). */
  V.leerNiveles = function (texto, maximo) {
    const vistos = [];
    for (const t of String(texto).split(/[^0-9]+/)) {
      if (t === '') continue;
      const n = Number(t);
      if (n >= 1 && n <= maximo && !vistos.includes(n)) vistos.push(n);
    }
    return vistos.sort((a, b) => a - b).slice(0, 6);
  };

  /**
   * @param cont  contenedor DOM
   * @param o.docs     { combatientes, progresion } (documentos tal como están en el editor)
   * @param o.valores  () => valores de configuración vigentes (clave → número) o {}
   */
  V.dibujar = function (cont, o) {
    cont.replaceChildren();
    const docs = o.docs;
    const tabla = B.tablaExperiencia(docs.progresion);
    const maxNivel = tabla.length || 99;
    const estado = { niveles: [1, Math.min(5, maxNivel), Math.min(10, maxNivel)].filter((n, i, a) => a.indexOf(n) === i) };

    cont.append(el('div', { clase: 'encabezado' }, el('h2', { texto: 'Vista de balance' }), el('span', { clase: 'insignia', texto: 'Solo cálculo' })));
    cont.append(el('p', { clase: 'nota', texto: 'Cada héroe ataca con un golpe físico normal a cada enemigo, y viceversa. Usa las fórmulas del motor y los valores de la configuración que tienes abiertos ahora (con o sin guardar). Entre paréntesis, el daño medio. No cuenta magia, objetos ni defensa en combate.' }));

    const entrada = el('input', { type: 'text', value: estado.niveles.join(', '), 'aria-label': 'Niveles a comparar' });
    const salida = el('div', { id: 'salidaBalance' });
    cont.append(el('div', { clase: 'campo' }, el('label', { texto: 'Niveles de los héroes (hasta 6, de 1 a ' + maxNivel + ')' }), entrada), salida);

    function pintar() {
      salida.replaceChildren();
      const p = B.parametros(o.valores());
      salida.append(el('p', { clase: 'nota', texto: 'Fuerza física ' + p.fuerzaFisica + ' · fuerza mágica ' + p.fuerzaMagica + ' · varianza ±' + p.varianza + ' %.' }));
      const filas = B.enfrentamientos(docs, o.valores(), estado.niveles);
      if (!filas.length) { salida.append(el('p', { clase: 'vacio', texto: 'No hay héroes con niveles válidos para calcular.' })); }
      for (const f of filas) {
        const s = f.estadisticas;
        const t = el('table', { clase: 'tabla-balance' },
          el('thead', {}, el('tr', {}, ...['Enemigo', 'Daño que le haces', 'Golpes para vencerlo', 'Daño que recibes', 'Golpes para caer', 'Combates para subir'].map((h) => el('th', { texto: h })))),
          el('tbody', {}));
        for (const c of f.contra) {
          t.tBodies[0].append(el('tr', {}, el('td', { texto: c.enemigo }), el('td', { texto: rango(c.danioAlEnemigo) }), el('td', { texto: golpes(c.golpesParaVencerlo) }),
            el('td', { texto: rango(c.danioAlHeroe) }), el('td', { texto: golpes(c.golpesParaCaer) }), el('td', { texto: c.combatesParaSubir === null ? '—' : String(c.combatesParaSubir) })));
        }
        salida.append(el('h3', { texto: f.heroe + ' · nivel ' + f.nivel }),
          el('p', { clase: 'nota', texto: 'Vida ' + s.vida + ' · ataque ' + s.ataque + ' · defensa ' + s.defensa + ' · velocidad ' + s.velocidad }), t);
      }
      salida.append(el('h3', { texto: 'Experiencia por nivel' }));
      if (!tabla.length) salida.append(el('p', { clase: 'vacio', texto: 'No hay progresion.json en esta carpeta.' }));
      else {
        const te = el('table', { clase: 'tabla-balance' }, el('thead', {}, el('tr', {}, ...['Nivel', 'Experiencia acumulada', 'Para subir al siguiente'].map((h) => el('th', { texto: h })))), el('tbody', {}));
        for (const r of tabla) te.tBodies[0].append(el('tr', {}, el('td', { texto: String(r.nivel) }), el('td', { texto: String(r.acumulada) }), el('td', { texto: r.paraSubir === null ? 'nivel máximo' : String(r.paraSubir) })));
        salida.append(te);
      }
    }

    entrada.addEventListener('input', () => { estado.niveles = V.leerNiveles(entrada.value, maxNivel); pintar(); });
    pintar();
  };

  raiz.VistaBalance = V;
})(window);
