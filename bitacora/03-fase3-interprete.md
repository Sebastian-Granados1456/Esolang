# Bitácora 3 — Fase 3: Intérprete

**Fecha:** 11 de noviembre de 2026

**Objetivo:** construir el intérprete, que recorre el árbol (AST) de la fase 2 y lo ejecuta. Con esta fase Cubun deja de solo revisar programas y empieza a correrlos.

## 1. Qué hace el intérprete

Hasta la fase 2, el programa terminaba en el árbol. Ahora el intérprete lo recorre y hace lo que dice cada nodo:

```
archivo .cbn → Lexer → tokens → Parser → árbol → Intérprete → resultado
```

Con `ejemplos/almuerzo.cbn`:

```
Alcanza para estos días:
3
Toca hacer una vaca
```

`Main` ahora ejecuta el programa por defecto. Las etapas anteriores se siguen viendo con `--tokens` (la tabla de tokens) y `--arbol` (el árbol).

## 2. Cómo funciona

El intérprete visita cada nodo y, según su tipo, hace algo:

| Nodo | Qué hace |
|---|---|
| `Asignacion` | Calcula el valor y lo guarda en la memoria con su nombre |
| `Imprimir` | Calcula el valor y lo muestra en una línea nueva |
| `Leer` | Lee lo que escribe la persona y lo guarda |
| `Si` | Evalúa la condición y ejecuta el bloque que corresponde |
| `Mientras` | Repite el cuerpo mientras la condición sea `de una` |
| `Binaria` / `Unaria` | Calcula `a + b`, `a > b`, `no a`… |
| `Variable` | Busca el valor guardado con ese nombre |

La memoria es un mapa de nombre a valor (`plata → 50000`). Por ahora hay una sola memoria para todo el programa; los ámbitos llegan con las funciones en la fase 4.

## 3. Valores y operaciones

Hay tres tipos de valor: **números**, **textos** y **booleanos** (`de una` / `nokas`).

| Ejemplo | Resultado |
|---|---|
| `7 / 2` | `3.5` |
| `6 / 2` | `3` (sin `.0`) |
| `0.1 + 0.2` | `0.3` |
| `"Total: " más 2 * 1.5` | `Total: 3` |
| `3 == 3.0` | `de una` |
| `"a" < "b" y de una` | `de una` |

## 4. Decisiones de diseño

| Decisión | Razón |
|---|---|
| Recorrer el árbol directamente (*tree-walking*) | Es la forma más simple de ejecutar un AST: un caso por cada tipo de nodo |
| `switch` sobre los nodos `sealed` | El compilador avisa si falta un tipo de nodo por manejar |
| `Valores.java` aparte del intérprete | Las reglas de los valores (sumar, comparar, imprimir) quedan en un solo lugar |
| `/` puede dar decimal | Como en una calculadora: `7 / 2` es `3.5`, no `3` |
| Los enteros se muestran sin `.0` y los decimales se redondean al mostrarlos | Que `0.1 + 0.2` se vea como `0.3` |
| `más` con un texto junta en vez de sumar | Permite armar frases: `"Quiubo, " más nombre` |
| Las condiciones solo aceptan booleanos | `Si 5:` no tiene sentido; mejor un error claro que un resultado raro |
| `y` / `o` no evalúan lo que sobra | `nokas y (1 / 0 == 1)` da `nokas` sin dividir entre cero |
| `Cuénteme` decide el tipo según lo que se escriba | Si se escribe `15`, se puede comparar como número sin convertirlo |
| Errores de ejecución con línea, en bogotano | Igual que los errores léxicos y de sintaxis; heredan de `ErrorCubun` |

## 5. Errores de ejecución

Son distintos a los de sintaxis: el programa está bien escrito, pero algo falla al correrlo.

```
¡Qué boleta! Línea 2: ¿Y quién es «plata»? Nadie la ha puesto.
¡Qué boleta! Línea 2: ¡Nadie puso pa' la vaca! No se puede dividir entre cero.
¡Qué boleta! Línea 2: No se puede hacer «-» entre un texto ("hola") y un número (1).
¡Qué boleta! Línea 2: La condición del «si» tiene que dar de una o nokas, pero dio un número (5).
```

Llevan solo la línea y no la columna, porque los nodos del árbol guardan la línea donde empieza cada instrucción u operación.

## 6. Muletillas con significado

Hasta aquí las muletillas (*parce*, *sumercé*, *veci*…) eran puro adorno: el lexer las botaba. En esta fase se decidió que algunas sí signifiquen algo. Ahora el lexer las entrega como tokens `MULETILLA` y el parser las aparta antes de revisar la gramática, así que pueden ir en cualquier parte de una frase sin romper nada.

