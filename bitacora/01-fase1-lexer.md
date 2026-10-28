# Bitácora 1 — Introducción y Fase 1: Lexer

**Fecha:** 21 de octubre de 2026

## Introducción

### Qué es Cubun

Cubun es un lenguaje de programación esotérico cuyas palabras clave son modismos bogotanos y colombianos. Un programa se lee como una conversación entre parceros: empieza con *¿Quiubo, parce?*, imprime con *Suelte el chisme*, cierra los bloques con *Listo el pollo* y termina con *Chao pues*.

El nombre viene de *muysccubun*, la lengua de los muiscas, el pueblo que habitaba Bacatá (hoy Bogotá) antes de la Colonia: *muysca* es «gente» y *cubun* es «lengua, palabra». Un lenguaje de programación que se llama, literalmente, «lengua». Al principio el proyecto se llamaba *Rolo* (como se les dice a los bogotanos), pero se cambió por un nombre menos directo y con más cara de herramienta técnica.

Como recurso de apoyo para el diseño se tiene en cuenta *Pensamiento y lenguaje* de Vygotsky (Obras escogidas, Tomo II). Sirve para pensar algunas decisiones sobre cómo se relacionan las palabras con lo que significan (ver `docs/01-fundamentacion-vygotsky.md`).

### Primer borrador y plan por fases

El primer borrador era un lenguaje de pilas muy pequeño (estilo Brainfuck), donde se partía el texto en palabras y cada modismo era una operación. Se descartó porque no tenía variables, funciones ni bloques anidados, y quedaba corto para un proyecto largo. Se decidió construir Cubun como un intérprete real, por fases:

| Fase | Contenido |
|---|---|
| 1 | Lexer: tokens carácter por carácter, con errores por línea |
| 2 | Parser con AST y gramática formal |
| 3 | Intérprete que recorre el AST |
| 4 | Funciones definidas por el usuario y ámbitos |
| 5 | Manejo de errores propio del lenguaje |
| 6 | Extras (REPL, modo debug, listas…) |

### Lluvia de modismos

Lo primero fue apuntar modismos sin ningún orden:

> quiubo, parce, sumercé, lucas, chimba, vaca, vuelto, chisme, de una, hágale, dele que dele, ya estuvo, listo el pollo, Transmi, póngale, quítele, chao pues, cuénteme, qué boleta, veci, rolo, ni un peso, ¿cuánto le debo?, camello, ni de fundas, nos pillamos

## Fase 1: Lexer

**Objetivo:** construir un analizador léxico real, que lea el código carácter por carácter, reconozca tipos de token y reporte errores con línea y columna.

### 1. Vocabulario: de modismos a tokens

Los modismos se agruparon según lo que evocan en la vida diaria, y cada grupo se volvió un tipo de token. Varias frases pueden dar el mismo token:

| Grupo | Modismos | Token |
|---|---|---|
| Saludo / despedida | quiubo, buenas, qué más / chao, nos pillamos, se cuida | `INICIO` / `FIN` |
| Contar algo | suelte el chisme, cuente, diga | `IMPRIMIR` |
| Preguntar | cuénteme, pregunte | `LEER` |
| Terminar algo | listo, listo el pollo, ya estuvo | `FIN_BLOQUE` |
| Insistir | dele mientras, dele que dele mientras | `MIENTRAS` |
| Dar el visto bueno | hágale, entonces | `ENTONCES` |
| Trabajo | camello | `FUNCION` |
| Pedir un favor | pídale a, pídale el favor a | `LLAMAR` |
| Entregar | devuelva, ahí le va | `RETORNAR` |
| Afirmar / negar | de una, obvio / ni por el putas, nokas | `BOOLEANO` |
| Vocativos y relleno | parce, sumercé, veci, mijo, chino, pues… | se descartan |

Quedaron por fuera, para fases siguientes: *lucas*, *vaca*, *vuelto*, *chimba*, *Transmi* y *ni un peso*. *Qué boleta* se usa en los mensajes de error.

Todo el vocabulario vive en `Palabras.java`. Para agregar una frase solo hay que añadirla a la lista que corresponda.

### 2. Tipos de token

| Tipo | Ejemplos |
|---|---|
| `PALABRA_CLAVE` | `Ponga que`, `Suelte el chisme`, `Listo el pollo` |
| `IDENTIFICADOR` | `plata`, `año`, `precio_final` |
| `NUMERO` | `42`, `3.14` |
| `TEXTO` | `"hola"`, `"qué más"` |
| `BOOLEANO` | `de una`, `nokas` |
| `OPERADOR` | `+`, `más`, `>=`, `mayor o igual que` |
| `PUNTUACION` | `.`, `,`, `:`, `(` |
| `FIN` | fin del archivo |

Cada token guarda su tipo, su valor (el nombre canónico, el número o el texto sin comillas), su lexema (lo que se escribió tal cual), la línea y la columna. Los comentarios (`# …` hasta el final de la línea) se ignoran.

