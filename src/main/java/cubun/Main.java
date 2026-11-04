package cubun;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cubun.lexer.Lexer;
import cubun.lexer.Token;
import cubun.parser.ImpresorAst;
import cubun.parser.Parser;
import cubun.parser.Programa;

public class Main {

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean soloTokens = args.length == 2 && args[0].equals("--tokens");
        if (args.length != 1 && !soloTokens) {
            out.println("Uso: java -jar target/cubun.jar [--tokens] archivo.cbn");
            System.exit(1);
        }
        String codigo = Files.readString(Path.of(args[args.length - 1]), StandardCharsets.UTF_8);
        try {
            List<Token> tokens = Lexer.tokenizar(codigo);
            if (soloTokens) {
                imprimirTokens(out, tokens);
                return;
            }
            Programa programa = Parser.parsear(tokens);
            out.print(ImpresorAst.imprimir(programa));
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
