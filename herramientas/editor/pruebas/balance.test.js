'use strict';
// Pruebas de la vista de balance. La comparación con el motor real está en BalanceEditorTest (Java),
// que lee el mismo balance-casos.json que usa esta prueba.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const B = require('../balance.js');

const CONT = path.join(__dirname, '..', '..', '..', 'app', 'src', 'main', 'assets', 'contenido');
const leer = (n) => JSON.parse(fs.readFileSync(path.join(CONT, n), 'utf8'));

test('las fórmulas reproducen los casos de referencia que comprueba el motor', () => {
  const casos = JSON.parse(fs.readFileSync(path.join(__dirname, 'balance-casos.json'), 'utf8')).casos;
  assert.ok(casos.length >= 30);
  for (const c of casos) {
    const p = { fuerzaFisica: c.fuerzaFisica || 10, fuerzaMagica: c.fuerzaMagica || 10, varianza: c.varianza };
    const r = c.tipo === 'fisico' ? B.danioFisico(c.ataque, c.defensa, p)
      : c.tipo === 'magico' ? B.danioMagico(c.poderHabilidad, c.poderActor, c.defensa, p)
        : B.curacion(c.poderHabilidad, c.poderActor, p);
    assert.equal(r.min, c.min, JSON.stringify(c));
    assert.equal(r.max, c.max, JSON.stringify(c));
  }
});

test('daño físico: ejemplo del contrato y suelo de 1', () => {
  const p = { fuerzaFisica: 10, fuerzaMagica: 10, varianza: 0 };
  assert.deepEqual(B.danioFisico(12, 4, p), { min: 10, max: 10, medio: 10 });
  assert.equal(B.danioFisico(2, 40, p).medio, 1);
});

test('la varianza abre un rango simétrico y el medio queda cerca de la base', () => {
  const r = B.danioFisico(12, 0, { fuerzaFisica: 10, fuerzaMagica: 10, varianza: 20 });
  assert.equal(r.min, 9); // 12 × 80 / 100 = 9,6 → 9
  assert.equal(r.max, 14); // 12 × 120 / 100 = 14,4 → 14
  assert.ok(r.medio > 11 && r.medio < 12.5);
});

test('golpes para vencer redondea hacia arriba', () => {
  assert.equal(B.golpesPara(20, 7), 3);
  assert.equal(B.golpesPara(21, 7), 3);
  assert.equal(B.golpesPara(22, 7), 4);
  assert.equal(B.golpesPara(10, 0), Infinity);
});

test('enNivel suma el crecimiento por nivel y respeta los topes del motor', () => {
  const clase = { vida: 40, magia: 0, ataque: 6, defensa: 5, poder: 0, velocidad: 250 };
  const s = B.enNivel(clase, { vida: 7, ataque: 2, velocidad: 3 }, 4);
  assert.deepEqual(s, { vida: 61, magia: 0, ataque: 12, defensa: 5, poder: 0, velocidad: 255 });
  assert.equal(B.enNivel(clase, undefined, 9).vida, 40);
});

test('tabla de experiencia con el contenido del juego', () => {
  const t = B.tablaExperiencia(leer('progresion.json'));
  assert.equal(t.length, 15);
  assert.deepEqual(t[0], { nivel: 1, acumulada: 0, paraSubir: 10 });
  assert.equal(t[t.length - 1].paraSubir, null);
});

test('enfrentamientos con el contenido del juego: todos los héroes contra todos los enemigos', () => {
  const docs = { combatientes: leer('combatientes.json'), progresion: leer('progresion.json') };
  const filas = B.enfrentamientos(docs, { 'combate.varianza': 0 }, [1, 5]);
  const heroes = docs.combatientes.lista.filter((c) => c.bando === 'heroe').length;
  const enemigos = docs.combatientes.lista.filter((c) => c.bando === 'enemigo').length;
  assert.equal(filas.length, heroes * 2);
  for (const f of filas) {
    assert.equal(f.contra.length, enemigos);
    for (const c of f.contra) {
      assert.ok(c.golpesParaVencerlo >= 1 && Number.isFinite(c.golpesParaVencerlo));
      assert.ok(c.golpesParaCaer >= 1);
    }
  }
  const nivel5 = filas.find((f) => f.heroe === 'guardian' && f.nivel === 5);
  assert.equal(nivel5.estadisticas.vida, docs.combatientes.lista.find((c) => c.id === 'guardian').vida + 7 * 4);
});

test('cambiar la fuerza física cambia el daño y los golpes para vencer', () => {
  const docs = { combatientes: leer('combatientes.json'), progresion: leer('progresion.json') };
  const a = B.enfrentamientos(docs, { 'combate.varianza': 0, 'combate.fuerzaFisica': 10 }, [1])[0].contra[0];
  const b = B.enfrentamientos(docs, { 'combate.varianza': 0, 'combate.fuerzaFisica': 30 }, [1])[0].contra[0];
  assert.ok(b.danioAlEnemigo.medio > a.danioAlEnemigo.medio);
  assert.ok(b.golpesParaVencerlo <= a.golpesParaVencerlo);
});
