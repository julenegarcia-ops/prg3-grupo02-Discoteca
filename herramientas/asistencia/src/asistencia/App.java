package asistencia;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Punto de entrada: configura, conecta con GitHub y arranca la sesión. */
public final class App {
    private static FileLock candado; // se mantiene mientras la app está abierta

    public static void main(String[] args) throws Exception {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignorado) { /* aspecto por defecto */ }
        Files.createDirectories(Config.DIR);
        candado = FileChannel.open(Config.DIR.resolve("app.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE).tryLock();
        if (candado == null) {
            JOptionPane.showMessageDialog(null, "La app de asistencia ya está abierta (mira junto al reloj de la barra de tareas).");
            System.exit(0);
        }

        String actualizacion = Actualizador.ejecutar(); // copia de seguridad + git pull, antes de nada

        Config config = Config.cargar();
        GitHub gh;
        String login;
        Chat chat;
        while (true) {
            if (config.token().isEmpty() && !pedirConfig(config, null)) System.exit(0);
            try {
                gh = new GitHub(config.repo(), config.token());
                login = gh.login();
                gh.asegurarRama();
                chat = new Chat(gh);
                break;
            } catch (Exception e) {
                if (!pedirConfig(config, e.getMessage())) System.exit(0);
            }
        }

        Jarvis jarvis = new Jarvis(config);
        Asistencia asistencia = new Asistencia(gh, jarvis, login, config);
        String token = config.token();
        String yo = login;
        asistencia.guardarTrabajo(motivo -> Actualizador.guardarTrabajo(yo, token, motivo));
        asistencia.recuperarSesionPerdida();
        asistencia.empezar(); // abrir la app = empezar a trabajar

        GitHub ghFinal = gh;
        Chat chatFinal = chat;
        SwingUtilities.invokeLater(() -> arrancarInterfaz(asistencia, chatFinal, jarvis, ghFinal, actualizacion));
    }

    private static void arrancarInterfaz(Asistencia asistencia, Chat chat, Jarvis jarvis, GitHub gh, String actualizacion) {
        Ventana v = new Ventana(asistencia, chat, jarvis);
        v.mostrarActualizacion(actualizacion);
        Runnable salir = () -> {
            v.setVisible(false);
            asistencia.apagar();
            System.exit(0);
        };

        if (SystemTray.isSupported()) {
            TrayIcon icono = new TrayIcon(iconoBandeja(), "Asistencia · Discoteca");
            icono.setImageAutoSize(true);
            PopupMenu menu = new PopupMenu();
            MenuItem abrir = new MenuItem("Abrir");
            abrir.addActionListener(e -> mostrar(v));
            MenuItem cerrar = new MenuItem("Salir (termina la sesión)");
            cerrar.addActionListener(e -> salir.run());
            menu.add(abrir);
            menu.addSeparator();
            menu.add(cerrar);
            icono.setPopupMenu(menu);
            icono.addActionListener(e -> mostrar(v));
            try {
                SystemTray.getSystemTray().add(icono);
                v.notificador(t -> Ventana.bandeja(icono, t));
                v.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                v.addWindowListener(new WindowAdapter() {
                    private boolean avisado;
                    @Override public void windowClosing(WindowEvent e) {
                        if (!avisado) Ventana.bandeja(icono, "Sigo funcionando aquí. Para salir: clic derecho → Salir.");
                        avisado = true;
                    }
                });
            } catch (AWTException e) {
                cerrarSale(v, salir);
            }
        } else {
            cerrarSale(v, salir);
        }
        // por si apagan el PC con la app abierta
        Runtime.getRuntime().addShutdownHook(new Thread(asistencia::apagar));
        v.setVisible(true);
    }

    private static void cerrarSale(JFrame v, Runnable salir) {
        v.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        v.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir.run(); }
        });
    }

    private static void mostrar(JFrame v) {
        v.setVisible(true);
        v.setExtendedState(JFrame.NORMAL);
        v.toFront();
    }

    /** Diálogo de primera vez (o si el token falla). Devuelve false si cancelan. */
    private static boolean pedirConfig(Config config, String error) {
        JPasswordField token = new JPasswordField(config.token(), 32);
        JTextField repo = new JTextField(config.repo(), 32);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        if (error != null) {
            JLabel e = new JLabel("Error: " + error);
            e.setForeground(new Color(0xC0392B));
            p.add(e, c);
        }
        p.add(new JLabel("<html>Necesitas un <b>token de GitHub</b> (clásico): GitHub → Settings → Developer settings →<br>"
                + "Personal access tokens → Tokens (classic) → Generate new token.<br>"
                + "Marca los permisos <b>repo</b> y <b>user</b> (user sirve para cambiar tu estado de GitHub).<br>"
                + "Se guarda solo en este PC: " + Config.fichero() + "</html>"), c);
        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Token:"), c);
        c.gridx = 1; p.add(token, c);
        c.gridy = 3; c.gridx = 0; p.add(new JLabel("Repositorio:"), c);
        c.gridx = 1; p.add(repo, c);
        int r = JOptionPane.showConfirmDialog(null, p, "Asistencia · Discoteca — configuración",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return false;
        config.token(new String(token.getPassword()));
        config.repo(repo.getText().isBlank() ? Config.REPO_POR_DEFECTO : repo.getText());
        try {
            config.guardar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo guardar la configuración: " + e.getMessage());
        }
        return true;
    }

    private static BufferedImage iconoBandeja() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x7B2FF7));
        g.fillOval(1, 1, 30, 30);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3f));
        g.drawPolyline(new int[]{9, 14, 23}, new int[]{16, 21, 11}, 3);
        g.dispose();
        return img;
    }
}
