package cubun.interprete;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class Valores {

    private Valores() {
    }

    public static String texto(Object valor) {
        if (valor instanceof Boolean b) {
            return b ? "de una" : "nokas";
        }
        if (valor instanceof Double d) {
            return BigDecimal.valueOf(d).round(new MathContext(15)).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(valor);
    }

    public static String describir(Object valor) {
        if (valor instanceof Number) {
            return "un número (" + texto(valor) + ")";
        }
        if (valor instanceof String) {
            return "un texto (\"" + valor + "\")";
        }
        return "un booleano (" + texto(valor) + ")";
    }

    public static Object numero(double d) {
        if (d == Math.rint(d) && Math.abs(d) < 9e15) {
            return (long) d;
        }
        return d;
    }

    public static Object aritmetica(String op, Object a, Object b, int linea) {
        if (!(a instanceof Number x) || !(b instanceof Number y)) {
            throw new ErrorEjecucion("No se puede hacer «" + op + "» entre " + describir(a) + " y " + describir(b) + ".", linea);
        }
        if ((op.equals("/") || op.equals("%")) && y.doubleValue() == 0) {
            throw new ErrorEjecucion("¡Nadie puso pa' la vaca! No se puede dividir entre cero.", linea);
        }
        if (x instanceof Long i && y instanceof Long j && !op.equals("/")) {
            return switch (op) {
                case "+" -> i + j;
                case "-" -> i - j;
                case "*" -> i * j;
                default -> i % j;
            };
        }
        double i = x.doubleValue();
        double j = y.doubleValue();
        return numero(switch (op) {
            case "+" -> i + j;
            case "-" -> i - j;
            case "*" -> i * j;
            case "/" -> i / j;
            default -> i % j;
        });
    }

    public static boolean iguales(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return x.doubleValue() == y.doubleValue();
        }
        return Objects.equals(a, b);
    }

    public static boolean comparar(String op, Object a, Object b, int linea) {
        int c;
        if (a instanceof Number x && b instanceof Number y) {
            c = Double.compare(x.doubleValue(), y.doubleValue());
        } else if (a instanceof String x && b instanceof String y) {
            c = x.compareTo(y);
        } else {
            throw new ErrorEjecucion("No se puede comparar " + describir(a) + " con " + describir(b) + ".", linea);
        }
        return switch (op) {
            case "<" -> c < 0;
            case ">" -> c > 0;
            case "<=" -> c <= 0;
            default -> c >= 0;
        };
    }
}
