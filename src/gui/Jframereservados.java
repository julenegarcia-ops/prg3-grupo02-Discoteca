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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import jdbc.GestorBD;
// Imports estimados hechos con IA.
public class Jframereservados extends JFrame {

	private static final long serialVersionUID = 1L;
	private List <Datomesa> listaMesas = new ArrayList <>();
	private Random random = new Random();
	private JPanel panelbarra;
	private JLabel camarero;
	private Thread hilocamarero;
	private JPanel panelpista;
	private volatile boolean corriendo = false;
	private GestorBD gestorBD;
	private JFrameInventario ventanaInventario;

	
	
	private static class Datomesa {
	    String nombre;
	    TipoMesa tipo;
	    EstadoMesa estado;
	    String cliente;
	    JButton boton;

	    Datomesa(String nombre, TipoMesa tipo, EstadoMesa estado, JButton boton) {
	        this.nombre = nombre;
	        this.tipo = tipo;
	        this.estado = estado;
	        this.cliente = "";
	        this.boton = boton;
	    }
	    @Override
	    public String toString() {
	    	return nombre;
	    }
	    
	}
	    
	public Jframereservados() {
	    this(null);
	}
	
	public Jframereservados(JFrameInventario ventanaInventario) {
		this.ventanaInventario = ventanaInventario;
		this.gestorBD = new GestorBD();
		this.gestorBD.inicializarBD();
		setTitle("Plano de la Discoteca");
		setSize(1020, 720);
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
		texto.setBounds(230, 180, 150, 30);
		panelpista.add(texto);
		
		// camarero

		camarero = new JLabel("", JLabel.CENTER);
		camarero.setFont(new Font("Arial", Font.BOLD, 12));
		camarero.setBounds(265, 10, 70, 90);
		ImageIcon iconoOriginal = new ImageIcon("./resources/images/joritzcamarero.jpeg");
		Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(70, 90, Image.SCALE_SMOOTH);
		camarero.setIcon(new ImageIcon(imagenEscalada));

		camarero.setOpaque(true);
		camarero.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		camarero.setVisible(false); //
		panelpista.add(camarero);
		
		JButton btnVolver = new JButton("Volver");
		btnVolver.setBounds(10, 10, 90, 30);
		btnVolver.addActionListener(e -> dispose());
		panelpista.add(btnVolver);

		add(panelpista, BorderLayout.CENTER);

		// mesas
		JPanel panelmesas = new JPanel(new GridLayout(4, 1, 10, 15));
		panelmesas.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		// mesas derecha
		for (int i = 1; i < 5; i++) {
			JButton mesa = crearmesa("Reservado " + "" + i, TipoMesa.MESA_NORMAL);
			
			panelmesas.add(mesa);
		}
		add(panelmesas, BorderLayout.EAST);

		// mesas inf
		JPanel panelmesasinf = new JPanel(new GridLayout(1, 3, 15, 10));
		panelmesasinf.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		panelmesasinf.setPreferredSize(new Dimension(0, 70));
		for (int i = 1; i < 4; i++) {
			JButton mesa = crearmesa("Reservado VIP" + "" + i, TipoMesa.MESA_VIP);
			mesa.setFont(new Font("Arial", Font.BOLD, 12));
			mesa.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			panelmesasinf.add(mesa);
		}
		add(panelmesasinf, BorderLayout.SOUTH);

		
		JPanel panelgestion = new JPanel(new BorderLayout());
		panelgestion.setPreferredSize(new Dimension(240, 0));
		panelgestion.setBackground(new Color(240, 242, 245));
		panelgestion.setBorder(BorderFactory.createTitledBorder(
		        BorderFactory.createLineBorder(Color.BLACK, 2), "Gestión de Mesas",
		        TitledBorder.CENTER, TitledBorder.TOP));
		
	add(panelgestion, BorderLayout.WEST);
		
		
	JPanel contenedorgestion = new JPanel(new GridLayout(0, 1, 0, 10));
	contenedorgestion.setOpaque(false);

	JLabel labelselecmesa = new JLabel("Seleccionar mesa:");
	labelselecmesa.setFont(new Font("Arial", Font.BOLD, 12));
	JComboBox<Datomesa> combomesas = new JComboBox<>(listaMesas.toArray(new Datomesa[0]));

	JLabel nombrereserva = new JLabel("Nombre de la reserva:");
	nombrereserva.setFont(new Font("Arial", Font.BOLD, 12));
	JTextField nombre = new JTextField();

	JButton nuevareserva = new JButton("Añadir Reserva");
	nuevareserva.setBackground(new Color(34, 139, 34));
	nuevareserva.setForeground(Color.WHITE);
	nuevareserva.setFont(new Font("Arial", Font.BOLD, 12));
	nuevareserva.setFocusPainted(false);

	nuevareserva.addActionListener(e -> {
		Datomesa seleccionada = (Datomesa) combomesas.getSelectedItem();
		if (seleccionada == null) {
			return;
		} 

		if (seleccionada.estado == EstadoMesa.OCUPADA) {
			JOptionPane.showMessageDialog(this, "Esa mesa ya está ocupada.", "Mesa ocupada", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String titular = nombre.getText().trim();
		if (titular.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Introduce el nombre del titular.", "Falta nombre", JOptionPane.WARNING_MESSAGE);
			return;
		}

		cambioestadomesa(seleccionada, EstadoMesa.OCUPADA, titular);
		nombre.setText("");
	});

	JButton libre = new JButton("Marcar como Libre");
	libre.setBackground(new Color(180, 45, 45));
	libre.setForeground(Color.WHITE);
	libre.setFont(new Font("Arial", Font.BOLD, 12));
	libre.setFocusPainted(false);

	libre.addActionListener(e -> {
		Datomesa seleccionada = (Datomesa) combomesas.getSelectedItem();
		if (seleccionada == null) return;

		if (seleccionada.estado == EstadoMesa.LIBRE) {
			JOptionPane.showMessageDialog(this, "Esa mesa ya está libre.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		cambioestadomesa(seleccionada, EstadoMesa.LIBRE, "");
	});

	contenedorgestion.add(labelselecmesa);
	contenedorgestion.add(combomesas);
	contenedorgestion.add(nombrereserva);
	contenedorgestion.add(nombre);
	contenedorgestion.add(nuevareserva);
	contenedorgestion.add(libre);

	panelgestion.add(contenedorgestion, BorderLayout.NORTH);
}
		
	

	// Panel para los pedidos

	public class PanelPedido extends JPanel {

		private static final long serialVersionUID = 1L;
		private JPanel panelcentro;
		private JLabel labelseleccionado;
		private String productoseleccionado = null;

		private JSpinner cantidad;
		private JButton confirmar;
		private String nombremesa;

		public PanelPedido(String nombre, JButton mesa, TipoMesa tipo) {
			this.nombremesa = nombre;
			setLayout(new BorderLayout(10, 10));
			JPanel panelogo = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JLabel logo = new JLabel("", JLabel.CENTER);
			logo.setIcon(recursosbebidas.obtenerIcono(recursosbebidas.LOGO_DISCOTECA, 520, 100));
			panelogo.add(logo);
			panelogo.setBackground(Color.BLACK);
			add(panelogo, BorderLayout.NORTH);
			
		

			//parte central
			
			panelcentro =  new JPanel(new BorderLayout());
			panelcentro.setBorder(BorderFactory.createTitledBorder("Menu para " + nombremesa));
			mostrarCategorias();
			add(panelcentro, BorderLayout.CENTER);
			
			//inf
			
			
			JPanel panelinf = new JPanel(new BorderLayout(10, 10));
			panelinf.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));


		
			
			
			//resumen
			JPanel resumen = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
			labelseleccionado = new JLabel("Aún no has seleccionado nada");
			labelseleccionado.setFont(new Font("Arial", Font.BOLD, 14));
			
			
			labelseleccionado.setForeground(new Color(0, 102, 204));
			
			JLabel cantidadlabel = new JLabel("Cantidad:", JLabel.RIGHT);
			cantidadlabel.setFont(new Font("Arial", Font.BOLD, 13));


			cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
			resumen.add(labelseleccionado);
			resumen.add(cantidadlabel);
			resumen.add(cantidad);
			
			
			confirmar = new JButton("Nuevo Pedido");
			confirmar.setFont(new Font("Arial", Font.BOLD, 13));
			confirmar.setPreferredSize(new Dimension(0, 38));

			confirmar.addActionListener(new ActionListener() {
				
				
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if (productoseleccionado == null) {
						JOptionPane.showMessageDialog(PanelPedido.this,
								"Por favor, selecciona primero un producto del menú.",
								"Falta producto", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					String nombreclean = productoseleccionado.split(" - ")[0].trim();
					int canti = (int) cantidad.getValue();

					boolean existencias = gestorBD.conectarstockynombre(nombreclean, canti);

					if (!existencias) {
					    JOptionPane.showMessageDialog(
					        PanelPedido.this,
					        "No hay suficiente stock en el inventario de " + nombreclean,
					        "Sin stock",
					        JOptionPane.ERROR_MESSAGE
					    );
					    return;
					}

					if (ventanaInventario != null) {
					    ventanaInventario.recargarDesdeBD();
					}
					
					
					
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
			        String producto = productoseleccionado;
					int cantidd = (int) cantidad.getValue();
					hilocamarero = new HiloCamarero(mesa, tipo, producto, cantidd);
					hilocamarero.start();
				}
			});

			panelinf.add(resumen, BorderLayout.NORTH);
			panelinf.add(confirmar, BorderLayout.SOUTH);
			add(panelinf, BorderLayout.SOUTH);

		
			
		}
		
		private void mostrarCategorias() {

			
			panelcentro.removeAll(); 
			JPanel panelcat = new JPanel(new GridLayout(1, 3, 15, 15));
			panelcat.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
			
			String[][] prodAlcohol = {
					{ "Heineken - 4.0€", recursosbebidas.HEINEKEN },
					{ "Estrella Galicia - 3.5€", recursosbebidas.ESTRELLA },
					{ "Mahou - 3.5€", recursosbebidas.MAHOU },
					{ "Vodka Absolut - 8.0€", recursosbebidas.VODKA },
					{ "Ron Barceló - 8.0€", recursosbebidas.RON },
					{ "Whisky Ballantines - 9.0€", recursosbebidas.WHISKY },
					{ "Ginebra Beefeater - 9.0€", recursosbebidas.GINEBRA },
					{ "Tequila José Cuervo - 7.0€", recursosbebidas.TEQUILA },
					{ "Jägermeister - 6.0€", recursosbebidas.JAGER }
				};

				String[][] prodRefrescos = {
					{ "Coca-Cola - 3.0€", recursosbebidas.COCA_COLA },
					{ "Coca-Cola Zero - 3.0€", recursosbebidas.COCA_ZERO },
					{ "Fanta Naranja - 3.0€", recursosbebidas.FANTA_NARANJA },
					{ "Fanta Limón - 3.0€", recursosbebidas.FANTA_LIMON },
					{ "Sprite - 3.0€", recursosbebidas.SPRITE },
					{ "Red Bull - 4.5€", recursosbebidas.RED_BULL },
					{ "Monster - 4.0€", recursosbebidas.MONSTER },
					{ "Red Bull Sugarfree - 4.5€", recursosbebidas.RED_BULL_SF }
				};

				String[][] prodOtros = {
					{ "Cerveza sin alcohol - 3.0€", recursosbebidas.CERVEZA_SIN },
					{ "Agua mineral - 2.0€", recursosbebidas.AGUA },
					{ "Zumo de piña - 3.0€", recursosbebidas.ZUMO_PINA },
					{ "Tónica - 2.5€", recursosbebidas.TONICA },
					{ "Hielo - 1.0€", recursosbebidas.HIELO }
				};
			
			
			

			panelcat.add(crearBotonCategoria("Bebidas Alcohólicas", recursosbebidas.CAT_ALCOHOL,prodAlcohol));
			panelcat.add(crearBotonCategoria("Refrescos", recursosbebidas.CAT_REFRESCOS,prodRefrescos));
			panelcat.add(crearBotonCategoria("Otros", recursosbebidas.CAT_OTROS,prodOtros));

			panelcentro.add(panelcat, BorderLayout.CENTER);
			panelcentro.revalidate();
			panelcentro.repaint();
		}

		private JButton crearBotonCategoria(String titulo, String rutaImagen, String[][] tipobebida) {
			JButton bottit = new JButton(titulo);
			bottit.setFont(new Font("Arial", Font.BOLD, 14));
			bottit.setIcon(recursosbebidas.obtenerIcono(rutaImagen, 110, 110));
			bottit.setHorizontalTextPosition(JButton.CENTER);
			bottit.setVerticalTextPosition(JButton.BOTTOM);
			bottit.setBackground(Color.WHITE);
			bottit.setFocusPainted(false);
			bottit.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
			
			bottit.addActionListener(e -> mostrarproductos (titulo, tipobebida));
			return bottit;
		}

		private void mostrarproductos(String titulo, String[][] tipobebida) {
			panelcentro.removeAll();

			JPanel volver = new JPanel(new BorderLayout());
			JButton btnVolver = new JButton("← Volver");
			btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
			btnVolver.addActionListener(e -> mostrarCategorias());

			JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
			lblTitulo.setFont(new Font("Arial", Font.BOLD, 15));

			volver.add(btnVolver, BorderLayout.WEST);
			volver.add(lblTitulo, BorderLayout.CENTER);
			panelcentro.add(volver, BorderLayout.NORTH);

			JPanel gridProductos = new JPanel(new GridLayout(0, 3, 10, 10));
			gridProductos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			for (String[] prod : tipobebida) {
				final String nombreprecio = prod[0];
				String rutaImg = prod[1];

				JButton botonprod = new JButton("<html><center>" + nombreprecio.replace(" - ", "<br><b>") + "</b></center></html>");
				botonprod.setIcon(recursosbebidas.obtenerIcono(rutaImg, 65, 65));
				botonprod.setHorizontalTextPosition(JButton.CENTER);
				botonprod.setVerticalTextPosition(JButton.BOTTOM);
				botonprod.setBackground(Color.WHITE);
				botonprod.setFocusPainted(false);

				botonprod.addActionListener(e -> {
					productoseleccionado = nombreprecio;
					labelseleccionado.setText("Seleccionado: " + productoseleccionado);
				});

				gridProductos.add(botonprod);
			}

			JScrollPane scroll = new JScrollPane(gridProductos);
			scroll.getVerticalScrollBar().setUnitIncrement(16);
			panelcentro.add(scroll, BorderLayout.CENTER);

			panelcentro.revalidate();
			panelcentro.repaint();		}
		
		
		
		
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
	                    	String nombrelimpio = botonMesa.getText().replaceAll("<[^>]*>", " ").trim(); // he usado la IA para el html
	                        JOptionPane.showMessageDialog(
	                                Jframereservados.this,
	                                "¡Producto entregado!\n(" + cantidad + "x " + producto + " en " + nombrelimpio + ")",
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

	
	
	
	
	private void cambioestadomesa (Datomesa datos, EstadoMesa nuevoestado, String nombrereserva) {
		datos.estado = nuevoestado;
		datos.cliente = nombrereserva;
		datos.boton.setBackground(nuevoestado.getColorEstado().darker());
		if (nuevoestado == EstadoMesa.OCUPADA) {
			datos.boton.setText("<html><center>" + datos.nombre + "<br><small>"  + "</small></center></html>"); //html hecho con IA
		} else {
			datos.boton.setText(datos.nombre);
		}
		
		
	} 
	private JButton crearmesa(String nombre, TipoMesa tipo) {
		
		EstadoMesa estado = random.nextBoolean() ? EstadoMesa.LIBRE : EstadoMesa.OCUPADA;

		JButton mesa = new JButton(nombre);
		mesa.setFont(new Font("Arial", Font.BOLD, 12));
		mesa.setForeground(Color.WHITE);
		mesa.setBackground(estado.getColorEstado().darker());
		mesa.setFocusPainted(false);
		mesa.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

		
		Datomesa datos = new Datomesa(nombre, tipo, estado, mesa);
		
		
		if (datos.estado == EstadoMesa.OCUPADA) {
			mesa.setText("<html><center>" + nombre + "<br><small>"  + "</small></center></html>"); //Html con ia
		}
		
		listaMesas.add(datos);

		
		
		mesa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String infocliente = datos.estado == EstadoMesa.OCUPADA ? "\nTitular: " + datos.cliente : "";
				String mensaje = "Mesa: " + nombre + "\n" + "Tipo: " + tipo.getDescripcion() + "\n" + "Precio: "
						+ tipo.getPrecioReserva() + "€\n" + "Estado actual: " + datos.estado + infocliente ;

				if (datos.estado == EstadoMesa.OCUPADA) {
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
				dialog.setSize(620, 540);
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


