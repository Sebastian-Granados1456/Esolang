# Especificación del lenguaje Cubun

Este documento crece con cada fase. Define el **nivel léxico** (fase 1, secciones 2–5) y la **gramática** (fase 2, secciones 6–8).

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
| `SI` | si, si es que, por si las moscas |
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

## 6. Gramática (EBNF)

La gramática trabaja sobre los tokens, no sobre el texto. Por eso cualquier frase del vocabulario sirve: `DECLARAR` es *ponga que* y `IMPRIMIR` es *suelte el chisme*, *cuente* o *diga*.

```ebnf
programa     = INICIO fin { sentencia } FIN fin ;
fin          = FIN_FRASE ;                                  (* "." "?" o "!" *)

sentencia    = asignacion | imprimir | leer | si | mientras ;
asignacion   = DECLARAR IDENTIFICADOR VALE expresion fin ;
imprimir     = IMPRIMIR expresion fin ;
leer         = LEER IDENTIFICADOR fin ;
si           = SI expresion [ ENTONCES ] ":" { sentencia }
               [ SINO ":" { sentencia } ]
               FIN_BLOQUE fin ;
mientras     = MIENTRAS expresion [ ENTONCES ] ":" { sentencia } FIN_BLOQUE fin ;

expresion    = o ;
o            = y { "o" y } ;
y            = negacion { "y" negacion } ;
negacion     = "no" negacion | comparacion ;
comparacion  = suma [ ( "==" | "!=" | "<" | ">" | "<=" | ">=" ) suma ] ;
suma         = producto { ( "+" | "-" ) producto } ;
producto     = unario { ( "*" | "/" | "%" ) unario } ;
unario       = "-" unario | primario ;
primario     = NUMERO | TEXTO | BOOLEANO | IDENTIFICADOR | "(" expresion ")" ;
```

Reglas que se desprenden de la gramática:

- **Toda frase termina en punto** (o en `?` o `!`), incluidos el saludo, la despedida y el cierre de los bloques.
- **Los bloques se abren con `:`** (con `hágale` o `entonces` opcional antes) y **se cierran con `listo`**.
- **Precedencia**, de menor a mayor: `o`, `y`, `no`, comparaciones, `+ -`, `* / %` y el `-` de un número negativo. `12000 + 500 * 2` es `12000 + (500 * 2)`. Los paréntesis cambian el orden.
- **Las comparaciones no se encadenan:** `a < b < c` no es válido; se escribe `a < b y b < c`.
- **`Ponga que` sirve para crear una variable y también para cambiarle el valor.**
- **No hay `si no, si` (else if):** se anida un `si` dentro del `si no`.
- **Las funciones (`camello`, `pídale a`, `devuelva`) todavía no hacen parte de la gramática.** Se agregan en la fase 4.

## 7. Árbol de sintaxis (AST)

El parser convierte los tokens en un árbol con estos nodos:

| Nodo | Representa | Hijos |
|---|---|---|
| `Programa` | El programa completo | lista de sentencias |
| `Asignacion` | `Ponga que x vale …` | nombre, expresión |
| `Imprimir` | `Diga …` | expresión |
| `Leer` | `Cuénteme x` | nombre |
| `Si` | `Si … : … o si no: … listo` | condición, sentencias del *entonces*, sentencias del *si no* |
| `Mientras` | `Mientras … : … listo` | condición, sentencias del cuerpo |
| `Numero`, `Texto`, `Booleano` | Valores literales | — |
| `Variable` | Un nombre usado como valor | nombre |
| `Binaria` | `a + b`, `a > b`, `a y b`… | operador, izquierda, derecha |
| `Unaria` | `-a`, `no a` | operador, operando |

Ejemplo:

```
Si plata mayor que 15000 hágale:
    Diga "Alcanza".
Listo.
```

```
Si
├── condición
│   └── Operación: >
│       ├── Variable: plata
│       └── Número: 15000
└── entonces
    └── Imprimir
        └── Texto: "Alcanza"
```

## 8. Errores de sintaxis

| Situación | Mensaje |
|---|---|
| No empieza con el saludo | `¡Qué maleducado! El programa tiene que empezar saludando («quiubo»).` |
| No termina con la despedida | `Se esperaba «chao» (¿se va a ir sin despedirse?) pero llegó el final del archivo.` |
| Falta el punto | `Le faltó el punto («.») al final de la frase.` |
| Un bloque sin cerrar | `Se esperaba «listo» para cerrar el «Si» de la línea 2 pero llegó «Chao».` |
| Un `listo` o `si no` que no cierra nada | `«Listo» no cierra nada: no hay ningún «si» ni «mientras» abierto.` |
| Falta una pieza de la frase | `Se esperaba «vale» pero llegó «1».` |
| Falta un valor | `Se esperaba un valor (número, texto, nombre...) pero llegó «.».` |
| Algo después de la despedida | `Se esperaba el final del archivo después de la despedida pero llegó «Diga».` |

Todos llevan línea y columna, igual que los errores léxicos.
