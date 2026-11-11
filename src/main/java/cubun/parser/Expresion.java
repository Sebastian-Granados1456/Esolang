package cubun.parser;

public sealed interface Expresion {

    record Numero(Object valor) implements Expresion {
    }

    record Texto(String valor) implements Expresion {
    }

    record Booleano(boolean valor) implements Expresion {
    }

    record Variable(String nombre, int linea) implements Expresion {
    }

    record ElMan(int linea) implements Expresion {
    }

    record Binaria(String operador, Expresion izquierda, Expresion derecha, int linea) implements Expresion {
    }

    record Unaria(String operador, Expresion operando, int linea) implements Expresion {
    }
}
