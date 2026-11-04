package cubun.parser;

import java.util.List;

// Nodos del AST que hacen algo (instrucciones).
public sealed interface Sentencia {

    // Ponga que nombre vale valor.
    record Asignacion(String nombre, Expresion valor, int linea) implements Sentencia {
    }

    // Diga valor.
    record Imprimir(Expresion valor, int linea) implements Sentencia {
    }

    // Cuénteme nombre.
    record Leer(String nombre, int linea) implements Sentencia {
    }

    // Si condicion: entonces... o si no: sino... listo.
    record Si(Expresion condicion, List<Sentencia> entonces, List<Sentencia> sino, int linea) implements Sentencia {
    }

    // Mientras condicion: cuerpo... listo.
    record Mientras(Expresion condicion, List<Sentencia> cuerpo, int linea) implements Sentencia {
    }
}
