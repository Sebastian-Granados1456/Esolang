# Cubun — un esolang bogotano

**Cubun** es un lenguaje de programación esotérico (esolang) cuyas palabras clave son modismos bogotanos y colombianos. Un programa se lee como una conversación entre parceros: empieza con un *¿Quiubo, parce?*, declara variables con *Ponga que… vale…*, imprime con *Suelte el chisme*, cierra los bloques con *Listo el pollo* y termina con un *Chao pues*.

El nombre viene de *muysccubun*, la lengua de los muiscas, el pueblo que habitaba Bacatá (hoy Bogotá) antes de la Colonia: *muysca* es «gente» y *cubun* es «lengua, palabra». Un lenguaje de programación que se llama, literalmente, «lengua».

Como recurso de apoyo se tiene en cuenta *Pensamiento y lenguaje* de L. S. Vygotsky (Obras escogidas, Tomo II). Ver `docs/01-fundamentacion-vygotsky.md`.

## Estructura del repositorio

```
Esolang/
├── README.md
├── docs/
│   ├── 01-fundamentacion-vygotsky.md   # relación entre la lectura y el diseño
│   └── 02-especificacion.md            # definición del lenguaje (crece con cada fase)
├── pom.xml                             # proyecto Maven (Java 21)
├── src/main/java/cubun/
│   ├── Main.java                       # imprime la tabla de tokens de un .cbn
│   └── lexer/
│       ├── Palabras.java               # vocabulario: aquí se agregan frases nuevas
│       ├── TipoToken.java              # tipos de token
│       ├── Token.java
│       ├── ErrorLexico.java
│       └── Lexer.java                  # analizador léxico
├── ejemplos/                           # programas .cbn
└── bitacora/
    ├── 01-fase1-lexer.md               # introducción + fase 1
    └── evidencias/                     # pantallazos
```

## Fases del proyecto

| Fase | Contenido | Estado |
|------|-----------|--------|
| 1 | Lexer: reconoce los tokens carácter por carácter, con errores por línea y columna | ✅ Hecha |
| 2 | Parser con AST y gramática formal | Pendiente |
| 3 | Intérprete que recorre el AST (bloques anidados) | Pendiente |
| 4 | Funciones definidas por el usuario (*camellos*) y ámbitos | Pendiente |
| 5 | Manejo de errores propio del lenguaje | Pendiente |
| 6 | Extras (REPL, modo debug *voz alta*, listas…) | Pendiente |
| 7 | Pruebas unitarias (JUnit), cuando el vocabulario esté estable | Pendiente |

## Ejemplo

```
¿Quiubo, parce?
Ponga que plata vale 20000.
Si plata mayor o igual que 15000 hágale:
    Suelte el chisme "Alcanza pa'l almuerzo".
Listo el pollo.
Chao pues.
```
