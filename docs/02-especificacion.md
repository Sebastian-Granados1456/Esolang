# Especificación del lenguaje Cubun

Este documento crece con cada fase. Por ahora define el **nivel léxico** (fase 1). La gramática se agrega en la fase 2.

## 1. Generalidades

- **Nombre:** Cubun. Viene de *muysccubun*, la lengua muisca: *muysca* = «gente», *cubun* = «lengua, palabra».
- **Paradigma:** imperativo, con variables, condicionales, bucles y funciones.
- **Extensión de archivo:** `.cbn`
- **Codificación:** UTF-8.
- **Implementación:** Java 21 con Maven.

## 2. Tipos de token

| Tipo | Qué es | Ejemplos |
|---|---|---|
| `PALABRA_CLAVE` | Frase del vocabulario, de una o varias palabras | `Ponga que`, `Suelte el chisme`, `Listo el pollo` |
| `IDENTIFICADOR` | Nombre de variable o de función | `plata`, `año`, `precio_final` |
| `NUMERO` | Entero o decimal | `42`, `3.14` |
| `TEXTO` | Cadena entre comillas dobles | `"hola"`, `"qué más"` |
| `BOOLEANO` | Verdadero o falso | `de una`, `nokas` |
| `OPERADOR` | Símbolo u operador en palabras | `+`, `más`, `>=`, `mayor o igual que` |
| `PUNTUACION` | Fin de frase, coma, dos puntos, paréntesis | `.`, `,`, `:`, `(` |
| `FIN` | Fin del archivo | — |

Cada token guarda su **tipo**, su **valor** (el nombre canónico, el número o el texto sin comillas), su **lexema** (lo que se escribió tal cual), la **línea** y la **columna**.

Los **comentarios** van desde `#` hasta el final de la línea y el lexer los ignora: `# pa' mí: esto es la plata`.

## 3. Reglas léxicas

### 3.1 Palabras clave de varias palabras

- No importan las mayúsculas ni las tildes: `Pídale a` = `PIDALE A` = `pidale a`.
- Entre las palabras de una frase puede haber espacios o tabulaciones, pero no saltos de línea.
- **Gana la frase más larga:** `si` → `SI`, pero `si no` → `SINO`.
- Si una frase queda incompleta, el lexer se devuelve: `dele` solo es un identificador, pero `dele mientras` es `MIENTRAS`.
- Varias frases pueden significar lo mismo. `Suelte el chisme`, `Cuente` y `Diga` producen el mismo token `IMPRIMIR`.

### 3.2 Identificadores

Empiezan con una letra (incluidas las tildes y la ñ) o con `_`, y siguen con letras, dígitos o `_`. Distinguen mayúsculas. Las palabras del vocabulario están reservadas.

### 3.3 Números

Son dígitos, con una parte decimal opcional (`3.14`). Un punto que no va seguido de un dígito es fin de frase: `vale 5.` → `5` y `.`. Un número pegado a letras es un error (`12lucas`).

### 3.4 Textos

Van entre comillas dobles `"…"` y no pueden ocupar varias líneas. No hay secuencias de escape, así que un texto no puede contener comillas dobles.

### 3.5 Muletillas

`parce`, `parcero`, `sumercé`, `veci`, `mijo`, `mija`, `pues`, `oiga`, `ve`, `vea`, `bueno`, `hermano`, `llave`, `socio`, `chino`, `china`.

El lexer las reconoce y las **descarta**, junto con las comas del vocativo: `¿Quiubo, parce?` produce solo `INICIO` y `FIN_FRASE`.

### 3.6 Puntuación

| Carácter | Token |
|---|---|
| `.` `?` `!` | `FIN_FRASE` |
| `,` | `COMA` |
| `:` | `DOS_PUNTOS` |
| `(` `)` | `PAREN_ABRE`, `PAREN_CIERRA` |
| `¿` `¡` | Se ignoran (solo adornan) |

## 4. Vocabulario

La lista completa vive en `src/main/java/cubun/lexer/Palabras.java`. Para agregar frases nuevas basta con añadirlas a la lista que corresponda.

### 4.1 Palabras clave

| Token | Frases |
|---|---|
| `INICIO` | quiubo, buenas, qué más |
| `FIN` | chao, nos pillamos, se cuida |
| `DECLARAR` | ponga que |
| `VALE` | vale |
| `IMPRIMIR` | suelte el chisme, cuente, diga |
| `LEER` | cuénteme, pregunte |
| `SI` | si, si es que |
| `SINO` | si no, o si no, de lo contrario |
| `ENTONCES` | entonces, hágale |
| `MIENTRAS` | mientras, dele mientras, dele que dele mientras |
| `FIN_BLOQUE` | listo, listo el pollo, ya estuvo |
| `FUNCION` | camello |
| `CON` | con |
| `RETORNAR` | devuelva, ahí le va |
| `LLAMAR` | pídale a, pídale el favor a |

### 4.2 Operadores

| Símbolo | En palabras |
|---|---|
| `+` `-` `*` `/` `%` | más, menos, por, entre, módulo |
| `==` `!=` | igual a / igualito a / tal cuál, distinto de / diferente de |
| `>` `<` `>=` `<=` | mayor que, menor que, mayor o igual que, menor o igual que |
| `y` `o` `no` | y, o, no |
| `=` | (solo símbolo) |

### 4.3 Booleanos

| Valor | Frases |
|---|---|
| verdadero | de una, obvio |
| falso | ni por el putas, nokas |

## 5. Errores léxicos

Todos empiezan con `¡Qué boleta! Línea L, columna C:`.

| Situación | Mensaje |
|---|---|
| Carácter que no pertenece al lenguaje | `¿Y eso qué es? No le entiendo el '@'.` |
| Texto sin cerrar | `Dejó el chisme a medias: le faltó cerrar las comillas.` |
| Número pegado a letras | `'12lucas' no es ni plata ni nombre.` |
