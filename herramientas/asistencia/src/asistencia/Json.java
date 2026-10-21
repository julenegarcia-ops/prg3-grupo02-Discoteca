package asistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lector/escritor JSON mínimo (para no depender de librerías externas). */
final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    static Object parse(String texto) {
        Json p = new Json(texto);
        p.ws();
        Object v = p.valor();
        p.ws();
        if (p.i != p.s.length()) throw p.error("texto sobrante");
        return v;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> obj(Object o) { return o instanceof Map ? (Map<String, Object>) o : new LinkedHashMap<>(); }

    @SuppressWarnings("unchecked")
    static List<Object> arr(Object o) { return o instanceof List ? (List<Object>) o : new ArrayList<>(); }

    static String str(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        return v == null ? null : String.valueOf(v);
    }

    static long num(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        return v instanceof Number n ? n.longValue() : 0;
    }

    // ---------- lectura ----------

    private Object valor() {
        if (i >= s.length()) throw error("fin inesperado");
        char c = s.charAt(i);
        switch (c) {
            case '{': return objeto();
            case '[': return lista();
            case '"': return cadena();
            case 't': literal("true"); return Boolean.TRUE;
            case 'f': literal("false"); return Boolean.FALSE;
            case 'n': literal("null"); return null;
            default: return numero();
        }
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;
        ws();
        if (es('}')) { i++; return m; }
        while (true) {
            ws();
            String k = cadena();
            ws();
            espera(':');
            ws();
            m.put(k, valor());
            ws();
            if (es(',')) { i++; continue; }
            espera('}');
            return m;
        }
    }

    private List<Object> lista() {
        List<Object> l = new ArrayList<>();
        i++;
        ws();
        if (es(']')) { i++; return l; }
        while (true) {
            ws();
            l.add(valor());
            ws();
            if (es(',')) { i++; continue; }
            espera(']');
            return l;
        }
    }

    private String cadena() {
        espera('"');
        StringBuilder b = new StringBuilder();
        while (true) {
            if (i >= s.length()) throw error("cadena sin cerrar");
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c != '\\') { b.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) {
                case 'n' -> b.append('\n');
                case 't' -> b.append('\t');
                case 'r' -> b.append('\r');
                case 'b' -> b.append('\b');
                case 'f' -> b.append('\f');
                case 'u' -> { b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                default -> b.append(e);
            }
        }
    }

    private Object numero() {
        int ini = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (ini == i) throw error("valor inesperado");
        String n = s.substring(ini, i);
        if (n.matches("-?\\d{1,18}")) return Long.parseLong(n);
        return Double.parseDouble(n);
    }

    private void literal(String l) {
        if (!s.startsWith(l, i)) throw error("se esperaba " + l);
        i += l.length();
    }

    private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
    private boolean es(char c) { return i < s.length() && s.charAt(i) == c; }
    private void espera(char c) { if (!es(c)) throw error("se esperaba '" + c + "'"); i++; }
    private IllegalArgumentException error(String msg) { return new IllegalArgumentException("JSON inválido (" + msg + ") en posición " + i); }

    // ---------- escritura ----------

    static String write(Object o) {
        StringBuilder b = new StringBuilder();
        escribe(b, o);
        return b.toString();
    }

    private static void escribe(StringBuilder b, Object o) {
        if (o == null) b.append("null");
        else if (o instanceof String t) escapa(b, t);
        else if (o instanceof Number || o instanceof Boolean) b.append(o);
        else if (o instanceof Map<?, ?> m) {
            b.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!primero) b.append(',');
                primero = false;
                escapa(b, String.valueOf(e.getKey()));
                b.append(':');
                escribe(b, e.getValue());
            }
            b.append('}');
        } else if (o instanceof List<?> l) {
            b.append('[');
            for (int k = 0; k < l.size(); k++) {
                if (k > 0) b.append(',');
                escribe(b, l.get(k));
            }
            b.append(']');
        } else escapa(b, o.toString());
    }

    private static void escapa(StringBuilder b, String t) {
        b.append('"');
        for (char c : t.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        b.append('"');
    }
}
