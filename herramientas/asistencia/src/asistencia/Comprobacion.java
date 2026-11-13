package asistencia;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.time.Duration;
import java.time.Instant;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/** Ventana emergente «¿Sigues ahí?»: siempre encima, con cuenta atrás. */
final class Comprobacion extends JDialog {
    private final Instant limite;
    private final JLabel cuenta = new JLabel("", SwingConstants.CENTER);
    private final Timer reloj;
    private boolean cerrada;

    /**
     * @param alResponder se ejecuta si la persona pulsa el botón a tiempo
     * @param alCaducar   se ejecuta si se acaba el tiempo sin respuesta
     */
    Comprobacion(Duration gracia, Runnable alResponder, Runnable alCaducar) {
        super((Frame) null, "¿Sigues ahí? · Discoteca", false);
        this.limite = Instant.now().plus(gracia);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JLabel pregunta = new JLabel("¿Sigues trabajando en el proyecto?", SwingConstants.CENTER);
        pregunta.setFont(pregunta.getFont().deriveFont(Font.BOLD, 18f));
        cuenta.setFont(cuenta.getFont().deriveFont(14f));
        JButton si = new JButton("Sí, sigo aquí");
        si.setFont(si.getFont().deriveFont(Font.BOLD, 16f));
        si.addActionListener(e -> { cerrar(); alResponder.run(); });

        JPanel p = new JPanel(new BorderLayout(10, 14));
        p.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        p.add(pregunta, BorderLayout.NORTH);
        p.add(cuenta, BorderLayout.CENTER);
        p.add(si, BorderLayout.SOUTH);
        setContentPane(p);
        getRootPane().setDefaultButton(si);
        pack();
        setLocationRelativeTo(null);

        reloj = new Timer(1000, e -> {
            long s = Duration.between(Instant.now(), limite).getSeconds();
            if (s <= 0) { cerrar(); alCaducar.run(); return; }
            cuenta.setText(String.format("Si no respondes en %d:%02d te marcaremos como inactivo.", s / 60, s % 60));
            if (s % 60 == 0) { Toolkit.getDefaultToolkit().beep(); toFront(); } // insiste cada minuto
        });
        reloj.setInitialDelay(0);
    }

    void mostrar() {
        reloj.start();
        setVisible(true);
        toFront();
        requestFocus();
        Toolkit.getDefaultToolkit().beep();
    }

    boolean cerrada() { return cerrada; }

    private void cerrar() {
        cerrada = true;
        reloj.stop();
        dispose();
    }
}
