package cubun.lexer;

import cubun.ErrorCubun;

public class ErrorLexico extends ErrorCubun {

    public ErrorLexico(String mensaje, int linea, int columna) {
        super(mensaje, linea, columna);
    }
}
