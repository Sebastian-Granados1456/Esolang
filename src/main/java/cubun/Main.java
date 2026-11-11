package cubun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cubun.interprete.Interprete;
import cubun.lexer.Lexer;
import cubun.lexer.Token;
import cubun.parser.ImpresorAst;
import cubun.parser.Parser;
import cubun.parser.Programa;

public class Main {

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        String modo = args.length == 2 ? args[0] : "";
        if (args.length == 0 || args.length > 2 || (args.length == 2 && !modo.equals("--tokens") && !modo.equals("--arbol"))) {
            out.println("Uso: java -jar target/cubun.jar [--tokens | --arbol] archivo.cbn");
            System.exit(1);
        }
        String codigo = Files.readString(Path.of(args[args.length - 1]), StandardCharsets.UTF_8);
        try {
            List<Token> tokens = Lexer.tokenizar(codigo);
            if (modo.equals("--tokens")) {
                imprimirTokens(out, tokens);
                return;
            }
            Programa programa = Parser.parsear(tokens);
            if (modo.equals("--arbol")) {
                out.print(ImpresorAst.imprimir(programa));
                return;
            }
            BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            new Interprete(out, entrada).ejecutar(programa);
        } catch (ErrorCubun e) {
            out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void imprimirTokens(PrintStream out, List<Token> tokens) {
        out.printf("%-10s %-14s %-22s %s%n", "LÍNEA:COL", "TIPO", "VALOR", "LEXEMA");
        for (Token t : tokens) {
            String valor = t.valor() instanceof String s ? "'" + s + "'" : String.valueOf(t.valor());
            out.printf("%-10s %-14s %-22s '%s'%n", t.linea() + ":" + t.columna(), t.tipo(), valor, t.lexema());
        }
    }
}
