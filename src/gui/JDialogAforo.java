package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import domain.Evento;
import domain.GestorEntradas;

public class JDialogAforo extends JDialog {

	private static final long serialVersionUID = 1L;

	public JDialogAforo(JFrame padre, GestorEntradas gestor) {
		super(padre, "Aforo", true);

		setSize(450, 250);
		setLocationRelativeTo(padre);
		setLayout(new BorderLayout(10, 10));

		JPanel panelBarras = new JPanel(new GridLayout(gestor.getEventos().size(), 2, 10, 10));

		for (Evento ev : gestor.getEventos()) {
			int vendidas = ev.getAforo() - gestor.plazasLibres(ev);

			JProgressBar barra = new JProgressBar(0, ev.getAforo());
			barra.setValue(vendidas);
			barra.setStringPainted(true);

			if (vendidas == ev.getAforo()) {
				barra.setString("SOLD OUT");
				barra.setForeground(Color.RED);
			} else {
				barra.setString(vendidas + "/" + ev.getAforo());
				// menos de la mitad verde, hasta el 80% naranja, mas rojo
				if (vendidas * 100 / ev.getAforo() < 50) {
					barra.setForeground(new Color(0, 150, 0));
				} else if (vendidas * 100 / ev.getAforo() < 80) {
					barra.setForeground(Color.ORANGE);
				} else {
					barra.setForeground(Color.RED);
				}
			}

			panelBarras.add(new JLabel(ev.toString()));
			panelBarras.add(barra);
		}
		add(panelBarras, BorderLayout.CENTER);

		JPanel panelBotones = new JPanel();
		JButton btnCerrar = new JButton("Cerrar");
		btnCerrar.addActionListener(e -> dispose());
		panelBotones.add(btnCerrar);
		add(panelBotones, BorderLayout.SOUTH);
	}
}
