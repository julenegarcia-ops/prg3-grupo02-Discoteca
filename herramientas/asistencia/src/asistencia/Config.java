package asistencia;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración LOCAL de cada persona (en su carpeta de usuario, nunca en el repositorio):
 * ~/.discoteca-asistencia/config.properties
 */
final class Config {
    static final Path DIR = Path.of(System.getProperty("user.home"), ".discoteca-asistencia");
    private static final Path FICHERO = DIR.resolve("config.properties");
    static final String REPO_POR_DEFECTO = "julenegarcia-ops/prg3-grupo02-Discoteca";

    private final Properties p = new Properties();

    static Config cargar() {
        Config c = new Config();
        if (Files.exists(FICHERO)) {
            try (Reader r = Files.newBufferedReader(FICHERO, StandardCharsets.UTF_8)) {
                c.p.load(r);
            } catch (IOException ignorado) {
                // config corrupta: se vuelve a pedir
            }
        }
        return c;
    }

    void guardar() throws IOException {
        Files.createDirectories(DIR);
        try (Writer w = Files.newBufferedWriter(FICHERO, StandardCharsets.UTF_8)) {
            p.store(w, "Asistencia Discoteca - contiene tu token: NO lo subas a GitHub ni lo compartas");
        }
    }

    static Path fichero() { return FICHERO; }

    String token() { return p.getProperty("token", "").trim(); }
    void token(String t) { p.setProperty("token", t.trim()); }

    String repo() { return p.getProperty("repo", REPO_POR_DEFECTO).trim(); }
    void repo(String r) { p.setProperty("repo", r.trim()); }

    /** Minutos entre comprobaciones «¿sigues ahí?» (admite decimales para hacer pruebas). */
    double intervaloMinutos() { return decimal("intervalo.minutos", 25); }

    /** Minutos que tienes para responder antes de que te marquen inactivo. */
    double graciaMinutos() { return decimal("gracia.minutos", 5); }

    /** Opcional, solo para quien tenga JARVIS: URL base, p. ej. https://mi-jarvis.ts.net */
    String jarvisUrl() { return p.getProperty("jarvis.url", "").trim(); }

    /** Opcional: token de dispositivo de JARVIS (POST /api/auth/device-token). */
    String jarvisToken() { return p.getProperty("jarvis.token", "").trim(); }

    private double decimal(String clave, double defecto) {
        try {
            double v = Double.parseDouble(p.getProperty(clave, String.valueOf(defecto)).trim());
            return v > 0 ? v : defecto;
        } catch (NumberFormatException e) {
            return defecto;
        }
    }
}
