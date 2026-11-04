package cubun.parser;

import cubun.ErrorCubun;

public class ErrorSintaxis extends ErrorCubun {

    public ErrorSintaxis(String mensaje, int linea, int columna) {
        super(mensaje, linea, columna);
    }
}
