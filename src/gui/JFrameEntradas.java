package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

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
	private JLabel lblWeb;
	private List<HiloComprador> compradores = new ArrayList<>();
	private boolean avisoSoldOutDado = false;

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
		JButton btnAforo = new JButton("Ver aforo");
		JButton btnAbrirWeb = new JButton("Abrir web");
		JButton btnCerrarWeb = new JButton("Cerrar web");
		lblWeb = new JLabel("Web: cerrada");
		panelBotones.add(btnVender);
		panelBotones.add(btnDevolver);
		panelBotones.add(btnAforo);
		panelBotones.add(btnAbrirWeb);
		panelBotones.add(btnCerrarWeb);
		panelBotones.add(lblWeb);
		panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

		btnVender.addActionListener(e -> {
			JDialogVenta dialogo = new JDialogVenta(this, gestor);
			dialogo.setVisible(true); // al ser modal, espera aqui hasta que se cierre
			actualizarTabla();
		});

		btnDevolver.addActionListener(e -> devolver());

		btnAforo.addActionListener(e -> {
			JDialogAforo dialogo = new JDialogAforo(this, gestor);
			dialogo.setVisible(true);
		});

		btnAbrirWeb.addActionListener(e -> abrirWeb());
		btnCerrarWeb.addActionListener(e -> cerrarWeb());

		// si cierran la ventana con la web abierta, paro los hilos
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				cerrarWeb();
			}
		});

		add(panelPrincipal);
	}

	public void actualizarTabla() {
		modelo.setEntradas(gestor.getEntradas());
	}

	private void abrirWeb() {
		if (webAbierta()) {
			JOptionPane.showMessageDialog(this, "La web ya esta abierta");
			return;
		}
		if (gestor.todoVendido()) {
			JOptionPane.showMessageDialog(this, "Ya no quedan plazas en ningun evento", "SOLD OUT",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		avisoSoldOutDado = false;
		compradores.clear();
		for (int i = 0; i < 3; i++) {
			HiloComprador hilo = new HiloComprador(gestor, this);
			compradores.add(hilo);
			hilo.start();
		}
		lblWeb.setText("Web: abierta (3 compradores)");
	}

	private void cerrarWeb() {
		for (HiloComprador hilo : compradores) {
			hilo.interrupt();
		}
		lblWeb.setText("Web: cerrada");
	}

	private boolean webAbierta() {
		for (HiloComprador hilo : compradores) {
			if (hilo.isAlive()) {
				return true;
			}
		}
		return false;
	}

	// la llaman los 3 hilos al llenarse todo, pero el aviso solo sale una vez
	public void avisarSoldOut() {
		if (avisoSoldOutDado) {
			return;
		}
		avisoSoldOutDado = true;
		cerrarWeb();
		lblWeb.setText("Web: cerrada (SOLD OUT)");
		JOptionPane.showMessageDialog(this, "SOLD OUT: todos los eventos estan llenos", "SOLD OUT",
				JOptionPane.INFORMATION_MESSAGE);
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
			actualizarTabla();
		}
	}
}
