package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import domain.Canal;
import domain.Entrada;
import domain.Evento;
import domain.GestorEntradas;
import domain.TipoEntrada;

public class JDialogVenta extends JDialog {

	private static final long serialVersionUID = 1L;

	private GestorEntradas gestor;
	private JComboBox<Evento> comboEvento;
	private JComboBox<TipoEntrada> comboTipo;
	private JSpinner spinnerCantidad;
	private JTextField txtNombre;
	private JTextField txtDni;
	private JLabel lblPrecio;

	public JDialogVenta(JFrame padre, GestorEntradas gestor) {
		super(padre, "Registrar venta", true); // modal: bloquea la ventana de atras
		this.gestor = gestor;

		setSize(400, 300);
		setLocationRelativeTo(padre);
		setLayout(new BorderLayout(10, 10));

		comboEvento = new JComboBox<>();
		for (Evento ev : gestor.getEventos()) {
			comboEvento.addItem(ev);
		}
		comboTipo = new JComboBox<>(TipoEntrada.values());
		spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		txtNombre = new JTextField();
		txtDni = new JTextField();
		lblPrecio = new JLabel();
		lblPrecio.setFont(new Font("Arial", Font.BOLD, 16));

		JPanel panelForm = new JPanel(new GridLayout(6, 2, 5, 5));
		panelForm.add(new JLabel("Evento:"));
		panelForm.add(comboEvento);
		panelForm.add(new JLabel("Tipo:"));
		panelForm.add(comboTipo);
		panelForm.add(new JLabel("Cantidad:"));
		panelForm.add(spinnerCantidad);
		panelForm.add(new JLabel("Nombre:"));
		panelForm.add(txtNombre);
		panelForm.add(new JLabel("DNI:"));
		panelForm.add(txtDni);
		panelForm.add(new JLabel("Total:"));
		panelForm.add(lblPrecio);
		add(panelForm, BorderLayout.CENTER);

		JPanel panelBotones = new JPanel();
		JButton btnVender = new JButton("Vender");
		JButton btnCancelar = new JButton("Cancelar");
		panelBotones.add(btnVender);
		panelBotones.add(btnCancelar);
		add(panelBotones, BorderLayout.SOUTH);

		// precio en vivo: se recalcula cada vez que cambia algo
		comboEvento.addActionListener(e -> actualizarPrecio());
		comboTipo.addActionListener(e -> actualizarPrecio());
		spinnerCantidad.addChangeListener(e -> actualizarPrecio());

		btnVender.addActionListener(e -> vender());
		btnCancelar.addActionListener(e -> dispose());

		actualizarPrecio();
	}

	private void actualizarPrecio() {
		Evento evento = (Evento) comboEvento.getSelectedItem();
		TipoEntrada tipo = (TipoEntrada) comboTipo.getSelectedItem();
		int cantidad = (Integer) spinnerCantidad.getValue();

		lblPrecio.setText(gestor.calcularPrecio(evento, tipo) * cantidad + " €");
	}

	// 8 numeros y una letra
	private boolean dniValido(String dni) {
		if (dni.length() != 9) {
			return false;
		}
		for (int i = 0; i < 8; i++) {
			if (!Character.isDigit(dni.charAt(i))) {
				return false;
			}
		}
		return Character.isLetter(dni.charAt(8));
	}

	private void vender() {
		Evento evento = (Evento) comboEvento.getSelectedItem();
		TipoEntrada tipo = (TipoEntrada) comboTipo.getSelectedItem();
		int cantidad = (Integer) spinnerCantidad.getValue();
		String nombre = txtNombre.getText().trim();
		String dni = txtDni.getText().trim().toUpperCase();

		if (nombre.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Escribe el nombre", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!dniValido(dni)) {
			JOptionPane.showMessageDialog(this, "DNI mal escrito (ej: 12345678A)", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (gestor.plazasLibres(evento) < cantidad) {
			JOptionPane.showMessageDialog(this, "Solo quedan " + gestor.plazasLibres(evento) + " plazas", "Sin plazas",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// una entrada por persona
		for (int i = 0; i < cantidad; i++) {
			Entrada vendida = gestor.vender(evento, tipo, Canal.TAQUILLA, nombre, dni);
			if (vendida == null) {
				JOptionPane.showMessageDialog(this, "Se han acabado las plazas", "Sin plazas",
						JOptionPane.WARNING_MESSAGE);
				break;
			}
		}

		dispose();
	}
}
