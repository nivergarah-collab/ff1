'use strict';
// Utilidad común de DOM para las interfaces del editor: crea un elemento con atributos e hijos.
// `texto` fija el texto, `clase` la clase, `onXxx` registra un evento; el resto son atributos.
(function (raiz) {
  raiz.Dom = {
    el: function (etiqueta, atributos, ...hijos) {
      const nodo = document.createElement(etiqueta);
      for (const [k, v] of Object.entries(atributos || {})) {
        if (k === 'texto') nodo.textContent = v;
        else if (k === 'clase') nodo.className = v;
        else if (k.startsWith('on')) nodo.addEventListener(k.slice(2), v);
        else nodo.setAttribute(k, v);
      }
      for (const h of hijos) nodo.append(h);
      return nodo;
    },
  };
})(typeof window !== 'undefined' ? window : globalThis);
