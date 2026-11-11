package cubun.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cubun.lexer.Lexer;
import cubun.lexer.Palabras;
import cubun.lexer.TipoToken;
import cubun.lexer.Token;
import cubun.parser.Expresion.Binaria;
import cubun.parser.Expresion.Booleano;
import cubun.parser.Expresion.ElMan;
import cubun.parser.Expresion.Numero;
import cubun.parser.Expresion.Texto;
import cubun.parser.Expresion.Unaria;
import cubun.parser.Expresion.Variable;
import cubun.parser.Sentencia.Asignacion;
import cubun.parser.Sentencia.Imprimir;
import cubun.parser.Sentencia.Leer;
import cubun.parser.Sentencia.Mientras;
import cubun.parser.Sentencia.Si;

public class Parser {

    private final List<Token> tokens = new ArrayList<>();
    private final Set<Integer> frasesCorteses = new HashSet<>();
    private int frases = 0;
    private int pos = 0;

    public Parser(List<Token> entrada) {
        List<String> cortesia = Palabras.CORTESIA.stream().map(Lexer::normalizar).toList();
        for (Token t : entrada) {
            if (t.tipo() == TipoToken.MULETILLA) {
                if (cortesia.contains(t.valor())) {
                    frasesCorteses.add(frases);
                }
                continue;
            }
            tokens.add(t);
            if (t.tipo() == TipoToken.PUNTUACION && t.valor().equals("FIN_FRASE")) {
                frases++;
            }
        }
    }

    public static Programa parsear(List<Token> tokens) {
        return new Parser(tokens).programa();
    }

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

    private void finDeFrase() {
        if (!es(TipoToken.PUNTUACION, "FIN_FRASE")) {
            Token anterior = tokens.get(pos - 1);
            throw new ErrorSintaxis("Le faltó el punto («.») al final de la frase.",
                    anterior.linea(), anterior.columna() + anterior.lexema().length());
        }
        avanzar();
    }

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
        Token despedida = avanzar();
        finDeFrase();
        if (!esFinArchivo()) {
            throw error("el final del archivo después de la despedida");
        }
        revisarCortesia(despedida);
        return new Programa(sentencias);
    }

    // Como el PLEASE de INTERCAL: entre 1 de cada 5 y 1 de cada 2 frases deben pedir el favor.
    private void revisarCortesia(Token despedida) {
        int corteses = frasesCorteses.size();
        int minimo = (frases + 4) / 5;
        int maximo = frases / 2;
        if (corteses < minimo) {
            String cuantas = corteses == 0 ? "ninguna pide" : "solo " + corteses + (corteses == 1 ? " pide" : " piden");
            throw new ErrorSintaxis("¡Qué confianzudo! De " + frases + " frases, " + cuantas
                    + " el favor. Use «sumercé» o «por favor» en al menos " + minimo + ".",
                    despedida.linea(), despedida.columna());
        }
        if (corteses > maximo) {
            throw new ErrorSintaxis("¡Qué lambón! " + corteses + " de " + frases
                    + " frases piden el favor. Tanta cortesía da desconfianza (máximo " + maximo + ").",
                    despedida.linea(), despedida.columna());
        }
    }

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

    private Sentencia asignacion() {
        Token inicio = avanzar();
        String nombre = identificador();
        esperarClave("VALE");
        Expresion valor = expresion();
        finDeFrase();
        return new Asignacion(nombre, valor, inicio.linea());
    }

    private Sentencia imprimir() {
        Token inicio = avanzar();
        Expresion valor = expresion();
        finDeFrase();
        return new Imprimir(valor, inicio.linea());
    }

    private Sentencia leer() {
        Token inicio = avanzar();
        String nombre = identificador();
        finDeFrase();
        return new Leer(nombre, inicio.linea());
    }

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

    private Expresion expresion() {
        return o();
    }

    private Expresion o() {
        Expresion izq = y();
        while (esOperador("o")) {
            Token op = avanzar();
            izq = new Binaria("o", izq, y(), op.linea());
        }
        return izq;
    }

    private Expresion y() {
        Expresion izq = negacion();
        while (esOperador("y")) {
            Token op = avanzar();
            izq = new Binaria("y", izq, negacion(), op.linea());
        }
        return izq;
    }

    private Expresion negacion() {
        if (esOperador("no")) {
            Token op = avanzar();
            return new Unaria("no", negacion(), op.linea());
        }
        return comparacion();
    }

    private Expresion comparacion() {
        Expresion izq = suma();
        if (esOperador("==", "!=", "<", ">", "<=", ">=")) {
            Token op = avanzar();
            return new Binaria((String) op.valor(), izq, suma(), op.linea());
        }
        return izq;
    }

    private Expresion suma() {
        Expresion izq = producto();
        while (esOperador("+", "-")) {
            Token op = avanzar();
            izq = new Binaria((String) op.valor(), izq, producto(), op.linea());
        }
        return izq;
    }

    private Expresion producto() {
        Expresion izq = unario();
        while (esOperador("*", "/", "%")) {
            Token op = avanzar();
            izq = new Binaria((String) op.valor(), izq, unario(), op.linea());
        }
        return izq;
    }

    private Expresion unario() {
        if (esOperador("-")) {
            Token op = avanzar();
            return new Unaria("-", unario(), op.linea());
        }
        return primario();
    }

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
                if (esClave("EL_MAN")) {
                    avanzar();
                    return new ElMan(t.linea());
                }
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
