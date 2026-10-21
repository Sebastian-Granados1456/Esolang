package rolo;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rolo.lexer.ErrorLexico;
import rolo.lexer.Lexer;
import rolo.lexer.Token;

public class Main {

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        if (args.length != 1) {
            out.println("Uso: java -jar rolo.jar archivo.rolo");
            System.exit(1);
        }
        String codigo = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        List<Token> tokens;
        try {
            tokens = Lexer.tokenizar(codigo);
        } catch (ErrorLexico e) {
            out.println(e.getMessage());
            System.exit(1);
            return;
        }
        out.printf("%-10s %-14s %-22s %s%n", "LÍNEA:COL", "TIPO", "VALOR", "LEXEMA");
        for (Token t : tokens) {
            String valor = t.valor() instanceof String s ? "'" + s.replace("\n", "\\n") + "'" : String.valueOf(t.valor());
            out.printf("%-10s %-14s %-22s '%s'%n", t.linea() + ":" + t.columna(), t.tipo(), valor, t.lexema());
        }
    }
}
