package cubun.lexer;

public record Token(TipoToken tipo, Object valor, String lexema, int linea, int columna) {

    @Override
    public String toString() {
        return "Token(" + tipo + ", " + valor + ", " + linea + ":" + columna + ")";
    }
}