### 6.1 Cortesía obligatoria

Es un homenaje a INTERCAL, el primer esolang (1972), que exige escribir *PLEASE* en algunas instrucciones y rechaza los programas maleducados y también los demasiado educados. En Cubun:

- Las muletillas de cortesía son *sumercé*, *su merced* y *por favor*.
- Al menos 1 de cada 5 frases tiene que llevar una. Si no: *«¡Qué confianzudo! De 13 frases, ninguna pide el favor. Use «sumercé» o «por favor» en al menos 3.»*
- Como máximo la mitad de las frases. Si se pasa: *«¡Qué lambón! 4 de 4 frases piden el favor. Tanta cortesía da desconfianza (máximo 2).»*

Los ejemplos `almuerzo.cbn` y `cedula.cbn` se actualizaron para cumplir la regla.

### 6.2 «El man»

`el man` (también *la man*, *ese man*, *esa man*) es el último valor del que se habló, como la tecla *Ans* de una calculadora:

```
Ponga que pasaje vale 3200.
Diga "Diez pasajes le salen en " más el man por 10 más " pesos.".
```

En el habla pasa lo mismo: en *«¿y el man qué dijo?»* el contexto dice quién es el man. Viene del inglés *man* («hombre») y en Colombia se volvió una palabra propia: *el man*, *la man*, *los manes*.

Un detalle encontrado al probar: después de un `Diga`, el man es todo lo que se imprimió (un texto), no el número que había adentro. Por eso en el ejemplo se usa justo después del `Ponga que`.

## 7. Pruebas

Las pruebas unitarias siguen pendientes para el final. Se verificó a mano con:

- `ejemplos/almuerzo.cbn`: ciclo, condicional con *si no*, operaciones y precedencia.
- `ejemplos/cedula.cbn`: un programa interactivo que pregunta nombre y edad. Se probó con dos edades (15 y 22) para recorrer las dos ramas del *si*.
- Programas pequeños para cada operación (división, decimales, textos, booleanos), para un `si` dentro de un `mientras` y para cada error de ejecución.
- Programas con poca, suficiente y demasiada cortesía, y con `el man` antes y después de calcular algo.

## 8. Relación con *Pensamiento y lenguaje*

| Idea de Vygotsky | Página | Dónde aparece en el intérprete |
|---|---|---|
| El pensamiento no se expresa en la palabra, sino que se realiza en ella | 174 | Hasta la fase 2 el programa solo tenía forma; con el intérprete se *realiza*: las palabras se vuelven acción |
| El sentido de la palabra es dinámico y cambia con el contexto | 197 | Una variable como `plata` siempre se llama igual, pero su valor cambia mientras corre el programa |
| El lenguaje es ante todo un medio de comunicación social | 7 | Cuando algo falla, el programa le habla a la persona en bogotano; y el programa tiene que ser cortés, como en una conversación |
| El habla interna es predicativa: se omite el sujeto porque el contexto lo da | 193 | `el man` no dice de qué valor se habla: el contexto (lo último que se calculó) lo dice |

## 9. Productos de la fase

- `interprete/Interprete.java`: recorre el árbol y lo ejecuta.
- `interprete/Valores.java`: operaciones, comparaciones y forma de imprimir los valores.
- `interprete/ErrorEjecucion.java`: errores de ejecución.
- `ErrorCubun.java`: ahora también acepta errores con solo la línea.
- `Main.java`: ejecuta el programa; `--tokens` y `--arbol` muestran las etapas anteriores.
- `ejemplos/cedula.cbn`: programa interactivo de ejemplo.
- `Palabras.java`, `Lexer.java`, `Parser.java`: muletillas como tokens, regla de cortesía y `el man`.
- `docs/02-especificacion.md`: valores, operaciones y errores de ejecución (secciones 9 y 10).

## 10. Evidencias

Guardar en `bitacora/evidencias/`:

- [ ] `f3-01-interprete.png`: el código del intérprete (el `switch` sobre las sentencias)
- [ ] `f3-02-almuerzo.png`: `almuerzo.cbn` ejecutándose
- [ ] `f3-03-cedula.png`: `cedula.cbn` ejecutándose, con los datos escritos por teclado
- [ ] `f3-04-errores.png`: dos o tres errores de ejecución
- [ ] `f3-05-cortesia.png`: el error de *¡Qué confianzudo!* o *¡Qué lambón!*
- [ ] `f3-06-repo.png`: el repositorio con el commit de la fase 3

## 11. Siguiente fase

Fase 4: las funciones (*camellos*), con parámetros, retorno y ámbitos de variables propios.
