package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

// Imports estimados hechos con IA.
public class Jframereservados extends JFrame {

	private static final long serialVersionUID = 1L;

	private Random random = new Random();
	private JPanel panelbarra;
	private JLabel camarero;
	private Thread hilocamarero;
	private JPanel panelpista;
	private volatile boolean corriendo = false;

	public Jframereservados() {

		setTitle("Plano de la Discoteca");
		setSize(800, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(10, 10));

		// Barra
		panelbarra = new JPanel();
		panelbarra.setPreferredSize(new Dimension(0, 80));
		panelbarra.setBackground(new Color(220, 220, 220));
		panelbarra.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Barra",
				TitledBorder.CENTER, TitledBorder.TOP));
		add(panelbarra, BorderLayout.NORTH);
		// pista
	    panelpista = new JPanel();
		panelpista.setLayout(null);
		panelpista.setBackground(Color.LIGHT_GRAY);
		panelpista.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

		JLabel texto = new JLabel("Pista", JLabel.CENTER);
		texto.setFont(new Font("Arial", Font.BOLD, 22));
		texto.setForeground(Color.DARK_GRAY);
		texto.getForeground().darker();
		texto.setBounds(230, 180, 150, 30);
		panelpista.add(texto);
		
		// camarero

		camarero = new JLabel("Camarero", JLabel.CENTER);
		camarero.setFont(new Font("Arial", Font.BOLD, 12));
		camarero.setOpaque(true);
		camarero.setBackground(Color.ORANGE);
		camarero.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		camarero.setBounds(265, 10, 80, 30);
		camarero.setVisible(false); //
		panelpista.add(camarero);

		add(panelpista, BorderLayout.CENTER);

		// mesas
		JPanel panelmesas = new JPanel(new GridLayout(4, 1, 10, 15));
		panelmesas.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		// mesas derecha
		for (int i = 1; i < 5; i++) {
			JButton mesa = crearmesa("Mesa barata" + "" + i, TipoMesa.MESA_NORMAL);
			mesa.setFont(new Font("Arial", Font.BOLD, 12));
			mesa.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			panelmesas.add(mesa);
		}
		add(panelmesas, BorderLayout.EAST);

		// mesas inf
		JPanel panelmesasinf = new JPanel(new GridLayout(1, 3, 15, 10));
		panelmesas.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		panelmesasinf.setPreferredSize(new Dimension(0, 70));
		for (int i = 1; i < 4; i++) {
			JButton mesa = crearmesa("Mesa Cara" + "" + i, TipoMesa.MESA_VIP);
			mesa.setFont(new Font("Arial", Font.BOLD, 12));
			mesa.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			panelmesasinf.add(mesa);
		}
		add(panelmesasinf, BorderLayout.SOUTH);

	}

	// Panel para los pedidos

	public class PanelPedido extends JPanel {

		private static final long serialVersionUID = 1L;

		private JComboBox<String> productos;
		private JSpinner cantidad;
		private JButton confirmar;

		public PanelPedido(String nombre, JButton mesa, TipoMesa tipo) {
			setLayout(new BorderLayout(15, 15));
			JPanel panelogo = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JLabel logo = new JLabel("", JLabel.CENTER);

			/// Reescalado de la Imagen generado con IA:
			ImageIcon imagen1 = new ImageIcon("./Resources/Images/LogoDiscoteca+.jpeg");
			Image imgEscalada = imagen1.getImage().getScaledInstance(450, 140, Image.SCALE_SMOOTH);
			logo.setIcon(new ImageIcon(imgEscalada));
			////

			panelogo.add(logo);
			panelogo.setBackground(Color.BLACK);
			add(panelogo, BorderLayout.NORTH);

			JPanel panelcont = new JPanel(new BorderLayout(10, 15));
			panelcont.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

			JPanel panelprod = new JPanel(new GridLayout(2, 2, 10, 15));
			panelprod.setBorder(BorderFactory.createTitledBorder("Pedido para " + nombre));

			JLabel producto = new JLabel("Producto:", JLabel.RIGHT);
			producto.setFont(new Font("Arial", Font.BOLD, 13));

			productos = new JComboBox<>(new String[] { "Coca-Cola - 3.0€", "Coca-Cola Zero - 3.0€",
					"Fanta Naranja - 3.0€", "Fanta Limón - 3.0€", "Sprite - 3.0€", "Red Bull - 4.5€", "Monster - 4.0€",
					"Red Bull Sugarfree - 4.5€", "Heineken - 4.0€", "Estrella Galicia - 3.5€", "Mahou - 3.5€",
					"Cerveza sin alcohol - 3.0€", "Vodka Absolut - 8.0€", "Ron Barceló - 8.0€",
					"Whisky Ballantines - 9.0€", "Ginebra Beefeater - 9.0€", "Tequila José Cuervo - 7.0€",
					"Jägermeister - 6.0€", "Agua mineral - 2.0€", "Zumo de piña - 3.0€", "Tónica - 2.5€", "Hielo - 1.0€"

			});

			JLabel cantidadlabel = new JLabel("Cantidad:", JLabel.RIGHT);
			cantidadlabel.setFont(new Font("Arial", Font.BOLD, 13));

			cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

			panelprod.add(producto);
			panelprod.add(productos);
			panelprod.add(cantidadlabel);
			panelprod.add(cantidad);

			panelcont.add(panelprod, BorderLayout.CENTER);

			confirmar = new JButton("Nuevo Pedido");
			confirmar.setFont(new Font("Arial", Font.BOLD, 13));
			confirmar.setPreferredSize(new Dimension(0, 38));

			confirmar.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					camarero.setVisible(true);
					panelbarra.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
					// No creamos el boton iniciar porque asumimos que al "aceptar pedido", ya has
					// aceptado este mismo
					JButton detener = new JButton("Detener");
					JButton cancelar = new JButton("Cancelar pedido");

					detener.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							corriendo = !corriendo;

							if (corriendo) {
								detener.setText("Reanudar");
							} else {
								detener.setText("Detener");
							}
						}
					});

					cancelar.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (hilocamarero != null && hilocamarero.isAlive()) {
								hilocamarero.interrupt();
							}
							camarero.setVisible(false);
							panelbarra.repaint();
						}
					});
					
					panelbarra.add(detener);
			        panelbarra.add(cancelar);
			        
			        panelbarra.repaint();
			        SwingUtilities.getWindowAncestor(PanelPedido.this).dispose();
			        String producto = (String) productos.getSelectedItem();
					int cantidd = (int) cantidad.getValue();
					hilocamarero = new HiloCamarero(mesa, tipo, producto, cantidd);
					hilocamarero.start();
				}
			});

			panelcont.add(confirmar, BorderLayout.SOUTH);
			add(panelcont, BorderLayout.CENTER);

		}
	}

	//clase del hilo.

	        
	        private class HiloCamarero extends Thread {

	            private JButton botonMesa;
	            private TipoMesa tipo;
	            private String producto;
	            private int cantidad;

	            public HiloCamarero(JButton botonMesa, TipoMesa tipo, String producto, int cantidad) {
	                this.botonMesa = botonMesa;
	                this.tipo = tipo;
	                this.producto = producto;
	                this.cantidad = cantidad;
	            }

	            @Override
	            public void run() {
	                int xInicio = 265;
	                int yInicio = 10;

	                int xDestino;
	                int yDestino;
	                //He usado la IA para que me ayude a colocar exáctamente en los puntos correctos 
	                //las mesas, y como hacer los cálculos correspondientes para que mi label se mueva hacia
	                //las mesas que yo deseo. 
	                if (tipo == TipoMesa.MESA_NORMAL) {
	                    xDestino = panelpista.getWidth() - camarero.getWidth() - 5;
	                    yDestino = botonMesa.getY() + (botonMesa.getHeight() / 2) - (camarero.getHeight() / 2);
	                } else {
	                    xDestino = botonMesa.getX() + (botonMesa.getWidth() / 2) - (camarero.getWidth() / 2);
	                    yDestino = panelpista.getHeight() - camarero.getHeight() - 5;
	                }

	                try {
	                    mover(xInicio, yInicio, xDestino, yDestino, 60);

	                    //confirmación
	                    SwingUtilities.invokeLater(() -> {
	                        JOptionPane.showMessageDialog(
	                                Jframereservados.this,
	                                "¡Producto entregado!\n(" + cantidad + "x " + producto + " en " + botonMesa.getText() + ")",
	                                "Entrega",
	                                JOptionPane.INFORMATION_MESSAGE);
	                    });

	                    mover(xDestino, yDestino, xInicio, yInicio, 60);
	                    SwingUtilities.invokeLater(() -> {
	                        camarero.setVisible(false);
	                        camarero.setLocation(xInicio, yInicio);
	                        panelbarra.removeAll();
	                        panelbarra.revalidate();
	                        panelbarra.repaint();
	                    });
	                
	                } catch (InterruptedException e) {
	                    SwingUtilities.invokeLater(() -> {
	                        JOptionPane.showMessageDialog(
	                                Jframereservados.this,
	                                "Pedido cancelado.",
	                                "Cancelado",
	                                JOptionPane.WARNING_MESSAGE);
	                        
	                        
	                        
	                    });
	                } 
	            }

	            private void mover(int posx1, int posy1, int posx2, int posy2, int pasos) throws InterruptedException {
	                for (int i = 1; i <= pasos; i++) {
	                    while (corriendo) {
	                        Thread.sleep(100);
	                    }

	                    int xActual = posx1 + (posx2 - posx1) * i / pasos;
	                    int yActual = posy1 + (posy2 - posy1) * i / pasos;

	                    SwingUtilities.invokeLater(() -> camarero.setLocation(xActual, yActual));

	                    Thread.sleep(50); //velocidad
	                }
	            }
	        }
	        
	        
	        
	    
	    
	public enum TipoMesa {
		MESA_VIP("Zona VIP", 150.0), MESA_NORMAL("Zona General", 50.0);

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
		LIBRE(Color.GREEN), OCUPADA(Color.RED);

		private Color colorEstado;

		EstadoMesa(Color colorEstado) {
			this.colorEstado = colorEstado;
		}

		public Color getColorEstado() {
			return colorEstado;
		}
	}

	private JButton crearmesa(String nombre, TipoMesa tipo) {
		EstadoMesa estado = random.nextBoolean() ? EstadoMesa.LIBRE : EstadoMesa.OCUPADA;

		JButton mesa = new JButton(nombre);
		mesa.setFont(new Font("Arial", Font.BOLD, 12));
		mesa.setBackground(estado.getColorEstado().darker());
		mesa.setFocusPainted(false);
		mesa.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

		mesa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensaje = "Mesa: " + nombre + "\n" + "Tipo: " + tipo.getDescripcion() + "\n" + "Precio: "
						+ tipo.getPrecioReserva() + "€\n" + "Estado actual: " + estado;

				if (estado == EstadoMesa.OCUPADA) {
					String[] optexto = { "Hacer Pedido", "Cerrar" };

					int respuesta = JOptionPane.showOptionDialog(null, mensaje, "Información sobre la Mesa",
							JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, optexto, optexto[0]);

					// Si la respuesta es que quiere un pedido abrimos un JDialog.
					if (respuesta == 0) {
						realizarpedido(nombre, mesa, tipo);
					}

				} else {
					JOptionPane.showMessageDialog(null, mensaje, "Información sobre la Mesa",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			private void realizarpedido(String nombre, JButton mesa, TipoMesa tipo) {
				JDialog dialog = new JDialog(Jframereservados.this, "Pedido para la mesa " + nombre, true); // Pregunta
																											// a la IA
																											// acerca de
																											// los
																											// campos
																											// del
																											// JDialog.
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setSize(450, 350);
				dialog.setLocationRelativeTo(Jframereservados.this); // pregunta al IA sobre porqué el .this va al
																		// final.

				PanelPedido panel = new PanelPedido(nombre, mesa, tipo);
				dialog.setContentPane(panel);
				dialog.setVisible(true);
			}
		});

		return mesa;
	}
}

//IDEAS DE MEJORA DE CÓDIGO A FUTURO

//ESTARIA BIEN INTENTAR FUSIONAR MEDIANTE UNA BD Y UN HILO EL TEMA DE LOS PEDIDOS DE LOS RESERVADOS Y EL INVENTARIO