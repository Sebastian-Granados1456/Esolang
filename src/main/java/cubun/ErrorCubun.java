package cubun;

// Base de todos los errores del lenguaje: siempre con línea y columna.
public class ErrorCubun extends RuntimeException {

    private final int linea;
    private final int columna;

    public ErrorCubun(String mensaje, int linea, int columna) {
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
