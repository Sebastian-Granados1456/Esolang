package cubun.parser;

import java.util.List;

// Raíz del AST.
public record Programa(List<Sentencia> sentencias) {
}
