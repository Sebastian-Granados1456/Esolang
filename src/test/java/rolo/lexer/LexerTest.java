package rolo.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class LexerTest {

    private record TV(TipoToken tipo, Object valor) {
    }

    private static TV tv(TipoToken tipo, Object valor) {
        return new TV(tipo, valor);
    }

    // Tipos y valores de los tokens, sin el FIN.
    private static List<TV> tiposValores(String codigo) {
        List<Token> tokens = Lexer.tokenizar(codigo);
        return tokens.subList(0, tokens.size() - 1).stream()
                .map(t -> tv(t.tipo(), t.valor()))
                .toList();
    }

    private static void assertError(String codigo, int linea, int columna, String fragmento) {
        ErrorLexico e = assertThrows(ErrorLexico.class, () -> Lexer.tokenizar(codigo));
        assertEquals(linea, e.getLinea());
        assertEquals(columna, e.getColumna());
        assertTrue(e.getMessage().contains(fragmento), e.getMessage());
    }

    // Tokens básicos

    @Test
    void programaVacioSoloTieneFin() {
        List<Token> tokens = Lexer.tokenizar("");
        assertEquals(1, tokens.size());
        assertEquals(TipoToken.FIN, tokens.get(0).tipo());
    }

    @Test
    void enterosYDecimales() {
        assertEquals(List.of(tv(TipoToken.NUMERO, 42L), tv(TipoToken.NUMERO, 3.14)),
                tiposValores("42 3.14"));
    }

    @Test
    void puntoDespuesDeNumeroEsFinDeFrase() {
        assertEquals(List.of(
                tv(TipoToken.PALABRA_CLAVE, "VALE"),
                tv(TipoToken.NUMERO, 5L),
                tv(TipoToken.PUNTUACION, "FIN_FRASE")), tiposValores("vale 5."));
    }

    @Test
    void identificadoresConTildesEnieYGuionBajo() {
        assertEquals(List.of(
                tv(TipoToken.IDENTIFICADOR, "año"),
                tv(TipoToken.IDENTIFICADOR, "precio_final"),
                tv(TipoToken.IDENTIFICADOR, "mamá2")), tiposValores("año precio_final mamá2"));
    }

    @Test
    void booleanos() {
        assertEquals(List.of(
                tv(TipoToken.BOOLEANO, true),
                tv(TipoToken.PUNTUACION, "COMA"),
                tv(TipoToken.BOOLEANO, false)), tiposValores("de una, ni de fundas"));
    }

    // Textos

    @Test
    void comillasDoblesYLatinas() {
        assertEquals(List.of(tv(TipoToken.TEXTO, "hola"), tv(TipoToken.TEXTO, "qué más")),
                tiposValores("\"hola\" «qué más»"));
    }

    @Test
    void escapes() {
        Token t = Lexer.tokenizar("\"línea\\nnueva \\\"citada\\\" \\\\\"").get(0);
        assertEquals("línea\nnueva \"citada\" \\", t.valor());
    }

    @Test
    void palabrasClaveDentroDelTextoNoCuentan() {
        assertEquals(List.of(tv(TipoToken.TEXTO, "quiubo parce")), tiposValores("\"quiubo parce\""));
    }

    // Palabras clave

    @Test
    void noImportanMayusculasNiTildes() {
        for (String frase : List.of("Pídale a", "PIDALE A", "pidale a")) {
            assertEquals(List.of(tv(TipoToken.PALABRA_CLAVE, "LLAMAR")), tiposValores(frase));
        }
    }

    @Test
    void fraseDeVariasPalabrasGuardaElLexemaOriginal() {
        Token t = Lexer.tokenizar("Suelte   el chisme").get(0);
        assertEquals("IMPRIMIR", t.valor());
        assertEquals("Suelte   el chisme", t.lexema());
    }

    @Test
    void ganaLaFraseMasLarga() {
        assertEquals(List.of(tv(TipoToken.PALABRA_CLAVE, "SI")), tiposValores("si"));
        assertEquals(List.of(tv(TipoToken.PALABRA_CLAVE, "SINO")), tiposValores("si no"));
        assertEquals(List.of(tv(TipoToken.PALABRA_CLAVE, "FIN_BLOQUE")), tiposValores("listo el pollo"));
    }

    @Test
    void fraseIncompletaVuelveAtras() {
        // "dele" solo no es palabra clave, "dele mientras" sí
        assertEquals(List.of(
                tv(TipoToken.IDENTIFICADOR, "dele"),
                tv(TipoToken.IDENTIFICADOR, "x")), tiposValores("dele x"));
        assertEquals(List.of(
                tv(TipoToken.OPERADOR, "o"),
                tv(TipoToken.PALABRA_CLAVE, "SI"),
                tv(TipoToken.IDENTIFICADOR, "x")), tiposValores("o si x"));
    }

    @Test
    void fraseNoCruzaLineas() {
        assertEquals(List.of(
                tv(TipoToken.IDENTIFICADOR, "ponga"),
                tv(TipoToken.IDENTIFICADOR, "que")), tiposValores("ponga\nque"));
    }

    @Test
    void palabraQueEmpiezaIgualAUnaClaveEsIdentificador() {
        assertEquals(List.of(
                tv(TipoToken.IDENTIFICADOR, "sino"),
                tv(TipoToken.IDENTIFICADOR, "listos")), tiposValores("sino listos"));
    }

    // Operadores

    @Test
    void simbolosDeDosCaracteres() {
        List<Object> valores = tiposValores("== != <= >= < > =").stream().map(TV::valor).toList();
        assertEquals(List.of("==", "!=", "<=", ">=", "<", ">", "="), valores);
    }

    @Test
    void operadoresVerbalesEquivalenASimbolos() {
        assertEquals(tiposValores("a + b"), tiposValores("a más b"));
        assertEquals(tiposValores("a >= b"), tiposValores("a mayor o igual que b"));
        assertEquals(tiposValores("a == b"), tiposValores("a igualito a b"));
    }

    @Test
    void signoDeAdmiracionSoloEsFinDeFrase() {
        assertEquals(List.of(
                tv(TipoToken.PALABRA_CLAVE, "FIN_BLOQUE"),
                tv(TipoToken.PUNTUACION, "FIN_FRASE")), tiposValores("¡Listo!"));
    }

    // Muletillas y comentarios

    @Test
    void muletillasSeDescartanConSusComas() {
        assertEquals(List.of(
                tv(TipoToken.PALABRA_CLAVE, "INICIO"),
                tv(TipoToken.PUNTUACION, "FIN_FRASE")), tiposValores("¿Quiubo, parce?"));
        assertEquals(List.of(
                tv(TipoToken.PALABRA_CLAVE, "IMPRIMIR"),
                tv(TipoToken.IDENTIFICADOR, "x")), tiposValores("Sumercé, diga x"));
    }

    @Test
    void comentarioEsHablaEgocentrica() {
        assertEquals(List.of(
                tv(TipoToken.IDENTIFICADOR, "x"),
                tv(TipoToken.COMENTARIO, "pa' mí: esto es la plata"),
                tv(TipoToken.IDENTIFICADOR, "z")), tiposValores("x # pa' mí: esto es la plata\nz"));
    }

    // Posiciones

    @Test
    void lineaYColumna() {
        List<int[]> posiciones = Lexer.tokenizar("quiubo\n  ponga que x vale 1.").stream()
                .map(t -> new int[] {t.linea(), t.columna()})
                .toList();
        int[][] esperadas = {{1, 1}, {2, 3}, {2, 13}, {2, 15}, {2, 20}, {2, 21}, {2, 22}};
        assertEquals(esperadas.length, posiciones.size());
        for (int i = 0; i < esperadas.length; i++) {
            assertEquals(esperadas[i][0], posiciones.get(i)[0], "línea del token " + i);
            assertEquals(esperadas[i][1], posiciones.get(i)[1], "columna del token " + i);
        }
    }

    // Errores

    @Test
    void caracterDesconocido() {
        assertError("quiubo\nx vale @", 2, 8, "'@'");
    }

    @Test
    void textoSinCerrar() {
        assertError("diga \"hola\nchao", 1, 6, "cerrar las comillas");
    }

    @Test
    void numeroPegadoALetras() {
        assertError("vale 12lucas", 1, 6, "'12lucas'");
    }

    @Test
    void escapeDesconocido() {
        assertError("\"hola\\q\"", 1, 6, "'\\q'");
    }

    @Test
    void mensajeEnRolo() {
        assertError("$", 1, 1, "¡Qué boleta! Línea 1, columna 1");
    }
}
