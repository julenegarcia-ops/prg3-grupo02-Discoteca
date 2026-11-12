package gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import domain.Entrada;
import domain.GestorEntradas;

public class JFrameEntradas extends JFrame {

	private static final long serialVersionUID = 1L;

	private GestorEntradas gestor;
	private EntradaTableModel modelo;
	private JTable tabla;

	public JFrameEntradas() {
		gestor = new GestorEntradas();

		setTitle("Venta de entradas");
		setSize(900, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana, no la app
		setLocationRelativeTo(null);

		JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));

		JLabel lblTitulo = new JLabel("VENTA DE ENTRADAS", SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
		panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

		modelo = new EntradaTableModel(gestor.getEntradas());
		tabla = new JTable(modelo);
		tabla.getTableHeader().setReorderingAllowed(false);
		panelPrincipal.add(new JScrollPane(tabla), BorderLayout.CENTER);

		JPanel panelBotones = new JPanel();
		JButton btnVender = new JButton("Registrar venta");
		JButton btnDevolver = new JButton("Devolver");
		panelBotones.add(btnVender);
		panelBotones.add(btnDevolver);
		panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

		btnVender.addActionListener(e -> {
			JDialogVenta dialogo = new JDialogVenta(this, gestor);
			dialogo.setVisible(true); // al ser modal, espera aqui hasta que se cierre
			modelo.fireTableDataChanged(); // la tabla se repinta con las entradas nuevas
		});

		btnDevolver.addActionListener(e -> devolver());

		add(panelPrincipal);
	}

	private void devolver() {
		int fila = tabla.getSelectedRow();

		if (fila == -1) {
			JOptionPane.showMessageDialog(this, "Selecciona una entrada de la tabla", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Entrada entrada = modelo.getEntrada(fila);
		int respuesta = JOptionPane.showConfirmDialog(this,
				"¿Devolver la entrada nº " + entrada.getNumero() + " de " + entrada.getNombre() + "?", "Devolver",
				JOptionPane.YES_NO_OPTION);

		if (respuesta == JOptionPane.YES_OPTION) {
			gestor.devolver(entrada);
			modelo.fireTableDataChanged();
		}
	}
}
