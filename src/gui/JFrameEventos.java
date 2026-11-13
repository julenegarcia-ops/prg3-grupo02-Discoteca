package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


public class JFrameEventos extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final Color COLOR_LIBRE = new Color(76, 175, 80);    // verde (IA)
	private static final Color COLOR_RESERVADO = new Color(229, 57, 53); // rojo (IA)

	
	private static final String[] TIPOS_EVENTO = { "Fiesta ibicenca", "Fiesta tecno", "Fiesta de máscaras",
			"Fiesta de roles", "Graduación", "Privado" };

	
	private HashMap<String, String[]> fechasPorEvento = new HashMap<>();
	private HashMap<String, boolean[]> reservasPorEvento = new HashMap<>();

	
	private String eventoSeleccionado = null;

	
	private JLabel lblTitulo;
	private JLabel lblEventoActual;
	private JLabel[] lblFechas = new JLabel[5];

	//JFRAME
	public JFrameEventos() {

		setTitle("Gestión de eventos");
		setSize(900, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		cargarDatos();

		JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
		panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		
		lblTitulo = new JLabel("¡Elige el evento!", SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Arial", Font.BOLD, 36));
		panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

	
		panelPrincipal.add(crearPanelEventos(), BorderLayout.WEST);

		
		panelPrincipal.add(crearPanelFechas(), BorderLayout.CENTER);

		add(panelPrincipal);
	}

	//DATOS HECHO CON IA
	private void cargarDatos() {
		fechasPorEvento.put("Fiesta ibicenca",
				new String[] { "06/06/2026", "13/06/2026", "20/06/2026", "27/06/2026", "04/07/2026" });
		fechasPorEvento.put("Fiesta tecno",
				new String[] { "05/12/2026", "12/12/2026", "19/12/2026", "09/01/2027", "16/01/2027" });
		fechasPorEvento.put("Fiesta de máscaras",
				new String[] { "13/02/2027", "20/02/2027", "27/02/2027", "06/03/2027", "13/03/2027" });
		fechasPorEvento.put("Fiesta de roles",
				new String[] { "28/11/2026", "23/01/2027", "30/01/2027", "06/02/2027", "20/03/2027" });
		fechasPorEvento.put("Graduación",
				new String[] { "29/05/2027", "05/06/2027", "12/06/2027", "19/06/2027", "26/06/2027" });
		fechasPorEvento.put("Privado",
				new String[] { "21/11/2026", "27/11/2026", "11/12/2026", "18/12/2026", "15/01/2027" });

		// Al principio todas las fechas están libres (false = no reservada)
		for (String tipo : TIPOS_EVENTO) {
			reservasPorEvento.put(tipo, new boolean[5]);
		}
	}

//BOTONES DE EVENTOS
	private JPanel crearPanelEventos() {
		JPanel panelEventos = new JPanel(new GridLayout(TIPOS_EVENTO.length, 1, 5, 5));
		panelEventos.setBorder(BorderFactory.createTitledBorder("Tipos de evento"));
		panelEventos.setPreferredSize(new Dimension(200, 0));

		for (String tipo : TIPOS_EVENTO) {
			JButton btnEvento = new JButton(tipo);
			btnEvento.addActionListener(e -> {
				eventoSeleccionado = tipo;
				mostrarFechas();
			});
			panelEventos.add(btnEvento);
		}
		return panelEventos;
	}

	//FECHAS
	private JPanel crearPanelFechas() {
		JPanel panelCentro = new JPanel(new BorderLayout(5, 5));

		lblEventoActual = new JLabel("Ningún evento seleccionado", SwingConstants.CENTER);
		lblEventoActual.setFont(new Font("Arial", Font.BOLD, 18));
		panelCentro.add(lblEventoActual, BorderLayout.NORTH);

		JPanel panelFechas = new JPanel(new GridLayout(5, 1, 5, 5));
		panelFechas.setBorder(BorderFactory.createTitledBorder("Fechas disponibles"));

		for (int i = 0; i < lblFechas.length; i++) {
			JLabel lblFecha = new JLabel("", SwingConstants.CENTER);
			lblFecha.setOpaque(true); 
			lblFecha.setFont(new Font("Arial", Font.BOLD, 20));
			lblFecha.setForeground(Color.WHITE);
			lblFecha.setVisible(false); 

			final int posicion = i; 

			lblFecha.addMouseListener(new MouseAdapter() {

				
				@Override
				public void mouseEntered(MouseEvent e) {
					if (!estaReservada(posicion)) {
						lblFecha.setText("Reservar");
						lblFecha.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}
				}

				
				@Override
				public void mouseExited(MouseEvent e) {
					actualizarFecha(posicion);
					lblFecha.setCursor(Cursor.getDefaultCursor());
				}

				
				@Override
				public void mouseClicked(MouseEvent e) {
					reservarFecha(posicion);
				}
			});

			lblFechas[i] = lblFecha;
			panelFechas.add(lblFecha);
		}

		panelCentro.add(panelFechas, BorderLayout.CENTER);
		return panelCentro;
	}

	// pinta las 5 fechas del evento seleccionado
	private void mostrarFechas() {
		lblEventoActual.setText("Evento: " + eventoSeleccionado);
		for (int i = 0; i < lblFechas.length; i++) {
			lblFechas[i].setVisible(true);
			actualizarFecha(i);
		}
	}

	
	private void actualizarFecha(int posicion) {
		if (eventoSeleccionado == null) {
			return;
		}
		String fecha = fechasPorEvento.get(eventoSeleccionado)[posicion];
		if (estaReservada(posicion)) {
			lblFechas[posicion].setText(fecha + " - RESERVADO");
			lblFechas[posicion].setBackground(COLOR_RESERVADO);
		} else {
			lblFechas[posicion].setText(fecha);
			lblFechas[posicion].setBackground(COLOR_LIBRE);
		}
	}

	private boolean estaReservada(int posicion) {
		return reservasPorEvento.get(eventoSeleccionado)[posicion];
	}

	
	private void reservarFecha(int posicion) {
		String fecha = fechasPorEvento.get(eventoSeleccionado)[posicion];

		if (estaReservada(posicion)) {
			JOptionPane.showMessageDialog(this, "La fecha " + fecha + " ya está reservada", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String cliente = JOptionPane.showInputDialog(this, "Nombre del cliente:", "Reservar " + fecha,
				JOptionPane.QUESTION_MESSAGE);

		// si cancela o no escribe nada no se reserva (IA)
		if (cliente == null || cliente.trim().isEmpty()) {
			actualizarFecha(posicion);
			return;
		}

		int respuesta = JOptionPane.showConfirmDialog(this,
				"¿Reservar " + eventoSeleccionado + " el " + fecha + " para " + cliente + "?", "Confirmar reserva",
				JOptionPane.YES_NO_OPTION);

		if (respuesta == JOptionPane.YES_OPTION) {
			reservasPorEvento.get(eventoSeleccionado)[posicion] = true;
		}
		actualizarFecha(posicion);
	}

	//main prueba (IA)
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrameEventos ventana = new JFrameEventos();
			ventana.setVisible(true);
		});
	}
}
