package cubun.interprete;

import cubun.ErrorCubun;

public class ErrorEjecucion extends ErrorCubun {

    public ErrorEjecucion(String mensaje, int linea) {
        super(mensaje, linea);
    }
}
