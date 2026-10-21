package rolo.lexer;

public class ErrorLexico extends RuntimeException {

    private final int linea;
    private final int columna;

    public ErrorLexico(String mensaje, int linea, int columna) {
        super("¡Qué boleta! Línea " + linea + ", columna " + columna + ": " + mensaje);
        this.linea = linea;
        this.columna = columna;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }
}