### 3. Decisiones de diseño

| Decisión | Razón |
|---|---|
| Implementación en Java con Maven | Lenguaje del curso; Maven organiza el proyecto y genera el `.jar` |
| Lectura carácter por carácter, sin `split()` | Si se parte por espacios, *Suelte el chisme* queda en tres palabras que solas no significan nada. Además así se sabe la línea y la columna exactas |
| Lista de frases ordenada de la más larga a la más corta | Reconoce frases de varias palabras y se queda con la más larga (`si no` antes que `si`) |
| Vocabulario separado en `Palabras.java` | Agregar frases nuevas no obliga a tocar el lexer |
| Varias frases → un mismo token | Hay muchas formas de decir lo mismo |
| Cada token guarda valor y lexema | El programa usa el valor, pero no se pierde cómo se dijo |
| No distinguir mayúsculas ni tildes en las palabras clave | Se escribe como se habla |
| Muletillas descartadas junto con sus comas | `¿Quiubo, parce?` y `quiubo` dan lo mismo; se puede escribir completo o abreviado |
| Comentarios `#` ignorados | Son para quien escribe, no para la máquina; así el parser no tiene que lidiar con ellos |
| Textos solo con `"…"` y sin escapes | Lo mínimo necesario; los saltos de línea al imprimir los decidirá el intérprete |
| Errores dichos en bogotano, con línea y columna | «¡Qué boleta! Línea 2, columna 20: ¿Y eso qué es? No le entiendo el '@'.» |

### 4. Simplificación

Después de la primera versión, el lexer se dejó lo más sencillo posible antes de pasar al parser, sin perder lo importante:

| Antes | Ahora | Por qué |
|---|---|---|
| Árbol de palabras (trie) con tres clases internas | Lista de frases ordenada por longitud | Hace lo mismo y se lee de corrido |
| Comentarios guardados como tokens | Comentarios ignorados | El parser no tiene que saltárselos |
| Textos con `"…"` y `«…»`, con escapes (`\n`, `\"`…) | Solo `"…"`, sin escapes | Menos casos y un error menos |

Durante la simplificación falló una prueba de booleanos. El lexer estaba bien: el vocabulario había cambiado (*ni de fundas* pasó a *ni por el putas* y *nokas*) y la prueba seguía con las frases viejas. Por eso se decidió dejar las pruebas para el final (sección 6).

### 5. Relación con *Pensamiento y lenguaje*

Algunas de estas decisiones coinciden con ideas de la lectura:

| Idea de Vygotsky | Página | Dónde aparece en el lexer |
|---|---|---|
| Analizar por unidades y no por elementos: el agua separada en hidrógeno y oxígeno pierde sus propiedades | 5 | Las frases se reconocen completas en vez de partirse en palabras sueltas |
| El significado de la palabra es una generalización | 7 | Varias frases dan un mismo token |
| Sentido y significado | 197 | El token guarda el valor (significado) y el lexema (lo que se dijo) |
| El habla interna es predicativa: se omite el sujeto | 193 | Se descartan las muletillas y los vocativos |
| Formación de conceptos: cúmulos → complejos → conceptos | 75–82 | Lluvia de modismos → grupos → tokens con regla formal |
| Habla egocéntrica: lenguaje para uno mismo que acompaña la acción | 59 | Los comentarios son habla para uno mismo: acompañan el código, pero la máquina los ignora |

### 6. Pruebas

Las pruebas unitarias se dejan para el final del proyecto. El vocabulario todavía está cambiando y cada cambio obligaba a corregir pruebas que no tenían ningún error. Por ahora el lexer se verifica a mano con `vocabulario.cbn` y con archivos que tienen errores a propósito.

### 7. Productos de la fase

- `pom.xml`: proyecto Maven (Java 21).
- `TipoToken.java` y `Token.java`: tipos de token y token (con valor, lexema, línea y columna).
- `Palabras.java`: vocabulario.
- `Lexer.java`: analizador léxico.
- `ErrorLexico.java`: errores con línea y columna.
- `Main.java`: imprime la tabla de tokens de un archivo `.cbn`.
- `ejemplos/vocabulario.cbn`: programa de muestra con casi todo el vocabulario.
- `docs/02-especificacion.md`: nivel léxico del lenguaje.

### 8. Evidencias

Guardar en `bitacora/evidencias/`:

- [ ] `f1-01-palabras.png`: `Palabras.java` con el vocabulario
- [ ] `f1-02-lexer.png`: el código del lexer (la búsqueda de la frase más larga)
- [ ] `f1-03-tokens.png`: la tabla de tokens de `vocabulario.cbn`
- [ ] `f1-04-error.png`: un error léxico con línea y columna
- [ ] `f1-05-repo.png`: el repositorio con el commit de la fase 1

### 9. Siguiente fase

Fase 2: definir la gramática formal (EBNF) y construir el parser que convierte los tokens en un AST.
