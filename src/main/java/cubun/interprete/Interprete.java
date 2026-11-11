package cubun.interprete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cubun.parser.Expresion;
import cubun.parser.Expresion.Binaria;
import cubun.parser.Expresion.Booleano;
import cubun.parser.Expresion.ElMan;
import cubun.parser.Expresion.Numero;
import cubun.parser.Expresion.Texto;
import cubun.parser.Expresion.Unaria;
import cubun.parser.Expresion.Variable;
import cubun.parser.Programa;
import cubun.parser.Sentencia;
import cubun.parser.Sentencia.Asignacion;
import cubun.parser.Sentencia.Imprimir;
import cubun.parser.Sentencia.Leer;
import cubun.parser.Sentencia.Mientras;
import cubun.parser.Sentencia.Si;

public class Interprete {

    private final Map<String, Object> variables = new HashMap<>();
    private Object ultimo;
    private final PrintStream salida;
    private final BufferedReader entrada;

    public Interprete(PrintStream salida, BufferedReader entrada) {
        this.salida = salida;
        this.entrada = entrada;
    }

    public void ejecutar(Programa programa) {
        ejecutar(programa.sentencias());
    }

    private void ejecutar(List<Sentencia> sentencias) {
        for (Sentencia s : sentencias) {
            ejecutar(s);
        }
    }

    private void ejecutar(Sentencia s) {
        switch (s) {
            case Asignacion a -> {
                ultimo = evaluar(a.valor());
                variables.put(a.nombre(), ultimo);
            }
            case Imprimir i -> {
                ultimo = evaluar(i.valor());
                salida.println(Valores.texto(ultimo));
            }
            case Leer l -> {
                ultimo = leer(l.linea());
                variables.put(l.nombre(), ultimo);
            }
            case Si si -> ejecutar(condicion(si.condicion(), "si", si.linea()) ? si.entonces() : si.sino());
            case Mientras m -> {
                while (condicion(m.condicion(), "mientras", m.linea())) {
                    ejecutar(m.cuerpo());
                }
            }
        }
    }

    private boolean condicion(Expresion e, String nombre, int linea) {
        Object valor = evaluar(e);
        if (!(valor instanceof Boolean b)) {
            throw new ErrorEjecucion("La condición del «" + nombre + "» tiene que dar de una o nokas, pero dio "
                    + Valores.describir(valor) + ".", linea);
        }
        return b;
    }

    private Object leer(int linea) {
        String texto;
        try {
            texto = entrada.readLine();
        } catch (IOException ex) {
            throw new ErrorEjecucion("No se pudo leer lo que escribió.", linea);
        }
        if (texto == null) {
            return "";
        }
        texto = texto.strip();
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException noEsEntero) {
            try {
                return Valores.numero(Double.parseDouble(texto));
            } catch (NumberFormatException noEsNumero) {
                return texto;
            }
        }
    }

    private Object evaluar(Expresion e) {
        return switch (e) {
            case Numero n -> n.valor();
            case Texto t -> t.valor();
            case Booleano b -> b.valor();
            case Variable v -> variable(v);
            case ElMan m -> {
                if (ultimo == null) {
                    throw new ErrorEjecucion("¿Cuál man? Todavía no se ha hablado de nada.", m.linea());
                }
                yield ultimo;
            }
            case Unaria u -> unaria(u);
            case Binaria b -> binaria(b);
        };
    }

    private Object variable(Variable v) {
        if (!variables.containsKey(v.nombre())) {
            throw new ErrorEjecucion("¿Y quién es «" + v.nombre() + "»? Nadie la ha puesto.", v.linea());
        }
        return variables.get(v.nombre());
    }

    private Object unaria(Unaria u) {
        Object valor = evaluar(u.operando());
        if (u.operador().equals("no")) {
            return !booleano(valor, "no", u.linea());
        }
        if (!(valor instanceof Number n)) {
            throw new ErrorEjecucion("No se le puede poner «-» a " + Valores.describir(valor) + ".", u.linea());
        }
        return Valores.numero(-n.doubleValue());
    }

    private Object binaria(Binaria b) {
        String op = b.operador();
        if (op.equals("y")) {
            return booleano(evaluar(b.izquierda()), "y", b.linea()) && booleano(evaluar(b.derecha()), "y", b.linea());
        }
        if (op.equals("o")) {
            return booleano(evaluar(b.izquierda()), "o", b.linea()) || booleano(evaluar(b.derecha()), "o", b.linea());
        }
        Object a = evaluar(b.izquierda());
        Object d = evaluar(b.derecha());
        return switch (op) {
            case "+" -> a instanceof String || d instanceof String
                    ? Valores.texto(a) + Valores.texto(d)
                    : Valores.aritmetica(op, a, d, b.linea());
            case "-", "*", "/", "%" -> Valores.aritmetica(op, a, d, b.linea());
            case "==" -> Valores.iguales(a, d);
            case "!=" -> !Valores.iguales(a, d);
            default -> Valores.comparar(op, a, d, b.linea());
        };
    }

    private boolean booleano(Object valor, String op, int linea) {
        if (!(valor instanceof Boolean b)) {
            throw new ErrorEjecucion("«" + op + "» solo funciona con de una o nokas, pero llegó "
                    + Valores.describir(valor) + ".", linea);
        }
        return b;
    }
}
