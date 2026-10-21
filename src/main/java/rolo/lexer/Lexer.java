package rolo.lexer;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Lexer {

    private enum Clase { CLAVE, OPERADOR, BOOLEANO, MULETILLA }

    private record Significado(Clase clase, Object valor) {
    }

    // Árbol de palabras: cada nodo sabe qué palabras pueden seguir y si ahí termina una frase.
    private static class Nodo {
        final Map<String, Nodo> hijos = new HashMap<>();
        Significado fin;
    }

    private static final Map<Character, Character> CIERRE_TEXTO = Map.of('"', '"', '«', '»');
    private static final Map<Character, Character> ESCAPES = Map.of(
            'n', '\n', 't', '\t', '\\', '\\', '"', '"', '»', '»');
    private static final Nodo TRIE = construirTrie();

    private final String codigo;
    private final List<Token> tokens = new ArrayList<>();
    private int pos = 0;
    private int linea = 1;
    private int columna = 1;

    public Lexer(String codigo) {
        this.codigo = codigo;
    }

    public static List<Token> tokenizar(String codigo) {
        return new Lexer(codigo).tokenizar();
    }

    public static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return sinTildes.replaceAll("\\p{M}", "");
    }

    private static Nodo construirTrie() {
        Nodo raiz = new Nodo();
        Palabras.PALABRAS_CLAVE.forEach((nombre, frases) ->
                frases.forEach(f -> insertar(raiz, f, new Significado(Clase.CLAVE, nombre))));
        Palabras.OPERADORES_VERBALES.forEach((simbolo, frases) ->
                frases.forEach(f -> insertar(raiz, f, new Significado(Clase.OPERADOR, simbolo))));
        Palabras.BOOLEANOS.forEach((valor, frases) ->
                frases.forEach(f -> insertar(raiz, f, new Significado(Clase.BOOLEANO, valor))));
        Palabras.MULETILLAS.forEach(f -> insertar(raiz, f, new Significado(Clase.MULETILLA, null)));
        return raiz;
    }

    private static void insertar(Nodo raiz, String frase, Significado significado) {
        Nodo nodo = raiz;
        for (String palabra : normalizar(frase).split("\\s+")) {
            nodo = nodo.hijos.computeIfAbsent(palabra, p -> new Nodo());
        }
        if (nodo.fin != null) {
            throw new IllegalStateException("La frase '" + frase + "' está repetida en Palabras.java");
        }
        nodo.fin = significado;
    }

    private static boolean esInicioPalabra(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean esPartePalabra(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    // '\0' indica que ya no hay más código.
    private char actual(int k) {
        int i = pos + k;
        return i < codigo.length() ? codigo.charAt(i) : '\0';
    }

    private char actual() {
        return actual(0);
    }

    private boolean hayMas() {
        return pos < codigo.length();
    }

    private char avanzar() {
        char c = codigo.charAt(pos++);
        if (c == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
        return c;
    }

    private void agregar(TipoToken tipo, Object valor, int inicio, int lineaInicio, int columnaInicio) {
        tokens.add(new Token(tipo, valor, codigo.substring(inicio, pos), lineaInicio, columnaInicio));
    }

    public List<Token> tokenizar() {
        while (hayMas()) {
            char c = actual();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                avanzar();
            } else if (c == '#') {
                leerComentario();
            } else if (Palabras.APERTURAS.indexOf(c) >= 0) {
                avanzar();
            } else if (CIERRE_TEXTO.containsKey(c)) {
                leerTexto();
            } else if (esDigito(c)) {
                leerNumero();
            } else if (esInicioPalabra(c)) {
                leerPalabra();
            } else if (!leerSimbolo()) {
                throw new ErrorLexico("¿Y eso qué es? No le entiendo el '" + c + "'.", linea, columna);
            }
        }
        tokens.add(new Token(TipoToken.FIN, null, "", linea, columna));
        return tokens;
    }

    private void leerComentario() {
        int inicio = pos, l = linea, col = columna;
        while (hayMas() && actual() != '\n') {
            avanzar();
        }
        String texto = codigo.substring(inicio + 1, pos).strip();
        agregar(TipoToken.COMENTARIO, texto, inicio, l, col);
    }

    private void leerTexto() {
        int inicio = pos, l = linea, col = columna;
        char cierre = CIERRE_TEXTO.get(avanzar());
        StringBuilder texto = new StringBuilder();
        while (true) {
            if (!hayMas() || actual() == '\n') {
                throw new ErrorLexico("Dejó el chisme a medias: le faltó cerrar las comillas.", l, col);
            }
            char c = actual();
            if (c == cierre) {
                avanzar();
                break;
            }
            if (c == '\\') {
                int lEscape = linea, colEscape = columna;
                avanzar();
                Character escape = hayMas() ? ESCAPES.get(actual()) : null;
                if (escape == null) {
                    String visto = hayMas() ? String.valueOf(actual()) : "";
                    throw new ErrorLexico("No conozco el escape '\\" + visto + "'.", lEscape, colEscape);
                }
                texto.append(escape);
                avanzar();
            } else {
                texto.append(avanzar());
            }
        }
        agregar(TipoToken.TEXTO, texto.toString(), inicio, l, col);
    }

    private void leerNumero() {
        int inicio = pos, l = linea, col = columna;
        while (esDigito(actual())) {
            avanzar();
        }
        boolean esDecimal = actual() == '.' && esDigito(actual(1));
        if (esDecimal) {
            avanzar();
            while (esDigito(actual())) {
                avanzar();
            }
        }
        if (hayMas() && esInicioPalabra(actual())) {
            while (hayMas() && esPartePalabra(actual())) {
                avanzar();
            }
            String lexema = codigo.substring(inicio, pos);
            throw new ErrorLexico("'" + lexema + "' no es ni plata ni nombre.", l, col);
        }
        String lexema = codigo.substring(inicio, pos);
        Object valor = esDecimal ? (Object) Double.parseDouble(lexema) : (Object) Long.parseLong(lexema);
        agregar(TipoToken.NUMERO, valor, inicio, l, col);
    }

    private void leerPalabra() {
        int inicio = pos, l = linea, col = columna;
        while (hayMas() && esPartePalabra(actual())) {
            avanzar();
        }
        Nodo nodo = TRIE.hijos.get(normalizar(codigo.substring(inicio, pos)));
        if (nodo == null) {
            agregar(TipoToken.IDENTIFICADOR, codigo.substring(inicio, pos), inicio, l, col);
            return;
        }

        // Busca la frase más larga que empiece con esta palabra (sin cambiar de línea).
        Significado mejor = nodo.fin;
        int finMejor = pos;
        int p = pos;
        while (true) {
            int q = p;
            while (q < codigo.length() && (codigo.charAt(q) == ' ' || codigo.charAt(q) == '\t')) {
                q++;
            }
            if (q >= codigo.length() || !esInicioPalabra(codigo.charAt(q))) {
                break;
            }
            int r = q;
            while (r < codigo.length() && esPartePalabra(codigo.charAt(r))) {
                r++;
            }
            nodo = nodo.hijos.get(normalizar(codigo.substring(q, r)));
            if (nodo == null) {
                break;
            }
            p = r;
            if (nodo.fin != null) {
                mejor = nodo.fin;
                finMejor = p;
            }
        }

        if (mejor == null) {
            agregar(TipoToken.IDENTIFICADOR, codigo.substring(inicio, pos), inicio, l, col);
            return;
        }

        columna += finMejor - pos;
        pos = finMejor;
        switch (mejor.clase()) {
            case CLAVE -> agregar(TipoToken.PALABRA_CLAVE, mejor.valor(), inicio, l, col);
            case OPERADOR -> agregar(TipoToken.OPERADOR, mejor.valor(), inicio, l, col);
            case BOOLEANO -> agregar(TipoToken.BOOLEANO, mejor.valor(), inicio, l, col);
            case MULETILLA -> descartarMuletilla();
        }
    }

    // Las comas del vocativo ("Quiubo, parce, ...") se van con la muletilla.
    private void descartarMuletilla() {
        if (!tokens.isEmpty()) {
            Token ultimo = tokens.get(tokens.size() - 1);
            if (ultimo.tipo() == TipoToken.PUNTUACION && "COMA".equals(ultimo.valor())) {
                tokens.remove(tokens.size() - 1);
            }
        }
        while (actual() == ' ' || actual() == '\t') {
            avanzar();
        }
        if (actual() == ',') {
            avanzar();
        }
    }

    private boolean leerSimbolo() {
        int inicio = pos, l = linea, col = columna;
        for (String simbolo : Palabras.OPERADORES_SIMBOLOS) {
            if (codigo.startsWith(simbolo, pos)) {
                for (int i = 0; i < simbolo.length(); i++) {
                    avanzar();
                }
                agregar(TipoToken.OPERADOR, simbolo, inicio, l, col);
                return true;
            }
        }
        String puntuacion = Palabras.PUNTUACION.get(actual());
        if (puntuacion != null) {
            avanzar();
            agregar(TipoToken.PUNTUACION, puntuacion, inicio, l, col);
            return true;
        }
        return false;
    }
}
