package asistencia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Integración OPCIONAL con JARVIS (solo si en tu config.properties pones jarvis.url y jarvis.token).
 * Envía avisos a «Compartir con JARVIS» (POST /api/share): te llegan aunque no estés delante del PC.
 * Si no está configurado, no hace nada.
 */
final class Jarvis {
    private final String url;
    private final String token;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    Jarvis(Config c) {
        this.url = c.jarvisUrl().replaceAll("/+$", "");
        this.token = c.jarvisToken();
    }

    boolean activo() { return !url.isEmpty() && !token.isEmpty(); }

    /** Envío asíncrono; si falla se ignora (JARVIS es un extra, no puede romper la app). */
    void avisar(String asunto, String texto) {
        if (!activo()) return;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("subject", asunto);
        cuerpo.put("text", texto);
        cuerpo.put("from", "Discoteca · asistencia");
        HttpRequest req = HttpRequest.newBuilder(URI.create(url + "/api/share"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.write(cuerpo)))
                .build();
        http.sendAsync(req, HttpResponse.BodyHandlers.discarding()).exceptionally(e -> null);
    }
}
