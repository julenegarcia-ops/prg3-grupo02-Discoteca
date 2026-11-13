package asistencia;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente de la API REST de GitHub. Todo se guarda en el propio repositorio:
 *  - rama {@value #RAMA}: presencia/&lt;login&gt;.json y registro/&lt;login&gt;.csv
 *  - una Issue con la etiqueta {@value #ETIQUETA_CHAT}: el chat (cada mensaje es un comentario)
 */
final class GitHub {
    static final String API = "https://api.github.com";
    static final String RAMA = "asistencia-datos";
    static final String ETIQUETA_CHAT = "chat-asistencia";
    static final String TITULO_CHAT = "💬 Chat del grupo";

    /** Error devuelto por GitHub (status = código HTTP). */
    static final class Fallo extends IOException {
        final int status;
        Fallo(int status, String msg) { super(msg); this.status = status; }
    }

    record Archivo(String contenido, String sha) {}

    final String owner;
    final String repo;
    private final String token;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    GitHub(String ownerRepo, String token) {
        String[] partes = ownerRepo.split("/");
        if (partes.length != 2) throw new IllegalArgumentException("El repositorio debe ser usuario/nombre");
        this.owner = partes[0];
        this.repo = partes[1];
        this.token = token;
    }

    private String base() { return "/repos/" + owner + "/" + repo; }

    String urlWeb() { return "https://github.com/" + owner + "/" + repo; }

    Object llamar(String metodo, String ruta, Object cuerpo) throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(ruta.startsWith("http") ? ruta : API + ruta))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "discoteca-asistencia");
        if (cuerpo == null) b.method(metodo, HttpRequest.BodyPublishers.noBody());
        else b.header("Content-Type", "application/json").method(metodo, HttpRequest.BodyPublishers.ofString(Json.write(cuerpo)));
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300) throw new Fallo(r.statusCode(), mensaje(r));
        return r.body() == null || r.body().isBlank() ? null : Json.parse(r.body());
    }

    private static String mensaje(HttpResponse<String> r) {
        String m;
        try {
            m = Json.str(Json.obj(Json.parse(r.body())), "message");
        } catch (RuntimeException e) {
            m = null;
        }
        return switch (r.statusCode()) {
            case 401 -> "Token no válido o caducado";
            case 403 -> "Sin permiso (¿te han añadido como colaborador? ¿el token tiene permisos?): " + m;
            case 404 -> "No encontrado: " + m;
            default -> "GitHub " + r.statusCode() + ": " + m;
        };
    }

    // ---------- usuario ----------

    String login() throws IOException, InterruptedException {
        return Json.str(Json.obj(llamar("GET", "/user", null)), "login");
    }

    /** Estado del perfil de GitHub (el «activo» de GitHub). Necesita un token clásico con el permiso «user». */
    void estadoPerfil(String emoji, String mensaje) throws IOException, InterruptedException {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("emoji", emoji);
        input.put("message", mensaje);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("query", "mutation($i: ChangeUserStatusInput!) { changeUserStatus(input: $i) { status { message } } }");
        cuerpo.put("variables", Map.of("i", input));
        Map<String, Object> r = Json.obj(llamar("POST", "/graphql", cuerpo));
        if (r.get("errors") != null) throw new Fallo(200, "GraphQL: " + Json.write(r.get("errors")));
    }

    // ---------- archivos en la rama de datos ----------

    /** Crea la rama de datos (huérfana, sin código) si todavía no existe. */
    void asegurarRama() throws IOException, InterruptedException {
        try {
            llamar("GET", base() + "/git/ref/heads/" + RAMA, null);
            return;
        } catch (Fallo f) {
            if (f.status != 404) throw f;
        }
        try {
            Map<String, Object> archivo = new LinkedHashMap<>();
            archivo.put("path", "LEEME.md");
            archivo.put("mode", "100644");
            archivo.put("type", "blob");
            archivo.put("content", "# Datos de asistencia\n\nRama generada por `herramientas/asistencia`. No la mezcles con `main`.\n\n"
                    + "- `presencia/<usuario>.json`: estado actual de cada persona\n- `registro/<usuario>.csv`: sesiones de trabajo\n");
            String arbol = Json.str(Json.obj(llamar("POST", base() + "/git/trees", Map.of("tree", List.of(archivo)))), "sha");
            Map<String, Object> commit = new LinkedHashMap<>();
            commit.put("message", "Crea la rama de asistencia");
            commit.put("tree", arbol);
            commit.put("parents", List.of());
            String sha = Json.str(Json.obj(llamar("POST", base() + "/git/commits", commit)), "sha");
            llamar("POST", base() + "/git/refs", Map.of("ref", "refs/heads/" + RAMA, "sha", sha));
        } catch (Fallo f) {
            if (f.status == 409) throw new Fallo(409, "El repositorio está vacío: haced antes el primer commit en main");
            if (f.status != 422) throw f; // 422 = otra persona la ha creado a la vez
        }
    }

    Archivo leer(String ruta) throws IOException, InterruptedException {
        try {
            Map<String, Object> r = Json.obj(llamar("GET", base() + "/contents/" + ruta + "?ref=" + RAMA, null));
            String b64 = Json.str(r, "content");
            String texto = new String(Base64.getMimeDecoder().decode(b64 == null ? "" : b64), StandardCharsets.UTF_8);
            return new Archivo(texto, Json.str(r, "sha"));
        } catch (Fallo f) {
            if (f.status == 404) return null;
            throw f;
        }
    }

    void escribir(String ruta, String contenido, String sha, String mensaje) throws IOException, InterruptedException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("message", mensaje);
        cuerpo.put("content", Base64.getEncoder().encodeToString(contenido.getBytes(StandardCharsets.UTF_8)));
        cuerpo.put("branch", RAMA);
        if (sha != null) cuerpo.put("sha", sha);
        llamar("PUT", base() + "/contents/" + ruta, cuerpo);
    }

    List<String> listar(String carpeta) throws IOException, InterruptedException {
        List<String> nombres = new ArrayList<>();
        try {
            for (Object o : Json.arr(llamar("GET", base() + "/contents/" + carpeta + "?ref=" + RAMA, null))) {
                Map<String, Object> e = Json.obj(o);
                if ("file".equals(Json.str(e, "type"))) nombres.add(Json.str(e, "name"));
            }
        } catch (Fallo f) {
            if (f.status != 404) throw f;
        }
        return nombres;
    }

    // ---------- chat (Issue) ----------

    /** Número de la Issue del chat; la crea si no existe. */
    long issueChat() throws IOException, InterruptedException {
        long elegida = 0;
        for (Object o : Json.arr(llamar("GET", base() + "/issues?state=all&per_page=100&labels=" + ETIQUETA_CHAT, null))) {
            long n = Json.num(Json.obj(o), "number");
            if (elegida == 0 || n < elegida) elegida = n; // si dos la crearon a la vez, todos usan la más antigua
        }
        if (elegida != 0) return elegida;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("title", TITULO_CHAT);
        cuerpo.put("body", "Chat de la app de asistencia (`herramientas/asistencia`). Se puede escribir desde la app o desde aquí.\n\n"
                + "Mencionad a alguien con @usuario y GitHub le avisará.");
        cuerpo.put("labels", List.of(ETIQUETA_CHAT));
        return Json.num(Json.obj(llamar("POST", base() + "/issues", cuerpo)), "number");
    }

    /** Comentarios de la Issue actualizados desde {@code desdeIso} (o todos si es null). */
    List<Map<String, Object>> comentarios(long issue, String desdeIso) throws IOException, InterruptedException {
        List<Map<String, Object>> todos = new ArrayList<>();
        for (int pagina = 1; pagina <= 20; pagina++) {
            String ruta = base() + "/issues/" + issue + "/comments?per_page=100&page=" + pagina
                    + (desdeIso == null ? "" : "&since=" + URLEncoder.encode(desdeIso, StandardCharsets.UTF_8));
            List<Object> lote = Json.arr(llamar("GET", ruta, null));
            for (Object o : lote) todos.add(Json.obj(o));
            if (lote.size() < 100) break;
        }
        return todos;
    }

    void comentar(long issue, String texto) throws IOException, InterruptedException {
        llamar("POST", base() + "/issues/" + issue + "/comments", Map.of("body", texto));
    }
}
