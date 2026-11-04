package cubun.parser;

import java.util.List;

public sealed interface Sentencia {

    record Asignacion(String nombre, Expresion valor, int linea) implements Sentencia {
    }

    record Imprimir(Expresion valor, int linea) implements Sentencia {
    }

    record Leer(String nombre, int linea) implements Sentencia {
    }

    record Si(Expresion condicion, List<Sentencia> entonces, List<Sentencia> sino, int linea) implements Sentencia {
    }

    record Mientras(Expresion condicion, List<Sentencia> cuerpo, int linea) implements Sentencia {
    }
}
