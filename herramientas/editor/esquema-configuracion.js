// Esquema de la configuración que ve el editor. Una prueba Java (EsquemaEditorTest) lo compara con el del motor.
window.ESQUEMA_CONFIGURACION = {
  "parametros": [
    {
      "nombre": "combate.velocidadBarra",
      "grupo": "Combate",
      "etiqueta": "Velocidad de la barra",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 100,
      "defecto": 10,
      "descripcion": "Rapidez con que se llena la barra de tiempo de cada combatiente. 10 es el ritmo normal; más alto, combates más ágiles."
    },
    {
      "nombre": "combate.cargaLlena",
      "grupo": "Combate",
      "etiqueta": "Carga de la barra llena",
      "tipo": "entero",
      "minimo": 100,
      "maximo": 100000,
      "defecto": 1000,
      "descripcion": "Carga que debe reunir la barra para que un combatiente actúe. Más alta, esperas más largas entre turnos."
    },
    {
      "nombre": "combate.avanceRapido",
      "grupo": "Combate",
      "etiqueta": "Multiplicador del avance rápido",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 8,
      "defecto": 2,
      "descripcion": "Cuántas veces más rápido corre el combate con el interruptor de avance rápido encendido."
    },
    {
      "nombre": "combate.ticksPorPaso",
      "grupo": "Combate",
      "etiqueta": "Ticks por paso",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 100,
      "defecto": 1,
      "descripcion": "Ticks que avanza el combate en cada paso de animación. Más alto, el combate transcurre más deprisa."
    },
    {
      "nombre": "combate.fuerzaFisica",
      "grupo": "Combate",
      "etiqueta": "Fuerza del daño físico",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 100,
      "defecto": 10,
      "descripcion": "Multiplicador del daño físico, en décimas (10 = ×1, 20 = ×2)."
    },
    {
      "nombre": "combate.fuerzaMagica",
      "grupo": "Combate",
      "etiqueta": "Fuerza del daño mágico",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 100,
      "defecto": 10,
      "descripcion": "Multiplicador de la magia (daño y curación), en décimas (10 = ×1, 20 = ×2)."
    },
    {
      "nombre": "combate.varianza",
      "grupo": "Combate",
      "etiqueta": "Variación del daño",
      "tipo": "entero",
      "minimo": 0,
      "maximo": 50,
      "defecto": 10,
      "descripcion": "Variación al azar del daño y de la curación, en por ciento (más o menos). 0 hace los golpes siempre iguales."
    },
    {
      "nombre": "combate.huidaBase",
      "grupo": "Combate",
      "etiqueta": "Probabilidad base de huida",
      "tipo": "entero",
      "minimo": 0,
      "maximo": 100,
      "defecto": 50,
      "descripcion": "Probabilidad base de huir, en por ciento. Se ajusta con la velocidad del grupo frente a la de los enemigos (siempre entre 5 y 95 %)."
    },
    {
      "nombre": "combate.venenoPorCiento",
      "grupo": "Combate",
      "etiqueta": "Daño del veneno",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 50,
      "defecto": 8,
      "descripcion": "Por ciento de la vida máxima que quita el veneno al terminar cada turno."
    },
    {
      "nombre": "combate.proteccionPorCiento",
      "grupo": "Combate",
      "etiqueta": "Daño con protección",
      "tipo": "entero",
      "minimo": 0,
      "maximo": 100,
      "defecto": 50,
      "descripcion": "Por ciento del daño que llega a un combatiente protegido. 50 reduce el daño a la mitad."
    },
    {
      "nombre": "inventario.maximoPorObjeto",
      "grupo": "Inventario",
      "etiqueta": "Máximo por objeto",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 999,
      "defecto": 99,
      "descripcion": "Cuántas unidades de un mismo objeto puede llevar el grupo."
    },
    {
      "nombre": "mundo.pasosMinimos",
      "grupo": "Exploración",
      "etiqueta": "Pasos mínimos entre encuentros",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 999,
      "defecto": 15,
      "descripcion": "Pasos que da el grupo por una zona con enemigos antes de que pueda saltar un combate."
    },
    {
      "nombre": "mundo.pasosMaximos",
      "grupo": "Exploración",
      "etiqueta": "Pasos máximos entre encuentros",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 999,
      "defecto": 30,
      "descripcion": "Pasos tras los cuales el combate salta seguro. Si es menor que el mínimo, se usa el mínimo."
    },
    {
      "nombre": "juego.msMensaje",
      "grupo": "Interfaz",
      "etiqueta": "Duración de los mensajes",
      "tipo": "entero",
      "minimo": 100,
      "maximo": 5000,
      "defecto": 900,
      "descripcion": "Milisegundos que se ve cada mensaje del combate."
    },
    {
      "nombre": "juego.rapidoAlEmpezar",
      "grupo": "Interfaz",
      "etiqueta": "Avance rápido al empezar",
      "tipo": "entero",
      "minimo": 0,
      "maximo": 1,
      "defecto": 0,
      "descripcion": "1 = el avance rápido empieza encendido en una partida nueva; 0 = apagado."
    },
    {
      "nombre": "juego.largoNombre",
      "grupo": "Interfaz",
      "etiqueta": "Largo del nombre de un héroe",
      "tipo": "entero",
      "minimo": 1,
      "maximo": 12,
      "defecto": 8,
      "descripcion": "Letras como máximo al nombrar a un héroe en la partida nueva."
    },
    {
      "nombre": "pueblo.ventaPorCiento",
      "grupo": "Pueblo",
      "etiqueta": "Precio de venta",
      "tipo": "entero",
      "minimo": 0,
      "maximo": 100,
      "defecto": 50,
      "descripcion": "Por ciento del precio que paga la tienda por un objeto del grupo."
    }
  ]
};
