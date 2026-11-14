package gui;


import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import domain.Evento;
import domain.ReservaEvento;
public class JDialogReservaEvento extends JDialog{
    private static final long serialVersionUID = 1L;
    
    private static final int MAX_PERSONAS = 20; //en gestor entradas pone maximo 20
	private int id;
	private String tipo;
	private String fecha;
	private JTextField txtCliente;
	private JTextField txtDni;
	private JSpinner spinnerPersonas;
	private ReservaEvento reserva = null;
	
	public JDialogReservaEvento(JFrame padre, int id, String tipo, String fecha) {
        super(padre, "Reservar " + tipo, true); // modal: espera hasta que se cierre
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;

        setSize(380, 260);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(10, 10));
        
        txtCliente = new JTextField();
        txtDni = new JTextField();
        spinnerPersonas = new JSpinner(new SpinnerNumberModel(10, 1, MAX_PERSONAS, 1));

        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelForm.add(new JLabel("Evento:"));
        panelForm.add(new JLabel(tipo));
        panelForm.add(new JLabel("Fecha:"));
        panelForm.add(new JLabel(fecha));
        panelForm.add(new JLabel("Cliente:"));
        panelForm.add(txtCliente);
        panelForm.add(new JLabel("DNI:"));
        panelForm.add(txtDni);
        panelForm.add(new JLabel("Nº personas:"));
        panelForm.add(spinnerPersonas);
        add(panelForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnReservar = new JButton("Reservar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnReservar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> dispose());
}

// misma regla que JDialogVenta de Iñigo: 8 números y una letra
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

private void reservar() {
        String cliente = txtCliente.getText().trim();
        String dni = txtDni.getText().trim().toUpperCase();
        int personas = (Integer) spinnerPersonas.getValue();

        if (cliente.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Escribe el nombre del cliente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        if (!dniValido(dni)) {
                JOptionPane.showMessageDialog(this, "DNI mal escrito (ej: 12345678A)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // el evento reservado es un Evento igual que los de la venta de entradas
        Evento evento = new Evento(id, tipo, fecha, personas, 1.0);
        reserva = new ReservaEvento(evento, cliente, dni);
        dispose();
}

public ReservaEvento getReserva() {
        return reserva;
}

	}
	

