package cubun.parser;

import java.util.ArrayList;
import java.util.List;

import cubun.lexer.Palabras;
import cubun.lexer.TipoToken;
import cubun.lexer.Token;
import cubun.parser.Expresion.Binaria;
import cubun.parser.Expresion.Booleano;
import cubun.parser.Expresion.Numero;
import cubun.parser.Expresion.Texto;
import cubun.parser.Expresion.Unaria;
import cubun.parser.Expresion.Variable;
import cubun.parser.Sentencia.Asignacion;
import cubun.parser.Sentencia.Imprimir;
import cubun.parser.Sentencia.Leer;
import cubun.parser.Sentencia.Mientras;
import cubun.parser.Sentencia.Si;

// Parser descendente recursivo: un método por cada regla de la gramática (ver docs/02-especificacion.md).
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static Programa parsear(List<Token> tokens) {
        return new Parser(tokens).programa();
    }

    // ---------- Herramientas ----------

    private Token actual() {
        return tokens.get(pos);
    }

    private Token avanzar() {
        Token t = actual();
        if (t.tipo() != TipoToken.FIN) {
            pos++;
        }
        return t;
    }

    private boolean es(TipoToken tipo, Object valor) {
        return actual().tipo() == tipo && valor.equals(actual().valor());
    }

    private boolean esClave(String nombre) {
        return es(TipoToken.PALABRA_CLAVE, nombre);
    }

    private boolean esOperador(String... simbolos) {
        for (String s : simbolos) {
            if (es(TipoToken.OPERADOR, s)) {
                return true;
            }
        }
        return false;
    }

    private boolean esFinArchivo() {
        return actual().tipo() == TipoToken.FIN;
    }

    // Cómo se le muestra una palabra clave al usuario: su primera frase en Palabras.java.
    private static String frase(String nombre) {
        return "«" + Palabras.PALABRAS_CLAVE.get(nombre).get(0) + "»";
    }

    private ErrorSintaxis error(String esperado) {
        Token t = actual();
        String llego = esFinArchivo() ? "el final del archivo" : "«" + t.lexema() + "»";
        return new ErrorSintaxis("Se esperaba " + esperado + " pero llegó " + llego + ".", t.linea(), t.columna());
    }

    private Token esperarClave(String nombre) {
        if (!esClave(nombre)) {
            throw error(frase(nombre));
        }
        return avanzar();
    }

    private void esperarPuntuacion(String nombre, String simbolo) {
        if (!es(TipoToken.PUNTUACION, nombre)) {
            throw error("«" + simbolo + "»");
        }
        avanzar();
    }

    // Toda frase termina en "." "?" o "!". El error se marca justo donde terminó la frase anterior.
    private void finDeFrase() {
        if (!es(TipoToken.PUNTUACION, "FIN_FRASE")) {
            Token anterior = tokens.get(pos - 1);
            throw new ErrorSintaxis("Le faltó el punto («.») al final de la frase.",
                    anterior.linea(), anterior.columna() + anterior.lexema().length());
        }
        avanzar();
    }

    // ---------- Programa y sentencias ----------

    // programa = INICIO fin { sentencia } FIN fin
    private Programa programa() {
        if (!esClave("INICIO")) {
            Token t = actual();
            throw new ErrorSintaxis("¡Qué maleducado! El programa tiene que empezar saludando (" + frase("INICIO") + ").",
                    t.linea(), t.columna());
        }
        avanzar();
        finDeFrase();

        List<Sentencia> sentencias = bloque();

        if (esFinArchivo()) {
            throw error(frase("FIN") + " (¿se va a ir sin despedirse?)");
        }
        if (!esClave("FIN")) {
            Token t = actual();
            throw new ErrorSintaxis("«" + t.lexema() + "» no cierra nada: no hay ningún " + frase("SI") + " ni "
                    + frase("MIENTRAS") + " abierto.", t.linea(), t.columna());
        }
        avanzar();
        finDeFrase();
        if (!esFinArchivo()) {
            throw error("el final del archivo después de la despedida");
        }
        return new Programa(sentencias);
    }

    // Lee sentencias hasta encontrar algo que cierre el bloque.
    private List<Sentencia> bloque() {
        List<Sentencia> sentencias = new ArrayList<>();
        while (!esClave("FIN_BLOQUE") && !esClave("SINO") && !esClave("FIN") && !esFinArchivo()) {
            sentencias.add(sentencia());
        }
        return sentencias;
    }

    private Sentencia sentencia() {
        if (esClave("DECLARAR")) {
            return asignacion();
        }
        if (esClave("IMPRIMIR")) {
            return imprimir();
        }
        if (esClave("LEER")) {
            return leer();
        }
        if (esClave("SI")) {
            return si();
        }
        if (esClave("MIENTRAS")) {
            return mientras();
        }
        throw error("una instrucción (" + frase("DECLARAR") + ", " + frase("IMPRIMIR") + ", " + frase("SI") + ", "
                + frase("MIENTRAS") + "...)");
    }

    // asignacion = DECLARAR IDENTIFICADOR VALE expresion fin
    private Sentencia asignacion() {
        Token inicio = avanzar();
        String nombre = identificador();
        esperarClave("VALE");
        Expresion valor = expresion();
        finDeFrase();
        return new Asignacion(nombre, valor, inicio.linea());
    }

    // imprimir = IMPRIMIR expresion fin
    private Sentencia imprimir() {
        Token inicio = avanzar();
        Expresion valor = expresion();
        finDeFrase();
        return new Imprimir(valor, inicio.linea());
    }

    // leer = LEER IDENTIFICADOR fin
    private Sentencia leer() {
        Token inicio = avanzar();
        String nombre = identificador();
        finDeFrase();
        return new Leer(nombre, inicio.linea());
    }

    // si = SI expresion [ENTONCES] ":" { sentencia } [ SINO ":" { sentencia } ] FIN_BLOQUE fin
    private Sentencia si() {
        Token inicio = avanzar();
        Expresion condicion = expresion();
        aperturaDeBloque();
        List<Sentencia> entonces = bloque();
        List<Sentencia> sino = List.of();
        if (esClave("SINO")) {
            avanzar();
            esperarPuntuacion("DOS_PUNTOS", ":");
            sino = bloque();
        }
        cerrarBloque(inicio);
        return new Si(condicion, entonces, sino, inicio.linea());
    }

    // mientras = MIENTRAS expresion [ENTONCES] ":" { sentencia } FIN_BLOQUE fin
    private Sentencia mientras() {
        Token inicio = avanzar();
        Expresion condicion = expresion();
        aperturaDeBloque();
        List<Sentencia> cuerpo = bloque();
        cerrarBloque(inicio);
        return new Mientras(condicion, cuerpo, inicio.linea());
    }

    private void aperturaDeBloque() {
        if (esClave("ENTONCES")) {
            avanzar();
        }
        esperarPuntuacion("DOS_PUNTOS", ":");
    }

    private void cerrarBloque(Token apertura) {
        if (!esClave("FIN_BLOQUE")) {
            throw error(frase("FIN_BLOQUE") + " para cerrar el «" + apertura.lexema() + "» de la línea " + apertura.linea());
        }
        avanzar();
        finDeFrase();
    }

    private String identificador() {
        if (actual().tipo() != TipoToken.IDENTIFICADOR) {
            throw error("un nombre");
        }
        return avanzar().lexema();
    }

    // ---------- Expresiones (de menor a mayor precedencia) ----------

    private Expresion expresion() {
        return o();
    }

    // o = y { "o" y }
    private Expresion o() {
        Expresion izq = y();
        while (esOperador("o")) {
            Token op = avanzar();
            izq = new Binaria("o", izq, y(), op.linea());
        }
        return izq;
    }

    // y = negacion { "y" negacion }
    private Expresion y() {
        Expresion izq = negacion();
        while (esOperador("y")) {
            Token op = avanzar();
            izq = new Binaria("y", izq, negacion(), op.linea());
        }
        return izq;
    }

    // negacion = "no" negacion | comparacion
    private Expresion negacion() {
        if (esOperador("no")) {
            Token op = avanzar();
            return new Unaria("no", negacion(), op.linea());
        }
        return comparacion();
    }

    // comparacion = suma [ ("==" | "!=" | "<" | ">" | "<=" | ">=") suma ]
    private Expresion comparacion() {
        Expresion izq = suma();
        if (esOperador("==", "!=", "<", ">", "<=", ">=")) {
            Token op = avanzar();
            return new Binaria((String) op.valor(), izq, suma(), op.linea());
        }
        return izq;
    }

    // suma = producto { ("+" | "-") producto }
    private Expresion suma() {
        Expresion izq = producto();
        while (esOperador("+", "-")) {
            Token op = avanzar();
            izq = new Binaria((String) op.valor(), izq, producto(), op.linea());
        }
        return izq;
    }

    // producto = unario { ("*" | "/" | "%") unario }
    private Expresion producto() {
        Expresion izq = unario();
        while (esOperador("*", "/", "%")) {
            Token op = avanzar();
            izq = new Binaria((String) op.valor(), izq, unario(), op.linea());
        }
        return izq;
    }

    // unario = "-" unario | primario
    private Expresion unario() {
        if (esOperador("-")) {
            Token op = avanzar();
            return new Unaria("-", unario(), op.linea());
        }
        return primario();
    }

    // primario = NUMERO | TEXTO | BOOLEANO | IDENTIFICADOR | "(" expresion ")"
    private Expresion primario() {
        Token t = actual();
        switch (t.tipo()) {
            case NUMERO -> {
                avanzar();
                return new Numero(t.valor());
            }
            case TEXTO -> {
                avanzar();
                return new Texto((String) t.valor());
            }
            case BOOLEANO -> {
                avanzar();
                return new Booleano((Boolean) t.valor());
            }
            case IDENTIFICADOR -> {
                avanzar();
                return new Variable(t.lexema(), t.linea());
            }
            default -> {
                if (es(TipoToken.PUNTUACION, "PAREN_ABRE")) {
                    avanzar();
                    Expresion dentro = expresion();
                    esperarPuntuacion("PAREN_CIERRA", ")");
                    return dentro;
                }
                throw error("un valor (número, texto, nombre...)");
            }
        }
    }
}
