# Bitácora 2 — Fase 2: Parser y AST

**Fecha:** 4 de noviembre de 2026

**Objetivo:** definir la gramática formal de Cubun y construir el parser, que revisa que los tokens estén bien armados y los organiza en un árbol de sintaxis abstracta (AST).

## 1. Antes de empezar: cambios desde la fase 1

- **Nombre nuevo:** el lenguaje pasó de llamarse *Rolo* a **Cubun** (de *muysccubun*, la lengua muisca). La extensión ahora es `.cbn` y el paquete Java es `cubun`.
- **Vocabulario:** se agregó *por si las moscas* como otra forma de `SI`.

## 2. Qué hace el parser

El lexer entrega una lista plana de tokens. El parser revisa que estén en un orden válido y construye un árbol que muestra qué va dentro de qué:

```
Si plata mayor que 15000 hágale:          SI plata > 15000 ENTONCES : IMPRIMIR "Alcanza" . FIN_BLOQUE .
    Diga "Alcanza".                 →
Listo.                                    Si
                                          ├── condición
                                          │   └── Operación: >
                                          │       ├── Variable: plata
                                          │       └── Número: 15000
                                          └── entonces
                                              └── Imprimir
                                                  └── Texto: "Alcanza"
```

## 3. La gramática

Se escribió en EBNF en `docs/02-especificacion.md` (sección 6). Cubre:

| Construcción | Forma |
|---|---|
| Programa | `Quiubo.` … `Chao.` |
| Asignación | `Ponga que x vale 5.` |
| Imprimir | `Diga x.` |
| Leer | `Cuénteme x.` |
| Condicional | `Si … hágale:` … `O si no:` … `Listo.` |
| Ciclo | `Dele mientras …:` … `Ya estuvo.` |
| Expresiones | números, textos, booleanos, variables, `+ - * / %`, comparaciones, `y o no`, paréntesis |

La gramática se escribe sobre los **tokens** y no sobre las frases. Por eso se pueden seguir agregando frases a `Palabras.java` sin tocar el parser: *por si las moscas* funcionó como `SI` sin cambiar nada.

## 4. Decisiones de diseño

| Decisión | Razón |
|---|---|
| Parser descendente recursivo | Es la técnica más clara a mano: un método por cada regla de la gramática |
| Precedencia con un nivel por regla (`o` → `y` → `no` → comparación → `+ -` → `* / %` → unario) | Así `12000 + 500 * 2` queda como `12000 + (500 * 2)` sin reglas especiales |
| Toda frase termina en punto, también el saludo y el cierre de bloques | Una sola regla, fácil de recordar; en español las oraciones terminan en punto |
| Bloques con `:` y `listo` | Se lee natural (*Si…: … Listo el pollo.*) y no depende de la sangría |
| `hágale` / `entonces` opcional | Se puede escribir *Si x > 5:* o *Si x > 5 hágale:* |
| Una sola asignación (`Ponga que`) para crear y para cambiar una variable | Menos palabras clave; los ámbitos se deciden en la fase 4 |
| Funciones fuera de esta fase | Siguen el plan: los *camellos* llegan en la fase 4 con los ámbitos |
| Nodos del AST como `record` de Java en interfaces `sealed` | Poco código, y el compilador avisa si falta un caso al recorrer el árbol |
| `ErrorCubun` como base de `ErrorLexico` y `ErrorSintaxis` | `Main` atrapa todos los errores en un solo lugar; sirve también para la fase 5 |
| `Main` muestra el árbol; `--tokens` muestra la tabla de tokens | Se puede ver cualquiera de las dos etapas |

## 5. Errores de sintaxis

Todos llevan línea y columna y se dicen en bogotano. Por ejemplo:

```
¡Qué boleta! Línea 1, columna 1: ¡Qué maleducado! El programa tiene que empezar saludando («quiubo»).
¡Qué boleta! Línea 2, columna 7: Le faltó el punto («.») al final de la frase.
¡Qué boleta! Línea 4, columna 1: Se esperaba «listo» para cerrar el «Si» de la línea 2 pero llegó «Chao».
¡Qué boleta! Línea 2, columna 1: «Listo» no cierra nada: no hay ningún «si» ni «mientras» abierto.
```

Dos ajustes durante la fase:

- **El punto faltante** se marcaba en la línea siguiente (donde estaba la palabra que llegó). Se cambió para que se marque justo donde termina la frase que no tiene punto.
- **Un `listo` suelto** daba un mensaje confuso. Ahora dice que no hay ningún bloque abierto.

## 6. Pruebas

Las pruebas unitarias siguen pendientes para el final. Se verificó a mano con `ejemplos/almuerzo.cbn`, que usa asignaciones, un ciclo, un condicional con *si no*, precedencia y paréntesis, y con programas con errores a propósito: sin saludo, sin despedida, sin punto, bloque sin cerrar, `listo` suelto, falta de `:`, paréntesis sin cerrar, valor faltante y algo después de la despedida.

`ejemplos/vocabulario.cbn` ya no pasa el parser, porque usa un *camello* (fase 4). Sigue sirviendo para ver los tokens con `--tokens`.

## 7. Relación con *Pensamiento y lenguaje*

| Idea de Vygotsky | Página | Dónde aparece en el parser |
|---|---|---|
| El pensamiento es un todo, pero el habla lo despliega en forma sucesiva: «una densa nube que descarga una lluvia de palabras» | 202 | El parser hace el camino inverso: recibe la «lluvia» de tokens uno tras otro y reconstruye la estructura completa (el árbol) |
| El habla interna es predicativa: queda lo esencial | 193 | El AST es *abstracto*: se quedan afuera los puntos, los `:`, el `hágale` y las muletillas; solo queda lo que importa para ejecutar |
| El significado de la palabra es una generalización | 7 | La gramática usa los tokens (el significado), no las frases; cualquier sinónimo sirve |
| El pensamiento no se expresa en la palabra, sino que se realiza en ella | 174 | El árbol muestra la estructura del programa, pero todavía no hace nada: se realiza en la fase 3, con el intérprete |

## 8. Productos de la fase

- `parser/Parser.java`: analizador sintáctico.
- `parser/Sentencia.java`, `parser/Expresion.java`, `parser/Programa.java`: nodos del AST.
- `parser/ImpresorAst.java`: dibuja el árbol.
- `parser/ErrorSintaxis.java` y `ErrorCubun.java`: errores.
- `Main.java`: muestra el AST, o los tokens con `--tokens`.
- `ejemplos/almuerzo.cbn`: programa de ejemplo.
- `docs/02-especificacion.md`: gramática, nodos del AST y errores de sintaxis.

## 9. Evidencias

Guardar en `bitacora/evidencias/`:

- [ ] `f2-01-gramatica.png`: la gramática EBNF en la especificación
- [ ] `f2-02-parser.png`: el código del parser (por ejemplo, el método `si()` o la cadena de precedencia)
- [ ] `f2-03-arbol.png`: el árbol de `almuerzo.cbn`
- [ ] `f2-04-errores.png`: dos o tres errores de sintaxis con línea y columna
- [ ] `f2-05-repo.png`: el repositorio con el commit de la fase 2

## 10. Siguiente fase

Fase 3: el intérprete, que recorre el árbol y lo ejecuta (variables, `diga`, `cuénteme`, `si`, `mientras`).
