package cubun.lexer;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Lexer {

    private record Frase(List<String> palabras, TipoToken tipo, Object valor) {
    }

    private static final List<Frase> FRASES = cargarFrases();
    private static final int MAX_PALABRAS = FRASES.get(0).palabras().size();

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

    private static List<Frase> cargarFrases() {
        List<Frase> frases = new ArrayList<>();
        Palabras.PALABRAS_CLAVE.forEach((nombre, lista) ->
                lista.forEach(f -> frases.add(frase(f, TipoToken.PALABRA_CLAVE, nombre))));
        Palabras.OPERADORES_VERBALES.forEach((simbolo, lista) ->
                lista.forEach(f -> frases.add(frase(f, TipoToken.OPERADOR, simbolo))));
        Palabras.BOOLEANOS.forEach((valor, lista) ->
                lista.forEach(f -> frases.add(frase(f, TipoToken.BOOLEANO, valor))));
        Palabras.MULETILLAS.forEach(f -> frases.add(frase(f, null, null)));

        Set<List<String>> vistas = new HashSet<>();
        for (Frase f : frases) {
            if (!vistas.add(f.palabras())) {
                throw new IllegalStateException("La frase '" + String.join(" ", f.palabras()) + "' está repetida en Palabras.java");
            }
        }
        // La más larga primero, para que "si no" gane sobre "si".
        frases.sort(Comparator.comparingInt((Frase f) -> f.palabras().size()).reversed());
        return frases;
    }

    private static Frase frase(String texto, TipoToken tipo, Object valor) {
        return new Frase(Arrays.asList(normalizar(texto).split("\\s+")), tipo, valor);
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

    private boolean hayMas() {
        return pos < codigo.length();
    }

    // '\0' indica que ya no hay más código.
    private char actual(int k) {
        int i = pos + k;
        return i < codigo.length() ? codigo.charAt(i) : '\0';
    }

    private char actual() {
        return actual(0);
    }

    private void avanzar() {
        if (codigo.charAt(pos++) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
    }

    private void agregar(TipoToken tipo, Object valor, int inicio, int lineaInicio, int columnaInicio) {
        tokens.add(new Token(tipo, valor, codigo.substring(inicio, pos), lineaInicio, columnaInicio));
    }

    public List<Token> tokenizar() {
        while (hayMas()) {
            char c = actual();
            if (Character.isWhitespace(c) || c == '¿' || c == '¡') {
                avanzar();
            } else if (c == '#') {
                while (hayMas() && actual() != '\n') {
                    avanzar();
                }
            } else if (c == '"') {
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

    private void leerTexto() {
        int inicio = pos, l = linea, col = columna;
        avanzar();
        while (actual() != '"') {
            if (!hayMas() || actual() == '\n') {
                throw new ErrorLexico("Dejó el chisme a medias: le faltó cerrar las comillas.", l, col);
            }
            avanzar();
        }
        avanzar();
        agregar(TipoToken.TEXTO, codigo.substring(inicio + 1, pos - 1), inicio, l, col);
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
        if (esInicioPalabra(actual())) {
            while (esPartePalabra(actual())) {
                avanzar();
            }
            throw new ErrorLexico("'" + codigo.substring(inicio, pos) + "' no es ni plata ni nombre.", l, col);
        }
        String lexema = codigo.substring(inicio, pos);
        Object valor = esDecimal ? (Object) Double.parseDouble(lexema) : (Object) Long.parseLong(lexema);
        agregar(TipoToken.NUMERO, valor, inicio, l, col);
    }

    private void leerPalabra() {
        int inicio = pos, l = linea, col = columna;

        // Palabras que siguen en la misma línea, con la posición donde termina cada una.
        List<String> palabras = new ArrayList<>();
        List<Integer> finales = new ArrayList<>();
        int p = pos;
        while (true) {
            int q = p;
            while (!palabras.isEmpty() && q < codigo.length() && (codigo.charAt(q) == ' ' || codigo.charAt(q) == '\t')) {
                q++;
            }
            if (q >= codigo.length() || !esInicioPalabra(codigo.charAt(q))) {
                break;
            }
            int r = q;
            while (r < codigo.length() && esPartePalabra(codigo.charAt(r))) {
                r++;
            }
            palabras.add(normalizar(codigo.substring(q, r)));
            finales.add(r);
            p = r;
            if (palabras.size() == MAX_PALABRAS) {
                break;
            }
        }

        for (Frase f : FRASES) {
            int n = f.palabras().size();
            if (n <= palabras.size() && f.palabras().equals(palabras.subList(0, n))) {
                saltarHasta(finales.get(n - 1));
                if (f.tipo() == null) {
                    descartarMuletilla();
                } else {
                    agregar(f.tipo(), f.valor(), inicio, l, col);
                }
                return;
            }
        }
        saltarHasta(finales.get(0));
        agregar(TipoToken.IDENTIFICADOR, codigo.substring(inicio, pos), inicio, l, col);
    }

    // Solo se usa dentro de una misma línea.
    private void saltarHasta(int fin) {
        columna += fin - pos;
        pos = fin;
    }

    // Las comas del vocativo ("Quiubo, parce, ...") se van con la muletilla.
    private void descartarMuletilla() {
        if (!tokens.isEmpty() && "COMA".equals(tokens.get(tokens.size() - 1).valor())) {
            tokens.remove(tokens.size() - 1);
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
