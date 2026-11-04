package cubun.parser;

// Nodos del AST que producen un valor.
public sealed interface Expresion {

    record Numero(Object valor) implements Expresion {
    }

    record Texto(String valor) implements Expresion {
    }

    record Booleano(boolean valor) implements Expresion {
    }

    record Variable(String nombre, int linea) implements Expresion {
    }

    // a + b, a > b, a y b...
    record Binaria(String operador, Expresion izquierda, Expresion derecha, int linea) implements Expresion {
    }

    // -a, no a
    record Unaria(String operador, Expresion operando, int linea) implements Expresion {
    }
}
