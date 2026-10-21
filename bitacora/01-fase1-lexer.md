# Bitácora 1 — Introducción y Fase 1: Lexer

**Fecha:** 21 de octubre de 2026

## Introducción

### Qué es Rolo

Rolo es un lenguaje de programación esotérico cuyas palabras clave son modismos bogotanos y colombianos. Un programa se lee como una conversación entre parceros: empieza con *¿Quiubo, parce?*, imprime con *Suelte el chisme*, cierra los bloques con *Listo el pollo* y termina con *Chao pues*.

Como recurso de apoyo para el diseño se tiene en cuenta *Pensamiento y lenguaje* de Vygotsky (Obras escogidas, Tomo II). Sirve para pensar algunas decisiones sobre cómo se relacionan las palabras con lo que significan (ver `docs/01-fundamentacion-vygotsky.md`).

### Primer borrador y plan por fases

El primer borrador era un lenguaje de pilas muy pequeño (estilo Brainfuck), donde se partía el texto en palabras y cada modismo era una operación. Se descartó porque no tenía variables, funciones ni bloques anidados, y quedaba corto para un proyecto largo. Se decidió construir Rolo como un intérprete real, por fases:

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
| Afirmar / negar | de una, obvio / ni de fundas, nones | `BOOLEANO` |
| Vocativos y relleno | parce, sumercé, veci, mijo, pues… | se descartan |

Quedaron por fuera, para fases siguientes: *lucas*, *vaca*, *vuelto*, *chimba*, *Transmi* y *ni un peso*. *Qué boleta* se usa en los mensajes de error.

Todo el vocabulario vive en `Palabras.java`. Para agregar una frase solo hay que añadirla a la lista que corresponda.

### 2. Tipos de token

| Tipo | Ejemplos |
|---|---|
| `PALABRA_CLAVE` | `Ponga que`, `Suelte el chisme`, `Listo el pollo` |
| `IDENTIFICADOR` | `plata`, `año`, `precio_final` |
| `NUMERO` | `42`, `3.14` |
| `TEXTO` | `"hola"`, `«qué más»` |
| `BOOLEANO` | `de una`, `ni de fundas` |
| `OPERADOR` | `+`, `más`, `>=`, `mayor o igual que` |
| `PUNTUACION` | `.`, `,`, `:`, `(` |
| `COMENTARIO` | `# pa' mí: esto es la plata` |
| `FIN` | fin del archivo |

Cada token guarda su tipo, su valor (el nombre canónico, el número o el texto ya procesado), su lexema (lo que se escribió tal cual), la línea y la columna.

### 3. Decisiones de diseño

| Decisión | Razón |
|---|---|
| Implementación en Java con Maven | Lenguaje del curso; Maven organiza el proyecto y corre las pruebas |
| Lectura carácter por carácter, sin `split()` | Si se parte por espacios, *Suelte el chisme* queda en tres palabras que solas no significan nada. Además así se sabe la línea y la columna exactas |
| Árbol de palabras (trie) para las frases | Reconoce frases de varias palabras y se queda con la más larga (`si no` antes que `si`) |
| Vocabulario separado en `Palabras.java` | Agregar frases nuevas no obliga a tocar el lexer |
| Varias frases → un mismo token | Hay muchas formas de decir lo mismo |
| Cada token guarda valor y lexema | El programa usa el valor, pero no se pierde cómo se dijo |
| No distinguir mayúsculas ni tildes en las palabras clave | Se escribe como se habla |
| Muletillas descartadas junto con sus comas | `¿Quiubo, parce?` y `quiubo` dan lo mismo; se puede escribir completo o abreviado |
| Comentarios `#` guardados como tokens | Se podrán mostrar en el modo debug *voz alta* de la fase 6 |
| Textos con `"…"` y con `«…»` | Las comillas latinas son las del español |
| Errores dichos en rolo, con línea y columna | «¡Qué boleta! Línea 2, columna 20: ¿Y eso qué es? No le entiendo el '@'.» |

### 4. Relación con *Pensamiento y lenguaje*

Algunas de estas decisiones coinciden con ideas de la lectura:

| Idea de Vygotsky | Página | Dónde aparece en el lexer |
|---|---|---|
| Analizar por unidades y no por elementos: el agua separada en hidrógeno y oxígeno pierde sus propiedades | 5 | Las frases se reconocen completas en vez de partirse en palabras sueltas |
| El significado de la palabra es una generalización | 7 | Varias frases dan un mismo token |
| Sentido y significado | 197 | El token guarda el valor (significado) y el lexema (lo que se dijo) |
| El habla interna es predicativa: se omite el sujeto | 193 | Se descartan las muletillas y los vocativos |
| Formación de conceptos: cúmulos → complejos → conceptos | 75–82 | Lluvia de modismos → grupos → tokens con regla formal |
| Habla egocéntrica: lenguaje para uno mismo que acompaña la acción | 59 | Los comentarios se guardan en vez de botarse |

### 5. Pruebas

En `LexerTest.java` hay 25 pruebas unitarias (JUnit 5), y todas pasan. Cubren:

- Cada tipo de token (números, textos, identificadores con tildes y ñ, booleanos, operadores, puntuación, comentarios).
- Frases de varias palabras, la regla de la frase más larga y el caso en que una frase queda incompleta.
- Que no importen las mayúsculas ni las tildes.
- Equivalencia entre operadores en palabras y en símbolos (`más` = `+`).
- Que se descarten las muletillas con sus comas.
- Que la línea y la columna sean correctas.
- Los cuatro errores léxicos.

Un error encontrado durante las pruebas: una prueba usaba `y` como nombre de variable, pero `y` es el operador lógico. Era un error de la prueba, no del lexer, y se dejó así a propósito: las palabras del vocabulario están reservadas.

### 6. Productos de la fase

- `pom.xml`: proyecto Maven (Java 21, JUnit 5).
- `TipoToken.java` y `Token.java`: tipos de token y token (con valor, lexema, línea y columna).
- `Palabras.java`: vocabulario.
- `Lexer.java`: analizador léxico.
- `ErrorLexico.java`: errores con línea y columna.
- `Main.java`: imprime la tabla de tokens de un archivo `.rolo`.
- `LexerTest.java`: pruebas.
- `ejemplos/vocabulario.rolo`: programa de muestra con casi todo el vocabulario.
- `docs/02-especificacion.md`: nivel léxico del lenguaje.

### 7. Evidencias

Guardar en `bitacora/evidencias/`:

- [ ] `f1-01-palabras.png`: `Palabras.java` con el vocabulario
- [ ] `f1-02-lexer.png`: el código del lexer (la búsqueda de la frase más larga)
- [ ] `f1-03-tokens.png`: la tabla de tokens de `vocabulario.rolo`
- [ ] `f1-04-error.png`: un error léxico con línea y columna
- [ ] `f1-05-pruebas.png`: las 25 pruebas pasando
- [ ] `f1-06-repo.png`: el repositorio con el commit de la fase 1

### 8. Siguiente fase

Fase 2: definir la gramática formal (EBNF) y construir el parser que convierte los tokens en un AST.
