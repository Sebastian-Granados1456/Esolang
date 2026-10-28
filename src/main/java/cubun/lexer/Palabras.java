package cubun.lexer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Vocabulario de Cubun: para agregar una frase basta con añadirla a su lista (tildes y mayúsculas da igual).
public final class Palabras {

    private Palabras() {
    }

    public static final Map<String, List<String>> PALABRAS_CLAVE = new LinkedHashMap<>();
    public static final Map<String, List<String>> OPERADORES_VERBALES = new LinkedHashMap<>();
    public static final Map<Boolean, List<String>> BOOLEANOS = new LinkedHashMap<>();

    static {
        PALABRAS_CLAVE.put("INICIO", List.of("quiubo", "buenas", "qué más"));
        PALABRAS_CLAVE.put("FIN", List.of("chao", "nos pillamos", "se cuida"));
        PALABRAS_CLAVE.put("DECLARAR", List.of("ponga que"));
        PALABRAS_CLAVE.put("VALE", List.of("vale"));
        PALABRAS_CLAVE.put("IMPRIMIR", List.of("suelte el chisme", "cuente", "diga"));
        PALABRAS_CLAVE.put("LEER", List.of("cuénteme", "pregunte"));
        PALABRAS_CLAVE.put("SI", List.of("si", "si es que", "por si las moscas"));
        PALABRAS_CLAVE.put("SINO", List.of("si no", "o si no", "de lo contrario"));
        PALABRAS_CLAVE.put("ENTONCES", List.of("entonces", "hágale"));
        PALABRAS_CLAVE.put("MIENTRAS", List.of("mientras", "dele mientras", "dele que dele mientras"));
        PALABRAS_CLAVE.put("FIN_BLOQUE", List.of("listo", "listo el pollo", "ya estuvo"));
        PALABRAS_CLAVE.put("FUNCION", List.of("camello"));
        PALABRAS_CLAVE.put("CON", List.of("con"));
        PALABRAS_CLAVE.put("RETORNAR", List.of("devuelva", "ahí le va"));
        PALABRAS_CLAVE.put("LLAMAR", List.of("pídale a", "pídale el favor a"));

        OPERADORES_VERBALES.put("+", List.of("más"));
        OPERADORES_VERBALES.put("-", List.of("menos"));
        OPERADORES_VERBALES.put("*", List.of("por"));
        OPERADORES_VERBALES.put("/", List.of("entre"));
        OPERADORES_VERBALES.put("%", List.of("módulo"));
        OPERADORES_VERBALES.put("==", List.of("igual a", "igualito a", "tal cuál"));
        OPERADORES_VERBALES.put("!=", List.of("distinto de", "diferente de"));
        OPERADORES_VERBALES.put(">", List.of("mayor que"));
        OPERADORES_VERBALES.put("<", List.of("menor que"));
        OPERADORES_VERBALES.put(">=", List.of("mayor o igual que"));
        OPERADORES_VERBALES.put("<=", List.of("menor o igual que"));
        OPERADORES_VERBALES.put("y", List.of("y"));
        OPERADORES_VERBALES.put("o", List.of("o"));
        OPERADORES_VERBALES.put("no", List.of("no"));

        BOOLEANOS.put(true, List.of("de una", "obvio"));
        BOOLEANOS.put(false, List.of("ni por el putas", "nokas"));
    }

    // Vocativos y relleno: el lexer los reconoce y los descarta.
    public static final List<String> MULETILLAS = List.of(
            "parce", "parcero", "sumercé", "veci", "mijo", "mija", "pues",
            "oiga", "ve", "vea", "bueno", "hermano", "llave", "socio", "chino", "china");

    // Los de dos caracteres van primero para que "==" no se lea como "=" "=".
    public static final List<String> OPERADORES_SIMBOLOS = List.of(
            "==", "!=", "<=", ">=", "+", "-", "*", "/", "%", "<", ">", "=");

    public static final Map<Character, String> PUNTUACION = Map.of(
            '.', "FIN_FRASE",
            '?', "FIN_FRASE",
            '!', "FIN_FRASE",
            ',', "COMA",
            ':', "DOS_PUNTOS",
            '(', "PAREN_ABRE",
            ')', "PAREN_CIERRA");
}
