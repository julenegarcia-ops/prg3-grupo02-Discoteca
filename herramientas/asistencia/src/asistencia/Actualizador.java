package asistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Al abrir la app: copia de seguridad de tu carpeta del proyecto y después la actualiza con lo que hay en GitHub.
 *
 * La actualización es segura: solo hace «git pull --ff-only». Si tienes commits sin subir que chocan
 * con lo de GitHub, o cambios sin guardar en los mismos archivos, NO toca nada y te avisa.
 * Las copias se guardan en la carpeta oculta {@value #CARPETA_COPIAS} (las {@value #MAX_COPIAS} últimas).
 */
final class Actualizador {
    static final String CARPETA_COPIAS = esWindows() ? "IngasPutoAmo" : ".IngasPutoAmo";
    private static final int MAX_COPIAS = 10;
    private static final DateTimeFormatter NOMBRE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private record Salida(int codigo, String texto) {}

    private Actualizador() {}

    /** Devuelve un mensaje para la persona (qué se hizo o por qué no se pudo). */
    static String ejecutar() {
        Path raiz;
        try {
            Salida s = git(Path.of("").toAbsolutePath(), "rev-parse", "--show-toplevel");
            if (s.codigo() != 0) return "No se actualizó: la app no está dentro de la carpeta del repositorio.";
            raiz = Path.of(s.texto().trim());
        } catch (IOException e) {
            return "No se actualizó: no tienes git instalado (https://git-scm.com).";
        }

        String copia;
        try {
            copia = copiaDeSeguridad(raiz);
        } catch (IOException e) {
            return "No se actualizó: falló la copia de seguridad (" + e.getMessage() + "). Mejor no tocar nada.";
        }

        try {
            if (git(raiz, "rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}").codigo() != 0) {
                return "Copia guardada en " + copia + ". Tu rama no está enlazada con GitHub, así que no se actualizó.";
            }
            Salida fetch = git(raiz, "fetch", "--prune");
            if (fetch.codigo() != 0) return "Copia guardada. No se pudo conectar con GitHub para actualizar: " + primeraLinea(fetch.texto());
            String antes = git(raiz, "rev-parse", "HEAD").texto().trim();
            Salida pull = git(raiz, "pull", "--ff-only");
            if (pull.codigo() != 0) {
                return "Copia guardada en " + copia + ". NO se actualizó automáticamente porque tienes cambios propios que chocan "
                        + "con los de GitHub. Haz el pull a mano (commit de lo tuyo y luego git pull).";
            }
            String despues = git(raiz, "rev-parse", "HEAD").texto().trim();
            if (antes.equals(despues)) return "Tu carpeta ya estaba al día (copia guardada en " + copia + ").";
            String n = git(raiz, "rev-list", "--count", antes + ".." + despues).texto().trim();
            return "Carpeta actualizada con " + n + " commit(s) de GitHub. La copia de antes está en " + copia + ".";
        } catch (IOException | RuntimeException e) {
            return "Copia guardada en " + copia + ". Error al actualizar: " + e.getMessage();
        }
    }

    /** Copia todo el proyecto (incluido .git, para no perder commits sin subir) a la carpeta oculta. */
    private static String copiaDeSeguridad(Path raiz) throws IOException {
        Path carpeta = raiz.resolve(CARPETA_COPIAS);
        Files.createDirectories(carpeta);
        if (esWindows()) {
            try { Files.setAttribute(carpeta, "dos:hidden", true); } catch (IOException | UnsupportedOperationException ignorado) { /* no crítico */ }
        }
        // que git nunca la suba, aunque alguien toque el .gitignore
        Path excluir = raiz.resolve(".git/info/exclude");
        if (Files.isDirectory(excluir.getParent())) {
            String actual = Files.exists(excluir) ? Files.readString(excluir, StandardCharsets.UTF_8) : "";
            if (!actual.contains(CARPETA_COPIAS + "/")) {
                Files.writeString(excluir, actual + (actual.isEmpty() || actual.endsWith("\n") ? "" : "\n") + CARPETA_COPIAS + "/\n", StandardCharsets.UTF_8);
            }
        }
        Path destino = carpeta.resolve(LocalDateTime.now().format(NOMBRE));
        Path compilados = raiz.resolve("herramientas/asistencia/out");
        try (Stream<Path> todo = Files.walk(raiz)) {
            for (Path p : (Iterable<Path>) todo::iterator) {
                if (p.startsWith(carpeta) || p.startsWith(compilados)) continue;
                Path q = destino.resolve(raiz.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(q);
                else Files.copy(p, q, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
        borrarAntiguas(carpeta);
        return CARPETA_COPIAS + "/" + destino.getFileName();
    }

    private static void borrarAntiguas(Path carpeta) throws IOException {
        List<Path> copias = new ArrayList<>();
        try (Stream<Path> s = Files.list(carpeta)) { s.filter(Files::isDirectory).forEach(copias::add); }
        copias.sort(Comparator.comparing(Path::getFileName)); // el nombre es la fecha → orden cronológico
        for (int i = 0; i < copias.size() - MAX_COPIAS; i++) {
            try (Stream<Path> s = Files.walk(copias.get(i))) {
                for (Path p : (Iterable<Path>) s.sorted(Comparator.reverseOrder())::iterator) {
                    p.toFile().setWritable(true); // los objetos de .git son de solo lectura
                    Files.delete(p);
                }
            }
        }
    }

    /**
     * Guarda TODO tu trabajo (también lo que no has hecho commit ni add) en la rama copias/&lt;login&gt; de GitHub.
     * No toca tu rama, ni tu índice, ni tus archivos: usa un índice temporal y sube un commit aparte.
     * Cada copia enlaza con la anterior, así que en esa rama queda el historial de todas.
     */
    static String guardarTrabajo(String login, String token, String motivo) {
        Path indice = null;
        try {
            Salida top = git(Path.of("").toAbsolutePath(), "rev-parse", "--show-toplevel");
            if (top.codigo() != 0) return "No se guardó el trabajo: la app no está dentro del repositorio.";
            Path raiz = Path.of(top.texto().trim());
            String ref = "refs/copias/" + login;
            Salida head = git(raiz, "rev-parse", "--verify", "-q", "HEAD");
            Salida previa = git(raiz, "rev-parse", "--verify", "-q", ref);

            indice = Files.createTempFile("asistencia-indice", "");
            Files.delete(indice); // git lo crea él
            Map<String, String> env = Map.of("GIT_INDEX_FILE", indice.toString());
            if (head.codigo() == 0) git(raiz, env, "read-tree", "HEAD");
            Salida add = git(raiz, env, "add", "-A");
            if (add.codigo() != 0) return "No se guardó el trabajo: " + primeraLinea(add.texto());
            String arbol = git(raiz, env, "write-tree").texto().trim();

            if (previa.codigo() == 0 && arbol.equals(git(raiz, "rev-parse", previa.texto().trim() + "^{tree}").texto().trim())) {
                return "Trabajo ya guardado (sin cambios desde la última copia).";
            }
            List<String> args = new ArrayList<>(List.of("commit-tree", arbol));
            if (previa.codigo() == 0) args.addAll(List.of("-p", previa.texto().trim()));
            if (head.codigo() == 0 && !head.texto().trim().equals(previa.texto().trim())) args.addAll(List.of("-p", head.texto().trim()));
            args.addAll(List.of("-m", "Copia automática de " + login + " (" + motivo + ") " + LocalDateTime.now().format(NOMBRE)));
            Salida commit = git(raiz, args.toArray(new String[0]));
            if (commit.codigo() != 0) return "No se guardó el trabajo: " + primeraLinea(commit.texto());
            String sha = commit.texto().trim();
            git(raiz, "update-ref", ref, sha); // queda guardado en local aunque no haya internet

            // se sube con el token de la app, para no depender de que git tenga la contraseña guardada
            String basic = Base64.getEncoder().encodeToString(("x-access-token:" + token).getBytes(StandardCharsets.UTF_8));
            Map<String, String> auth = Map.of(
                    "GIT_CONFIG_COUNT", "1",
                    "GIT_CONFIG_KEY_0", "http.https://github.com/.extraheader",
                    "GIT_CONFIG_VALUE_0", "AUTHORIZATION: basic " + basic);
            Salida push = git(raiz, auth, "push", "--force", "origin", sha + ":refs/heads/copias/" + login);
            if (push.codigo() != 0) return "Trabajo guardado en tu PC, pero no se pudo subir a GitHub: " + primeraLinea(push.texto());
            return "Trabajo guardado en GitHub (rama copias/" + login + ").";
        } catch (IOException | RuntimeException e) {
            return "No se guardó el trabajo: " + e.getMessage();
        } finally {
            if (indice != null) try { Files.deleteIfExists(indice); } catch (IOException ignorado) { /* temporal */ }
        }
    }

    private static Salida git(Path dir, String... args) throws IOException {
        return git(dir, Map.of(), args);
    }

    private static Salida git(Path dir, Map<String, String> env, String... args) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.addAll(List.of(args));
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true);
        pb.environment().put("GIT_TERMINAL_PROMPT", "0"); // que nunca se quede esperando una contraseña
        pb.environment().putAll(env);
        Process p = pb.start();
        try {
            byte[] salida = p.getInputStream().readAllBytes();
            if (!p.waitFor(90, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IOException("git tardó demasiado");
            }
            return new Salida(p.exitValue(), new String(salida, StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrumpido");
        }
    }

    private static String primeraLinea(String s) {
        String t = s.strip();
        int i = t.indexOf('\n');
        return i < 0 ? t : t.substring(0, i);
    }

    private static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
