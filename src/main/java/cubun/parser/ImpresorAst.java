package cubun.parser;

import java.util.ArrayList;
import java.util.List;

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

public final class ImpresorAst {

    private record Rama(String etiqueta, List<Rama> hijos) {
        Rama(String etiqueta, Rama... hijos) {
            this(etiqueta, List.of(hijos));
        }
    }

    private ImpresorAst() {
    }

    public static String imprimir(Programa programa) {
        StringBuilder sb = new StringBuilder("Programa\n");
        dibujarHijos(sentencias(programa.sentencias()), "", sb);
        return sb.toString();
    }

    private static List<Rama> sentencias(List<Sentencia> lista) {
        List<Rama> ramas = new ArrayList<>();
        for (Sentencia s : lista) {
            ramas.add(rama(s));
        }
        return ramas;
    }

    private static Rama rama(Sentencia s) {
        return switch (s) {
            case Asignacion a -> new Rama("Ponga que: " + a.nombre(), rama(a.valor()));
            case Imprimir i -> new Rama("Imprimir", rama(i.valor()));
            case Leer l -> new Rama("Leer: " + l.nombre());
            case Si si -> {
                List<Rama> hijos = new ArrayList<>();
                hijos.add(new Rama("condición", rama(si.condicion())));
                hijos.add(new Rama("entonces", sentencias(si.entonces())));
                if (!si.sino().isEmpty()) {
                    hijos.add(new Rama("si no", sentencias(si.sino())));
                }
                yield new Rama("Si", hijos);
            }
            case Mientras m -> new Rama("Mientras", List.of(
                    new Rama("condición", rama(m.condicion())),
                    new Rama("cuerpo", sentencias(m.cuerpo()))));
        };
    }

    private static Rama rama(Expresion e) {
        return switch (e) {
            case Numero n -> new Rama("Número: " + n.valor());
            case Texto t -> new Rama("Texto: \"" + t.valor() + "\"");
            case Booleano b -> new Rama("Booleano: " + (b.valor() ? "de una" : "nokas"));
            case Variable v -> new Rama("Variable: " + v.nombre());
            case ElMan m -> new Rama("El man (último valor)");
            case Binaria b -> new Rama("Operación: " + b.operador(), rama(b.izquierda()), rama(b.derecha()));
            case Unaria u -> new Rama("Operación: " + u.operador(), rama(u.operando()));
        };
    }

    private static void dibujarHijos(List<Rama> hijos, String prefijo, StringBuilder sb) {
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = i == hijos.size() - 1;
            Rama r = hijos.get(i);
            sb.append(prefijo).append(ultimo ? "└── " : "├── ").append(r.etiqueta()).append('\n');
            dibujarHijos(r.hijos(), prefijo + (ultimo ? "    " : "│   "), sb);
        }
    }
}
