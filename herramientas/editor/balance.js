'use strict';
// Vista de balance (H11): las fórmulas de combate.Acciones replicadas en JavaScript puro, sin DOM.
// Una prueba de Java (BalanceEditorTest) compara estas fórmulas con el motor real sobre los mismos
// casos de pruebas/balance-casos.json, así que si el motor cambia y esto no, la prueba falla.
// Todas las divisiones son enteras como en Java (los valores son positivos, así que truncar = redondear hacia abajo).
(function (raiz) {
  const B = {};
  const div = (a, b) => Math.trunc(a / b);

  /** Parámetros que usan las fórmulas, con los valores por defecto del contrato. */
  B.DEFECTOS = { fuerzaFisica: 10, fuerzaMagica: 10, varianza: 10 };

  B.parametros = function (valores) {
    const v = valores || {};
    return {
      fuerzaFisica: v['combate.fuerzaFisica'] !== undefined ? v['combate.fuerzaFisica'] : B.DEFECTOS.fuerzaFisica,
      fuerzaMagica: v['combate.fuerzaMagica'] !== undefined ? v['combate.fuerzaMagica'] : B.DEFECTOS.fuerzaMagica,
      varianza: v['combate.varianza'] !== undefined ? v['combate.varianza'] : B.DEFECTOS.varianza,
    };
  };

  /** Aplica la varianza de Acciones.variar a todos los valores posibles de r en [−v, +v]: {min, max, medio}. */
  function conVarianza(base, varianza, suelo) {
    const valores = [];
    if (varianza === 0) valores.push(base);
    else for (let r = -varianza; r <= varianza; r++) valores.push(div(base * (100 + r), 100));
    const f = suelo ? (x) => Math.max(suelo, x) : (x) => x;
    const finales = valores.map(f);
    const suma = finales.reduce((a, x) => a + x, 0);
    return { min: Math.min.apply(null, finales), max: Math.max.apply(null, finales), medio: suma / finales.length };
  }

  /** Acciones.danioFisico */
  B.danioFisico = function (ataque, defensa, p) {
    const base = div(ataque * p.fuerzaFisica, 10) - div(defensa, 2);
    return conVarianza(Math.max(1, base), p.varianza, 1);
  };

  /** Acciones.danioMagico */
  B.danioMagico = function (poderHabilidad, poderActor, defensa, p) {
    const base = poderHabilidad + div(poderActor * p.fuerzaMagica, 10) - div(defensa, 4);
    return conVarianza(Math.max(1, base), p.varianza, 1);
  };

  /** Acciones.curacion (sin tope de vida máxima: ese lo pone el combatiente). */
  B.curacion = function (poderHabilidad, poderActor, p) {
    return conVarianza(poderHabilidad + div(poderActor * p.fuerzaMagica, 10), p.varianza, 0);
  };

  /** Golpes necesarios para quitar `vida` con un daño por golpe. */
  B.golpesPara = function (vida, danio) {
    return danio > 0 ? Math.ceil(vida / danio) : Infinity;
  };

  /** TablaProgresion.enNivel: estadísticas de una clase en un nivel, con los topes del motor. */
  B.enNivel = function (clase, crecimiento, nivel) {
    const g = crecimiento || {};
    const n = nivel - 1;
    const crece = (k) => (clase[k] || 0) + (g[k] || 0) * n;
    return {
      vida: Math.min(99999, crece('vida')), magia: Math.min(9999, crece('magia')),
      ataque: Math.min(999, crece('ataque')), defensa: Math.min(999, crece('defensa')),
      poder: Math.min(999, crece('poder')), velocidad: Math.min(255, crece('velocidad')),
    };
  };

  /** Filas de experiencia: [{nivel, acumulada, paraSubir}] (paraSubir = null en el nivel máximo). */
  B.tablaExperiencia = function (progresion) {
    const xp = progresion && Array.isArray(progresion.experiencia) ? progresion.experiencia : [];
    return xp.map((acumulada, i) => ({ nivel: i + 1, acumulada, paraSubir: i + 1 < xp.length ? xp[i + 1] - acumulada : null }));
  };

  /**
   * Enfrentamientos de cada héroe (en los niveles pedidos) con cada enemigo, atacando sin más:
   * daño medio en ambos sentidos, golpes para vencer y combates con este enemigo para subir de nivel.
   */
  B.enfrentamientos = function (docs, valores, niveles) {
    const p = B.parametros(valores);
    const lista = docs.combatientes && Array.isArray(docs.combatientes.lista) ? docs.combatientes.lista : [];
    const heroes = lista.filter((c) => c.bando === 'heroe');
    const enemigos = lista.filter((c) => c.bando === 'enemigo');
    const clases = {};
    if (docs.progresion && Array.isArray(docs.progresion.clases)) for (const c of docs.progresion.clases) clases[c.id] = c.crecimiento;
    const tabla = B.tablaExperiencia(docs.progresion);
    const salida = [];
    for (const h of heroes) {
      for (const nivel of niveles) {
        if (tabla.length && nivel > tabla.length) continue;
        const s = B.enNivel(h, clases[h.id], nivel);
        const fila = { heroe: h.id, nivel, estadisticas: s, contra: [] };
        const paraSubir = tabla.length && nivel <= tabla.length ? tabla[nivel - 1].paraSubir : null;
        for (const e of enemigos) {
          const aEl = B.danioFisico(s.ataque, e.defensa || 0, p);
          const deEl = B.danioFisico(e.ataque || 0, s.defensa, p);
          fila.contra.push({
            enemigo: e.id,
            danioAlEnemigo: aEl, danioAlHeroe: deEl,
            golpesParaVencerlo: B.golpesPara(e.vida, aEl.medio),
            golpesParaCaer: B.golpesPara(s.vida, deEl.medio),
            combatesParaSubir: paraSubir !== null && e.experiencia > 0 ? Math.ceil(paraSubir / e.experiencia) : null,
          });
        }
        salida.push(fila);
      }
    }
    return salida;
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = B;
  else raiz.Balance = B;
})(typeof window !== 'undefined' ? window : globalThis);
