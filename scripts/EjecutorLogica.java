import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Ejecuta clases de prueba JUnit 4 e imprime una línea por clase
 * y el detalle solo de los fallos. Lo usa scripts/probar-logica.sh.
 * Termina con código 1 si alguna prueba falla.
 */
public final class EjecutorLogica {

    private EjecutorLogica() {
    }

    public static void main(String[] args) throws Exception {
        int total = 0;
        int fallidas = 0;
        List<String> detalles = new ArrayList<>();
        for (String nombre : args) {
            Class<?> clase = Class.forName(nombre);
            Result r = new JUnitCore().run(Request.aClass(clase));
            total += r.getRunCount();
            fallidas += r.getFailureCount();
            String estado = r.wasSuccessful() ? "OK" : "FALLA";
            System.out.println(estado + ": " + nombre + " (" + r.getRunCount() + " pruebas)");
            for (Failure f : r.getFailures()) {
                Description d = f.getDescription();
                detalles.add("  " + d.getClassName() + "." + d.getMethodName() + ": "
                        + primeraLinea(f) + lineaDePrueba(f, d.getClassName()));
            }
        }
        for (String d : detalles) {
            System.out.println(d);
        }
        System.out.println("Resumen: " + (total - fallidas) + " de " + total + " pruebas pasan");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static String primeraLinea(Failure f) {
        String m = String.valueOf(f.getMessage());
        int salto = m.indexOf('\n');
        return salto < 0 ? m : m.substring(0, salto);
    }

    private static String lineaDePrueba(Failure f, String clase) {
        for (StackTraceElement e : f.getException().getStackTrace()) {
            if (e.getClassName().equals(clase)) {
                return " (línea " + e.getLineNumber() + ")";
            }
        }
        return "";
    }
}
