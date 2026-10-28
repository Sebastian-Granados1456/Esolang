package cubun.lexer;

// valor = significado (nombre canónico, número, texto...); lexema = lo que se escribió tal cual.
public record Token(TipoToken tipo, Object valor, String lexema, int linea, int columna) {

    @Override
    public String toString() {
        return "Token(" + tipo + ", " + valor + ", " + linea + ":" + columna + ")";
    }
}
