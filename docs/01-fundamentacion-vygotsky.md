# Fundamentación: Vygotsky y el diseño de Rolo

Lectura de apoyo: L. S. Vygotsky, *Pensamiento y lenguaje*, en *Obras escogidas*, Tomo II. Las páginas que se citan son las del PDF usado en clase.

## Idea central

Para Vygotsky el lenguaje no es un simple código para transmitir información. Es una **herramienta cultural** que media el pensamiento: el pensamiento se transforma al pasar por las palabras de una comunidad. Un lenguaje de programación también es un sistema de signos que media entre lo que uno piensa y lo que la máquina hace. Rolo lleva esa idea al extremo: programar es hablar en bogotano.

> «El pensamiento no se expresa en la palabra, sino que se realiza en ella.» (p. 174)

Un programa en Rolo es un pensamiento que se realiza en modismos. El lexer, el parser y el intérprete son las etapas en que esas palabras se convierten en acción.

## Conceptos de la lectura y cómo aparecen en el lenguaje

### 1. El lenguaje como medio de comunicación social

> «La función inicial del lenguaje es la comunicativa. El lenguaje es ante todo un medio de comunicación social…» (p. 7)

**En Rolo:** un programa tiene forma de conversación. Empieza con un saludo (`¿Quiubo, parce?`) y termina con una despedida (`Chao pues`). Las palabras clave son modismos que solo tienen sentido dentro de una comunidad concreta (Bogotá y Colombia): el signo es cultural, no universal.

### 2. El significado de la palabra como generalización

> «…el significado de la palabra es ante todo una generalización.» (p. 7)
> «…el significado de la palabra es la unidad de ambas funciones del lenguaje en la misma medida que es unidad del pensamiento.» (p. 8)

**En Rolo:** el lexer hace literalmente una generalización. Muchas expresiones concretas se agrupan en una sola categoría: `Suelte el chisme`, `Cuente` y `Diga` son el mismo token `IMPRIMIR`; `más` y `+` son el mismo operador. El token es el significado; las frases son las formas concretas en que se dice.

### 3. Sentido y significado

> «…el sentido de la palabra es siempre una formación dinámica, variable y compleja…» (p. 197)

Vygotsky, siguiendo a Paulhan, distingue el **significado** (estable, de diccionario) del **sentido** (lo que la palabra evoca en un contexto concreto).

**En Rolo:** cada token guarda las dos cosas. El **valor** es el significado (`IMPRIMIR`). El **lexema** es la forma concreta que eligió quien escribió (`Suelte el chisme` o `Diga`), con su tono y su sentido. La máquina usa el significado, pero el programa conserva el sentido.

### 4. El habla egocéntrica

> «El lenguaje egocéntrico es interno por su función, es un lenguaje para uno mismo, que se halla en el camino de pasar al interior…» (p. 59)

Contra Piaget, Vygotsky muestra que el niño que habla solo mientras juega no está hablando «para nada»: esa habla acompaña la acción, la organiza y la planifica (pp. 29–30).

**En Rolo:** los comentarios (`# pa' mí: …`) se llaman **habla egocéntrica**. El lexer no los bota: los guarda como tokens `COMENTARIO`. En la fase 6, el modo debug (*voz alta*) los dirá en pantalla en el momento en que la ejecución pase por ellos, acompañando la acción.

### 5. El habla interna: predicatividad

> «La forma predicativa es la única y fundamental del lenguaje interno…» (p. 193)

En el habla interna se omite el sujeto (uno ya sabe de qué habla) y queda solo el predicado.

**En Rolo:** las **muletillas** y los vocativos (`parce`, `sumercé`, `veci`…) son el «sujeto» y la cortesía del habla social. El lexer los reconoce y los descarta: `¿Quiubo, parce?` y `quiubo` producen lo mismo. Además, las frases tienen formas largas y cortas (`Suelte el chisme` / `Diga`). Al principio el programador escribe completo y, cuando domina el lenguaje, abrevia, como en el paso del habla externa a la interna.

### 6. La zona de desarrollo próximo (ZDP)

> «…el nivel que alcanza el niño al resolver las tareas, no por su cuenta, sino en colaboración, es lo que determina la zona de desarrollo próximo.» (p. 139)
> «…lo que el niño sabe hacer hoy en colaboración será capaz de realizar mañana por sí mismo.» (p. 148)

**En Rolo (fase 4):** las funciones se llaman **camellos** y se invocan pidiéndole el favor a alguien (`Pídale a`). Un programa resuelve una tarea grande repartiéndola: cada camello tiene su propio ámbito de variables y puede pedirle ayuda a otro, o a sí mismo (recursión). Lo que no se puede resolver de una sola vez se resuelve en colaboración.

### 7. La formación de conceptos como método de diseño

En el capítulo 5 Vygotsky describe cómo se forman los conceptos: primero se juntan objetos en **cúmulos** sin orden (p. 75), después en **complejos** unidos por asociaciones concretas (complejos en cadena, p. 80; pseudoconceptos, p. 82) y al final en **conceptos** abstractos.

**En el proceso de diseño:**

1. **Cúmulo:** una lista suelta de modismos, sin criterio.
2. **Complejos:** se agruparon por asociación concreta. *Listo el pollo* y *ya estuvo* cierran algo, así que cierran bloques; *suelte el chisme* es contar algo, así que imprime; *camello* es trabajo, así que es una función.
3. **Concepto:** cada grupo se convirtió en un tipo de token con una regla formal (`02-especificacion.md`).

## Tabla resumen

| Concepto de Vygotsky | Página | Elemento de Rolo | Fase |
|---|---|---|---|
| Lenguaje como medio social | 7 | El programa es un diálogo: saludo y despedida | 1–2 |
| Significado = generalización | 7–8 | Varias frases → un mismo token | 1 |
| Sentido vs. significado | 197 | Cada token guarda valor (significado) y lexema (sentido) | 1 |
| Habla egocéntrica | 29–30, 59 | Comentarios `#` guardados como tokens; modo voz alta | 1, 6 |
| Habla interna: predicatividad | 193 | Se descartan las muletillas; formas cortas de las frases | 1 |
| Zona de desarrollo próximo | 139, 148 | Funciones (*camellos*) que colaboran, con ámbitos propios | 4 |
| Formación de conceptos | 75–82 | Etapas del diseño: cúmulo → complejos → tokens | 1 |
| Pensamiento que se realiza en la palabra | 174 | Lexer → parser → intérprete | 1–3 |
