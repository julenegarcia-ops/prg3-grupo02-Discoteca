package asistencia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TrayIcon;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Ventana principal: estado de tu sesión, el equipo y el chat. */
final class Ventana extends JFrame {
    private static final Color VERDE = new Color(0x1E9E4A);
    private static final Color GRIS = new Color(0x888888);
    private static final Color NARANJA = new Color(0xD9822B);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final Asistencia asistencia;
    private final Chat chat;
    private final Jarvis jarvis;
    private final ExecutorService fondo = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "asistencia-ui");
        t.setDaemon(true);
        return t;
    });

    private final JLabel estado = new JLabel();
    private final JLabel detalle = new JLabel();
    private final JLabel error = new JLabel();
    private final JButton boton = new JButton();
    private final DefaultTableModel modeloEquipo = new DefaultTableModel(
            new Object[]{"Miembro", "Estado", "Última señal", "Hoy", "Esta semana", "Total"}, 0) {
        @Override public boolean isCellEditable(int f, int c) { return false; }
    };
    private final JTextArea mensajes = new JTextArea();
    private final JTextField escribir = new JTextField();
    private final JTabbedPane pestanas = new JTabbedPane();
    private final AtomicBoolean leyendoChat = new AtomicBoolean();
    private final AtomicBoolean leyendoEquipo = new AtomicBoolean();
    private Comprobacion comprobacion;
    private Consumer<String> notificar = t -> {};
    private int sinLeer;
    private long ultimoChat;
    private long ultimoEquipo;

    Ventana(Asistencia asistencia, Chat chat, Jarvis jarvis) {
        super("Asistencia · Discoteca (" + asistencia.login + ")");
        this.asistencia = asistencia;
        this.chat = chat;
        this.jarvis = jarvis;
        setSize(760, 520);
        setLocationRelativeTo(null);

        // --- cabecera ---
        estado.setFont(estado.getFont().deriveFont(Font.BOLD, 16f));
        error.setForeground(new Color(0xC0392B));
        boton.addActionListener(e -> {
            if (asistencia.enSesion()) {
                cerrarComprobacion();
                asistencia.terminar("terminó manualmente");
            } else {
                asistencia.empezar();
            }
            refrescarCabecera();
        });
        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.add(estado, BorderLayout.NORTH);
        textos.add(detalle, BorderLayout.CENTER);
        textos.add(error, BorderLayout.SOUTH);
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(boton, BorderLayout.EAST);

        // --- equipo ---
        JTable tabla = new JTable(modeloEquipo);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
                super.getTableCellRendererComponent(t, v, sel, foco, f, c);
                String s = String.valueOf(v);
                if (!sel) setForeground(s.startsWith("● Trabajando") ? VERDE : s.startsWith("● Desconectado") ? NARANJA : GRIS);
                return this;
            }
        });
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> leerEquipo());
        JButton datos = new JButton("Ver registro en GitHub");
        datos.addActionListener(e -> abrir(chat.urlWeb().replaceAll("/issues/\\d+$", "") + "/tree/" + GitHub.RAMA + "/registro"));
        JPanel pieEquipo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pieEquipo.add(datos);
        pieEquipo.add(actualizar);
        JPanel equipo = new JPanel(new BorderLayout());
        equipo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        equipo.add(pieEquipo, BorderLayout.SOUTH);

        // --- chat ---
        mensajes.setEditable(false);
        mensajes.setLineWrap(true);
        mensajes.setWrapStyleWord(true);
        mensajes.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JButton enviar = new JButton("Enviar");
        enviar.addActionListener(e -> enviar());
        escribir.addActionListener(e -> enviar());
        escribir.setToolTipText("Menciona con @usuario y GitHub le avisará");
        JButton web = new JButton("Abrir en GitHub");
        web.addActionListener(e -> abrir(chat.urlWeb()));
        JPanel barra = new JPanel(new BorderLayout(6, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        barra.add(escribir, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        botones.add(enviar);
        botones.add(web);
        barra.add(botones, BorderLayout.EAST);
        JPanel panelChat = new JPanel(new BorderLayout());
        panelChat.add(new JScrollPane(mensajes), BorderLayout.CENTER);
        panelChat.add(barra, BorderLayout.SOUTH);

        pestanas.addTab("Equipo", equipo);
        pestanas.addTab("Chat", panelChat);
        pestanas.addChangeListener(e -> { if (pestanas.getSelectedIndex() == 1) { sinLeer = 0; tituloChat(); } });

        add(cabecera, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        asistencia.alCambiar(err -> SwingUtilities.invokeLater(this::refrescarCabecera));

        // reloj principal: cabecera, comprobaciones y sondeo de red
        new Timer(1000, e -> latido()).start();
        refrescarCabecera();
        leerEquipo();
        leerChat();
    }

    /** Resultado de la copia de seguridad + actualización del arranque, en la parte de abajo de la ventana. */
    void mostrarActualizacion(String texto) {
        JLabel l = new JLabel(texto);
        l.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));
        l.setForeground(texto.contains("NO se actualizó") || texto.startsWith("No se") ? new Color(0xC0392B) : GRIS);
        add(l, BorderLayout.SOUTH);
        if (texto.contains("NO se actualizó")) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, texto, "Actualización", JOptionPane.WARNING_MESSAGE));
        }
    }

    /** Cómo avisar fuera de la ventana (bandeja del sistema). */
    void notificador(Consumer<String> n) { this.notificar = n; }

    private void latido() {
        refrescarCabecera();
        Instant proxima = asistencia.proximaComprobacion();
        if (proxima != null && !Instant.now().isBefore(proxima) && (comprobacion == null || comprobacion.cerrada())) {
            comprobacion = new Comprobacion(asistencia.gracia, asistencia::confirmar, () -> {
                asistencia.terminar(Asistencia.SIN_RESPUESTA);
                notificar.accept("Te hemos marcado como inactivo. Pulsa «Empezar a trabajar» cuando vuelvas.");
                JOptionPane.showMessageDialog(this, "No respondiste a tiempo: te hemos marcado como inactivo.\n"
                        + "Tu sesión se ha guardado hasta la última vez que confirmaste.\n\n"
                        + "Pulsa «Empezar a trabajar» cuando vuelvas.", "Inactivo", JOptionPane.INFORMATION_MESSAGE);
            });
            comprobacion.mostrar();
        }
        long ahora = System.currentTimeMillis();
        boolean mirandoChat = isActive() && pestanas.getSelectedIndex() == 1;
        if (ahora - ultimoChat > (mirandoChat ? 4_000 : 20_000)) leerChat();
        if (ahora - ultimoEquipo > 60_000) leerEquipo();
    }

    private void cerrarComprobacion() {
        if (comprobacion != null && !comprobacion.cerrada()) comprobacion.dispose();
        comprobacion = null;
    }

    private void refrescarCabecera() {
        if (asistencia.enSesion()) {
            estado.setText("● Trabajando");
            estado.setForeground(VERDE);
            Duration sesion = Duration.between(asistencia.inicio(), Instant.now());
            long faltan = Math.max(0, Duration.between(Instant.now(), asistencia.proximaComprobacion()).toMinutes());
            detalle.setText("Sesión actual: " + Asistencia.formato(sesion) + " · próxima comprobación en " + faltan + " min");
            boton.setText("Terminar sesión");
        } else {
            estado.setText("● Inactivo");
            estado.setForeground(GRIS);
            String g = asistencia.ultimoGuardado();
            detalle.setText("No se está contando tu tiempo." + (g == null ? "" : " " + g));
            boton.setText("Empezar a trabajar");
        }
        String err = asistencia.ultimoError();
        error.setText(err == null ? " " : err);
    }

    // ---------- equipo ----------

    private void leerEquipo() {
        ultimoEquipo = System.currentTimeMillis();
        if (!leyendoEquipo.compareAndSet(false, true)) return;
        fondo.submit(() -> {
            try {
                List<Asistencia.Miembro> lista = asistencia.equipo();
                SwingUtilities.invokeLater(() -> pintarEquipo(lista));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> error.setText("No se pudo leer el equipo: " + e.getMessage()));
            } finally {
                leyendoEquipo.set(false);
            }
        });
    }

    private void pintarEquipo(List<Asistencia.Miembro> lista) {
        modeloEquipo.setRowCount(0);
        for (Asistencia.Miembro m : lista) {
            String est = switch (m.estado()) {
                case "activo" -> "● Trabajando" + (m.sesionInicio() == null ? "" : " desde " + hora(m.sesionInicio()));
                case "desconectado" -> "● Desconectado";
                default -> "● Inactivo";
            };
            modeloEquipo.addRow(new Object[]{m.login(), est, m.actualizado() == null ? "-" : diaHora(m.actualizado()),
                    Asistencia.formato(m.hoy()), Asistencia.formato(m.semana()), Asistencia.formato(m.total())});
        }
    }

    // ---------- chat ----------

    private void leerChat() {
        ultimoChat = System.currentTimeMillis();
        if (!leyendoChat.compareAndSet(false, true)) return;
        fondo.submit(() -> {
            try {
                List<Chat.Mensaje> nuevos = chat.nuevos();
                if (!nuevos.isEmpty()) SwingUtilities.invokeLater(() -> pintarMensajes(nuevos));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> error.setText("No se pudo leer el chat: " + e.getMessage()));
            } finally {
                leyendoChat.set(false);
            }
        });
    }

    private boolean primeraCarga = true;

    private void pintarMensajes(List<Chat.Mensaje> nuevos) {
        String yo = asistencia.login;
        for (Chat.Mensaje m : nuevos) {
            mensajes.append("[" + diaHora(m.fecha()) + "] " + m.autor() + ": " + m.texto() + "\n");
            boolean mio = m.autor().equalsIgnoreCase(yo);
            if (primeraCarga || mio) continue;
            boolean mencion = m.texto().toLowerCase().contains("@" + yo.toLowerCase());
            if (!isActive() || pestanas.getSelectedIndex() != 1) {
                sinLeer++;
                notificar.accept(m.autor() + ": " + recorta(m.texto(), 120));
            }
            if (mencion) jarvis.avisar("Discoteca: " + m.autor() + " te ha mencionado", m.autor() + " escribió en el chat del grupo: " + recorta(m.texto(), 500));
        }
        primeraCarga = false;
        mensajes.setCaretPosition(mensajes.getDocument().getLength());
        tituloChat();
    }

    private void tituloChat() {
        pestanas.setTitleAt(1, sinLeer == 0 ? "Chat" : "Chat (" + sinLeer + ")");
    }

    private void enviar() {
        String texto = escribir.getText().trim();
        if (texto.isEmpty()) return;
        escribir.setText("");
        escribir.setEnabled(false);
        fondo.submit(() -> {
            try {
                chat.enviar(texto);
                SwingUtilities.invokeLater(this::leerChat);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    escribir.setText(texto);
                    error.setText("No se pudo enviar: " + e.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> { escribir.setEnabled(true); escribir.requestFocusInWindow(); });
            }
        });
    }

    // ---------- utilidades ----------

    private static String hora(Instant i) { return HORA.format(i.atZone(ZoneId.systemDefault())); }

    private static String diaHora(Instant i) {
        var z = i.atZone(ZoneId.systemDefault());
        return z.toLocalDate().equals(LocalDate.now()) ? "hoy " + HORA.format(z) : DIA_HORA.format(z);
    }

    private static String recorta(String s, int n) {
        String una = s.replace('\n', ' ');
        return una.length() <= n ? una : una.substring(0, n) + "…";
    }

    private void abrir(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, url, "Ábrelo en el navegador", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    static void bandeja(TrayIcon icono, String texto) {
        icono.displayMessage("Asistencia · Discoteca", texto, TrayIcon.MessageType.INFO);
    }
}
