package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

// Imports estimados hechos con IA.
public class Jframereservados extends JFrame {

	private static final long serialVersionUID = 1L;
	private Image imagen;
	
	
	public Jframereservados () {
		

        setTitle("Plano de la Discoteca");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

		
		//escaleras
		JPanel panelescaleras = new JPanel();
		panelescaleras.setPreferredSize(new Dimension(0,80));
		panelescaleras.setBackground(new Color(220,220,220));
		panelescaleras.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK,2),"Escaleras",TitledBorder.CENTER,TitledBorder.TOP));
		add (panelescaleras, BorderLayout.NORTH);
		//pista
		JPanel panelpista = new JPanel();
		panelpista.setLayout(new FlowLayout(FlowLayout.CENTER, 0,100));
		panelpista.setBackground(Color.LIGHT_GRAY);
		panelpista.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
		
		JLabel texto = new JLabel("Pista",JLabel.CENTER);
		texto.setFont(new Font("Arial",Font.BOLD,22));
		texto.setForeground(Color.DARK_GRAY);
		texto.getForeground().darker();
		panelpista.add(texto, BorderLayout.CENTER);
		add(panelpista,BorderLayout.CENTER);
		//mesas
		JPanel panelmesas = new JPanel (new GridLayout(4,1,10,15));
		panelmesas.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		
		//mesas derecha
		for (int i = 1 ;  i < 5 ; i++) {
			JButton mesa = crearmesa(//aqui tienes que meter el nombre de la mesa + i);
			mesa.setFont(new Font("Arial", Font.BOLD, 12));	
			mesa.setBackground(new Color (230,240,255));
			mesa.setBorder(BorderFactory.createLineBorder(Color.black,2));
			panelmesas.add(mesa);
		}
		add(panelmesas, BorderLayout.EAST);
		
		
		//mesas inf
		JPanel panelmesasinf = new JPanel (new GridLayout(1,3,15,10));
		panelmesas.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		panelmesasinf.setPreferredSize(new Dimension(0, 70));
		for (int i = 1 ;  i < 4 ; i++) {
			JButton mesa = new JButton("Reservado Caro" + i);
			mesa.setFont(new Font("Arial", Font.BOLD, 12));	
			mesa.setBackground(new Color (230,240,255));
			mesa.setBorder(BorderFactory.createLineBorder(Color.black,2));
			panelmesasinf.add(mesa);
		}
		add(panelmesasinf, BorderLayout.SOUTH);

		}

	public enum TipoMesa {
        VIP("Zona VIP", 150.0),
        STANDARD("Zona General", 50.0);

        private String descripcion;
        private double precioReserva;

        TipoMesa(String descripcion, double precioReserva) {
            this.descripcion = descripcion;
            this.precioReserva = precioReserva;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public double getPrecioReserva() {
            return precioReserva;
        }
    }

    public enum EstadoMesa {
        LIBRE(Color.GREEN),
        RESERVADA(Color.ORANGE),
        OCUPADA(Color.RED);

        private Color colorEstado;

        EstadoMesa(Color colorEstado) {
            this.colorEstado = colorEstado;
        }

        public Color getColorEstado() {
            return colorEstado;
        }
    }
    
	private JButton crearmesa(String nombre, TipoMesa tipo, EstadoMesa estado)  {
			    
			//comparativa de los ifs para diferencias las mesas entre sí.....
			JButton mesa = new JButton(nombre);
	        mesa.setFont(new Font("Arial", Font.BOLD, 12));
	        mesa.setBackground(estado.getColorEstado());
	        mesa.setFocusPainted(false);
	        mesa.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

	        mesa.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String mensaje = "Mesa: " + nombre  +
	                                 "Tipo: " + tipo.getDescripcion() + 
	                                 "Precio: " + tipo.getPrecioReserva() + 
	                                 "Estado actual: " + estado;
	                                 
	                JOptionPane.showMessageDialog(null, mensaje, "Información de Mesa", JOptionPane.INFORMATION_MESSAGE);
	            }
	        });

	        return mesa;
	    
	}
};
//NOTAS PARA EL DIA SIGUIENTE
// TERMINAR DE DEFINIR EL CONSTRUCTOR DE LAS MESAS USANDO LOS ENUMS y diferenciandolos con ifs.


//COGER LOS DATOS DE ALGUN SITIO, (O QUE SE GENEREN ALEATORIAMENTE)
//

//
	

