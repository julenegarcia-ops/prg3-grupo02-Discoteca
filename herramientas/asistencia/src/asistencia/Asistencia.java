package asistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Lógica de presencia y registro de horas.
 *
 * Una sesión empieza al abrir la app (o al pulsar «Empezar») y termina al cerrarla, al pulsar «Terminar»
 * o al no responder a la comprobación. Si no respondes, la sesión se cierra en el momento de tu
 * ÚLTIMA confirmación: el tiempo sin confirmar no cuenta.
 */
final class Asistencia {
    static final String SIN_RESPUESTA = "sin respuesta";
    private static final Path SESION = Config.DIR.resolve("sesion-abierta.properties");
    private static final Path PENDIENTES = Config.DIR.resolve("pendientes.csv");
    private static final Path COPIA_LOCAL = Config.DIR.resolve("registro-local.csv");
    private static final String CABECERA = "inicio,fin,minutos,motivo\n";

    record Miembro(String login, String estado, Instant actualizado, Instant sesionInicio,
                   Duration hoy, Duration semana, Duration total) {}

    final String login;
    final Duration intervalo;
    final Duration gracia;
    private final GitHub gh;
    private final Jarvis jarvis;
    private final ExecutorService red = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "asistencia-red");
        t.setDaemon(true);
        return t;
    });
    private volatile Instant inicio;
    private volatile Instant ultimaConfirmacion;
    private volatile String ultimoError;
    private volatile String ultimoGuardado;
    private UnaryOperator<String> guardarTrabajo = motivo -> null;
    private Consumer<String> alCambiar = e -> {};

    Asistencia(GitHub gh, Jarvis jarvis, String login, Config c) {
        this.gh = gh;
        this.jarvis = jarvis;
        this.login = login;
        this.intervalo = Duration.ofMillis(Math.round(c.intervaloMinutos() * 60_000));
        this.gracia = Duration.ofMillis(Math.round(c.graciaMinutos() * 60_000));
    }

    /** Se llama (desde cualquier hilo) cuando cambia el estado o hay un error de red; null = sin error. */
    void alCambiar(Consumer<String> oyente) { this.alCambiar = oyente; }

    boolean enSesion() { return inicio != null; }
    Instant inicio() { return inicio; }
    Instant proximaComprobacion() { Instant u = ultimaConfirmacion; return u == null ? null : u.plus(intervalo); }
    String ultimoError() { return ultimoError; }
    String ultimoGuardado() { return ultimoGuardado; }

    /** Qué hacer para guardar el trabajo al terminar una sesión (recibe el motivo, devuelve un mensaje). */
    void guardarTrabajo(UnaryOperator<String> g) { this.guardarTrabajo = g; }

    // ---------- ciclo de la sesión ----------

    /** Si la app se cerró de golpe (apagón, matar proceso...), registra esa sesión hasta su última confirmación. */
    void recuperarSesionPerdida() {
        if (!Files.exists(SESION)) return;
        try {
            Properties p = new Properties();
            try (var r = Files.newBufferedReader(SESION, StandardCharsets.UTF_8)) { p.load(r); }
            Files.deleteIfExists(SESION);
            registrar(Instant.parse(p.getProperty("inicio")), Instant.parse(p.getProperty("ultimaConfirmacion")), "cierre inesperado");
        } catch (Exception e) {
            ultimoError = "No se pudo recuperar la sesión anterior: " + e.getMessage();
        }
        subirPendientes();
    }

    synchronized void empezar() {
        if (enSesion()) return;
        inicio = ultimaConfirmacion = Instant.now();
        guardarSesionLocal();
        publicarPresencia();
        estadoPerfil(true);
        avisar();
    }

    synchronized void confirmar() {
        if (!enSesion()) return;
        ultimaConfirmacion = Instant.now();
        guardarSesionLocal();
        publicarPresencia();
        subirPendientes();
        avisar();
    }

    synchronized void terminar(String motivo) {
        if (!enSesion()) return;
        Instant ini = inicio;
        Instant fin = SIN_RESPUESTA.equals(motivo) ? ultimaConfirmacion : Instant.now();
        inicio = ultimaConfirmacion = null;
        try { Files.deleteIfExists(SESION); } catch (IOException ignorado) { /* se reintentará */ }
        registrar(ini, fin, motivo);
        subirPendientes();
        red.submit(() -> { ultimoGuardado = guardarTrabajo.apply(motivo); avisar(); });
        publicarPresencia();
        estadoPerfil(false);
        if (SIN_RESPUESTA.equals(motivo)) {
            jarvis.avisar("Discoteca: te han marcado inactivo",
                    "No respondiste a la comprobación de asistencia del proyecto Discoteca. Sesión registrada: "
                            + formato(Duration.between(ini, fin)) + ".");
        }
        avisar();
    }

    /** Cierra la sesión y espera (máx. 60 s) a que se suba todo, trabajo incluido, antes de salir. */
    void apagar() {
        terminar("cerró la app");
        red.shutdown();
        try { red.awaitTermination(60, TimeUnit.SECONDS); } catch (InterruptedException ignorado) { Thread.currentThread().interrupt(); }
    }

    // ---------- persistencia ----------

    private void guardarSesionLocal() {
        try {
            Files.createDirectories(Config.DIR);
            Files.writeString(SESION, "inicio=" + inicio + "\nultimaConfirmacion=" + ultimaConfirmacion + "\n", StandardCharsets.UTF_8);
        } catch (IOException ignorado) { /* sin copia local no se pierde nada salvo en un cierre inesperado */ }
    }

    private void registrar(Instant ini, Instant fin, String motivo) {
        long minutos = Duration.between(ini, fin).toMinutes();
        if (minutos < 1) return;
        String linea = local(ini) + "," + local(fin) + "," + minutos + "," + motivo.replace(",", " ") + "\n";
        try {
            Files.createDirectories(Config.DIR);
            synchronized (PENDIENTES) {
                Files.writeString(PENDIENTES, linea, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            Files.writeString(COPIA_LOCAL, linea, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            ultimoError = "No se pudo guardar el registro local: " + e.getMessage();
        }
    }

    /** Sube al CSV de GitHub las sesiones que aún no se han podido subir (p. ej. por estar sin internet). */
    private void subirPendientes() {
        red.submit(() -> {
            synchronized (PENDIENTES) {
                try {
                    if (!Files.exists(PENDIENTES)) return;
                    String nuevas = Files.readString(PENDIENTES, StandardCharsets.UTF_8);
                    if (nuevas.isBlank()) return;
                    reintentar(() -> {
                        String ruta = "registro/" + login + ".csv";
                        GitHub.Archivo a = gh.leer(ruta);
                        String previo = a == null ? CABECERA : a.contenido();
                        if (!previo.endsWith("\n")) previo += "\n";
                        gh.escribir(ruta, previo + nuevas, a == null ? null : a.sha(), "Asistencia: sesión de " + login);
                    });
                    Files.delete(PENDIENTES);
                    ok();
                } catch (Exception e) {
                    fallo(e);
                }
            }
        });
    }

    private void publicarPresencia() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("login", login);
        datos.put("estado", enSesion() ? "activo" : "inactivo");
        datos.put("actualizado", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        datos.put("sesionInicio", inicio == null ? null : inicio.truncatedTo(ChronoUnit.SECONDS).toString());
        red.submit(() -> {
            try {
                reintentar(() -> {
                    String ruta = "presencia/" + login + ".json";
                    GitHub.Archivo a = gh.leer(ruta);
                    gh.escribir(ruta, Json.write(datos) + "\n", a == null ? null : a.sha(),
                            "Asistencia: " + login + " " + datos.get("estado"));
                });
                ok();
            } catch (Exception e) {
                fallo(e);
            }
        });
    }

    private void estadoPerfil(boolean activo) {
        red.submit(() -> {
            try {
                if (activo) gh.estadoPerfil(":green_circle:", "Trabajando en " + gh.repo);
                else gh.estadoPerfil(":zzz:", "Inactivo");
            } catch (Exception ignorado) {
                // el token no tiene permiso «user»: la presencia sigue funcionando en la app
            }
        });
    }

    private interface Paso { void run() throws Exception; }

    /** Un reintento si GitHub responde 409/422 (dos escrituras seguidas sobre el mismo archivo). */
    private static void reintentar(Paso p) throws Exception {
        try {
            p.run();
        } catch (GitHub.Fallo f) {
            if (f.status != 409 && f.status != 422) throw f;
            Thread.sleep(1500);
            p.run();
        }
    }

    private void ok() {
        if (ultimoError != null) { ultimoError = null; avisar(); }
    }

    private void fallo(Exception e) {
        ultimoError = "Sin conexión con GitHub (" + e.getMessage() + "). Se reintentará.";
        avisar();
    }

    private void avisar() { alCambiar.accept(ultimoError); }

    // ---------- resumen del equipo (llamar fuera del hilo de la interfaz) ----------

    List<Miembro> equipo() throws Exception {
        Map<String, Map<String, Object>> presencias = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String nombre : gh.listar("presencia")) {
            if (!nombre.endsWith(".json")) continue;
            GitHub.Archivo a = gh.leer("presencia/" + nombre);
            if (a != null) presencias.put(nombre.substring(0, nombre.length() - 5), Json.obj(Json.parse(a.contenido())));
        }
        Map<String, String> registros = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String nombre : gh.listar("registro")) {
            if (!nombre.endsWith(".csv")) continue;
            GitHub.Archivo a = gh.leer("registro/" + nombre);
            if (a != null) registros.put(nombre.substring(0, nombre.length() - 4), a.contenido());
        }
        TreeMap<String, Boolean> logins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        presencias.keySet().forEach(l -> logins.put(l, true));
        registros.keySet().forEach(l -> logins.put(l, true));

        Instant ahora = Instant.now();
        LocalDate hoy = LocalDate.now();
        WeekFields semanaEs = WeekFields.of(java.util.Locale.forLanguageTag("es-ES"));
        LocalDate lunes = hoy.with(semanaEs.dayOfWeek(), 1);
        Duration caducidad = intervalo.plus(gracia).plusMinutes(3);

        List<Miembro> lista = new ArrayList<>();
        for (String l : logins.keySet()) {
            Duration dHoy = Duration.ZERO, dSemana = Duration.ZERO, dTotal = Duration.ZERO;
            for (String linea : registros.getOrDefault(l, "").split("\n")) {
                String[] c = linea.split(",");
                if (c.length < 3 || c[0].equals("inicio")) continue;
                try {
                    LocalDate dia = OffsetDateTime.parse(c[0]).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
                    Duration d = Duration.ofMinutes(Long.parseLong(c[2].trim()));
                    dTotal = dTotal.plus(d);
                    if (!dia.isBefore(lunes)) dSemana = dSemana.plus(d);
                    if (dia.equals(hoy)) dHoy = dHoy.plus(d);
                } catch (RuntimeException ignorado) { /* línea mal formada */ }
            }
            Map<String, Object> p = presencias.getOrDefault(l, Map.of());
            Instant actualizado = instante(Json.str(p, "actualizado"));
            Instant sesion = instante(Json.str(p, "sesionInicio"));
            String estado = "activo".equals(Json.str(p, "estado")) ? "activo" : "inactivo";
            if (estado.equals("activo") && (actualizado == null || actualizado.plus(caducidad).isBefore(ahora))) {
                estado = "desconectado"; // dejó de confirmar sin cerrar la app (sin internet, apagón...)
            }
            if (estado.equals("activo") && sesion != null) {
                // la sesión en curso cuenta hasta su última confirmación
                Duration enCurso = Duration.between(sesion, actualizado);
                dTotal = dTotal.plus(enCurso);
                dSemana = dSemana.plus(enCurso);
                dHoy = dHoy.plus(enCurso);
            }
            lista.add(new Miembro(l, estado, actualizado, sesion, dHoy, dSemana, dTotal));
        }
        return lista;
    }

    private static Instant instante(String s) {
        try { return s == null ? null : Instant.parse(s); } catch (RuntimeException e) { return null; }
    }

    private static String local(Instant i) {
        return OffsetDateTime.ofInstant(i.truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    static String formato(Duration d) {
        long min = Math.max(0, d.toMinutes());
        return min < 60 ? min + " min" : (min / 60) + " h " + String.format("%02d", min % 60) + " min";
    }
}
