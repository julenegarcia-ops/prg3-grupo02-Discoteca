package asistencia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Chat del grupo sobre los comentarios de una Issue del repositorio. */
final class Chat {
    record Mensaje(long id, String autor, Instant fecha, String texto) {}

    private final GitHub gh;
    private final long issue;
    private final Set<Long> vistos = new HashSet<>();
    private Instant desde = Instant.now().minusSeconds(14 * 86_400); // al abrir: los últimos 14 días

    Chat(GitHub gh) throws Exception {
        this.gh = gh;
        this.issue = gh.issueChat();
    }

    String urlWeb() { return gh.urlWeb() + "/issues/" + issue; }

    /** Mensajes nuevos desde la última llamada (llamar fuera del hilo de la interfaz). */
    synchronized List<Mensaje> nuevos() throws Exception {
        List<Mensaje> lista = new ArrayList<>();
        for (Map<String, Object> c : gh.comentarios(issue, desde.minusSeconds(5).toString())) {
            long id = Json.num(c, "id");
            if (!vistos.add(id)) continue;
            Instant fecha = Instant.parse(Json.str(c, "created_at"));
            String autor = Json.str(Json.obj(c.get("user")), "login");
            lista.add(new Mensaje(id, autor, fecha, Json.str(c, "body")));
            if (fecha.isAfter(desde)) desde = fecha;
        }
        lista.sort((a, b) -> a.fecha().compareTo(b.fecha()));
        return lista;
    }

    void enviar(String texto) throws Exception { gh.comentar(issue, texto); }
}
