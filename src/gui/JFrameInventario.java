package gui;


import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;

import domain.Producto;

public class JFrameInventario extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private List<Producto> productos;

	private DefaultTableModel modeloDatosProductos;

	private JTable tablaProductos;
	
	private JLabel lblSeleccion;
	
	private JButton btnActualizarStock;
	
	// Botones para controlar la simulación del Thread que simula el consumo de productos durante los eventos
	private JButton btnIniciar;
	private JButton btnParar;

	// Thread que simulará el consumo de los productos
	private HiloConsumo hiloConsumo;
	
	//Para volver a la ventana principal
	private JButton btnVolver;
	private JFramePrincipal ventanaAnterior;
	
	// Colores tanto para decorar como para el stock.
	private static final Color MORADO = new Color(124, 58, 237);
	private static final Color BARRA_FONDO = new Color(232, 230, 240);
	private static final Color VERDE = new Color(34, 160, 80);
	private static final Color AMBAR = new Color(230, 160, 20);
	private static final Color ROJO = new Color(220, 60, 60);
	private static final Color OK_FONDO = new Color(214, 240, 222);
	private static final Color OK_TEXTO = new Color(30, 120, 60);
	private static final Color BAJO_FONDO = new Color(253, 236, 180);
	private static final Color BAJO_TEXTO = new Color(150, 100, 10);
	private static final Color AGOTADO_FONDO = new Color(250, 215, 215);
	private static final Color AGOTADO_TEXTO = new Color(180, 40, 40);

	public JFrameInventario(JFramePrincipal ventanaAnterior) {
		this.ventanaAnterior = ventanaAnterior;
		
		//Generado con IA
		// Refrescos
		Producto p1 = new Producto(1, "Coca-Cola", "Refrescos", 3.0, 80, 15);
		Producto p2 = new Producto(2, "Coca-Cola Zero", "Refrescos", 3.0, 45, 15);
		Producto p3 = new Producto(3, "Fanta Naranja", "Refrescos", 3.0, 12, 15);
		Producto p4 = new Producto(4, "Fanta Limón", "Refrescos", 3.0, 30, 10);
		Producto p5 = new Producto(5, "Sprite", "Refrescos", 3.0, 0, 10);

		// Bebidas energéticas
		Producto p6 = new Producto(6, "Red Bull", "Energéticas", 4.5, 60, 20);
		Producto p7 = new Producto(7, "Monster", "Energéticas", 4.0, 8, 15);
		Producto p8 = new Producto(8, "Red Bull Sugarfree", "Energéticas", 4.5, 20, 10);

		// Cervezas
		Producto p9 = new Producto(9, "Heineken", "Cervezas", 4.0, 100, 25);
		Producto p10 = new Producto(10, "Estrella Galicia", "Cervezas", 3.5, 20, 25);
		Producto p11 = new Producto(11, "Mahou", "Cervezas", 3.5, 55, 20);
		Producto p12 = new Producto(12, "Cerveza sin alcohol", "Cervezas", 3.0, 0, 10);

		// Licores
		Producto p13 = new Producto(13, "Vodka Absolut", "Licores", 8.0, 15, 5);
		Producto p14 = new Producto(14, "Ron Barceló", "Licores", 8.0, 4, 6);
		Producto p15 = new Producto(15, "Whisky Ballantines", "Licores", 9.0, 12, 5);
		Producto p16 = new Producto(16, "Ginebra Beefeater", "Licores", 9.0, 3, 5);
		Producto p17 = new Producto(17, "Tequila José Cuervo", "Licores", 7.0, 10, 5);
		Producto p18 = new Producto(18, "Jägermeister", "Licores", 6.0, 0, 5);

		// Otros productos
		Producto p19 = new Producto(19, "Agua mineral", "Otros", 2.0, 90, 20);
		Producto p20 = new Producto(20, "Zumo de piña", "Otros", 3.0, 5, 10);
		Producto p21 = new Producto(21, "Tónica", "Refrescos", 2.5, 25, 10);
		Producto p22 = new Producto(22, "Hielo", "Otros", 1.0, 40, 15);
        
		
		// Creo la lista de productos
		productos = new ArrayList<>(Arrays.asList(p1, p2, p3, p4, p5,
		        p6, p7, p8, p9, p10,
		        p11, p12, p13, p14, p15,
		        p16, p17, p18, p19, p20,
		        p21, p22
		));
		//Fin generado por IA

		
		
		// Cabecera del modelo de datos
		Vector<String> cabeceraProductos = new Vector<String>(
		        Arrays.asList("ID", "NOMBRE", "CATEGORÍA",
		                      "PRECIO", "STOCK", "STOCK MÍNIMO", "ESTADO"));

		// Creo el  modelo

		this.modeloDatosProductos = new DefaultTableModel(
				new Vector<Vector<Object>>(), cabeceraProductos) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
		};

		// Creo la JTable y le paso el modelo de datos
		this.tablaProductos = new JTable(this.modeloDatosProductos);

		//Ahora aplicamos el renderizado de la tabla
		//Creo el renderer a aplicar a cada fila
		//Ese renderer cambia el color de las filas en función del stock del producto y su stock mínimo
		//Si el stock == 0, Rojo, no hay stock
		//Si el stock <stockminimo, amarillo
		//Si el stock > stock minimo, verde
		
		TableCellRenderer mirenderer = (table, value, isSelected, hasFocus, row, column) ->  {



		        JLabel result = new JLabel();

		        // Mostrar el valor de la celda
		        if (value != null) {
		            result.setText(value.toString());
		        } else {
		            result.setText("");
		        }
		        
		        result.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		        result.setOpaque(true);
		       


		        // Coloreo según el stock
		        // Si la fila está seleccionada
		        if (isSelected) { 
		            result.setBackground(table.getSelectionBackground());
		            result.setForeground(table.getSelectionForeground());
		        }else {
		        	result.setBackground(Color.WHITE);
		        }
		        return result;
		    
		};

		// Aplico el renderer a todas las columnas
		
		tablaProductos.setDefaultRenderer(Object.class, mirenderer);
		
		tablaProductos.getTableHeader().setReorderingAllowed(false);
		
		tablaProductos.getColumnModel().getColumn(4).setCellRenderer(new BarraStockRenderer());
		tablaProductos.getColumnModel().getColumn(6).setCellRenderer(new EstadoRenderer());
		
		
		tablaProductos.setRowHeight(34);
		tablaProductos.setShowGrid(false);
		
		tablaProductos.setShowHorizontalLines(true);
		tablaProductos.setGridColor(new Color(235, 233, 242));
		// Fila seleccionada en lila suave (como en la segunda imagen)
		tablaProductos.setSelectionBackground(new Color(240, 236, 252));
		tablaProductos.setSelectionForeground(Color.BLACK);
		tablaProductos.setIntercellSpacing(new Dimension(0, 1));
		int[] anchos = {40, 190, 120, 70, 120, 110, 100};
		for (int i = 0; i < anchos.length; i++) {
		    tablaProductos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
		}
		
		// Sólo permito la elección de una sola fila de la tabla
		this.tablaProductos.setSelectionMode(
		        ListSelectionModel.SINGLE_SELECTION);

		// Introduzco los productos en el modelo
		for (Producto p : productos) {

		    Vector<Object> fila = new Vector<Object>();

		    fila.add(p.getId());
		    fila.add(p.getNombre());
		    fila.add(p.getCategoria());
		    fila.add(p.getPrecio());
		    fila.add(p.getStock());
		    fila.add(p.getStockmin());
		    fila.add(null);

		    this.modeloDatosProductos.addRow(fila);
		}
		
		//GESTIÓN DE EVENTOS
		// 1- Al seleccionar un producto de la JTable, se muestra debajo su nombre y stock actual.
		// 2- Al pulsar Actualizar stock, aparezca un JOptionPane para introducir la nueva cantidad.
		// 3- Al confirmar, se actualice tanto el objeto Producto del ArrayList como la fila de la tabla.
		//4- El renderizador cambie automáticamente el color según el nuevo stock.
		
		
		// Etiqueta para mostrar el producto seleccionado
		this.lblSeleccion = new JLabel("Selecciona un producto de la tabla");

		// Botón para actualizar el stock
		this.btnActualizarStock = new JButton("Actualizar stock");

		// Inicialmente estará desactivado
		this.btnActualizarStock.setEnabled(false);
		

		// Evento al seleccionar una fila de la tabla
		this.tablaProductos.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int fila = tablaProductos.getSelectedRow();
				
				if (fila != -1) {
					String nombre = (String) tablaProductos.getValueAt(fila, 1);
					int stock = (int) tablaProductos.getValueAt(fila, 4);
					
					lblSeleccion.setText(
							"Producto seleccionado: " + nombre
							+ " - Stock actual: " + stock);
					//btnActualizarStock.setEnabled(true);
					// Cambio el boton para que no se active cuando se está haciendo la simulación de ventas con el Thread
					btnActualizarStock.setEnabled(
							hiloConsumo == null || !hiloConsumo.isAlive());
				} else {
					lblSeleccion.setText("Selecciona un producto de la tabla");
					btnActualizarStock.setEnabled(false);
			    }
			    
			}
		});

		

		// Evento del botón Actualizar stock, te pide que indroduzcas el nuevo stock del producto seleccionado a traves de un Dialog
		this.btnActualizarStock.addActionListener(e -> {

		    int fila = tablaProductos.getSelectedRow();

		    if (fila != -1) {

		        int id = (int) tablaProductos.getValueAt(fila, 0);

		        // Busco el producto seleccionado en el ArrayList
		        Producto productoSeleccionado = null;

		        for (Producto p : productos) {
		            if (p.getId() == id) {
		                productoSeleccionado = p;
		                break;
		            }
		        }

		        if (productoSeleccionado == null) {
		            JOptionPane.showMessageDialog(this,
		                    "No se ha encontrado el producto.",
		                    "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }

		        String respuesta = JOptionPane.showInputDialog(
		                this,
		                "Introduce el nuevo stock de "
		                        + productoSeleccionado.getNombre() + ":",
		                productoSeleccionado.getStock());

		        // Si el usuario cancela, no hacemos nada
		        if (respuesta == null) {
		            return;
		        }

		        try {
		            int nuevoStock = Integer.parseInt(respuesta.trim());

		            if (nuevoStock < 0) {
		                JOptionPane.showMessageDialog(this,
		                        "El stock no puede ser negativo.",
		                        "Error", JOptionPane.ERROR_MESSAGE);
		                return;
		            }

		            // Actualizo el objeto Producto
		            productoSeleccionado.setStock(nuevoStock);

		            // Actualizo el modelo de la JTable
		            tablaProductos.setValueAt(nuevoStock, fila, 4);
		            tablaProductos.repaint();

		            // Actualizo la etiqueta inferior
		            lblSeleccion.setText(
		                    "Producto seleccionado: "
		                    + productoSeleccionado.getNombre()
		                    + " - Stock actual: " + nuevoStock);

		            JOptionPane.showMessageDialog(this,
		                    "Stock actualizado correctamente.");

		        } catch (NumberFormatException ex) {

		            JOptionPane.showMessageDialog(this,
		                    "Debes introducir un número entero válido.",
		                    "Error", JOptionPane.ERROR_MESSAGE);
		        }
		    }
		});

		
		
		//Implemento un MouseListener: Al hacer doble clic sobre una fila, aparece un JOptionPane con la información del producto.
		

		tablaProductos.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 2) {

					int fila = tablaProductos.rowAtPoint(e.getPoint());

					if (fila != -1) {

						mostrarInfoProducto(fila);
					}
				}
			}
		});

		//CREO LOS BOTONES Y SUS LISTENERS para el Thread de simulación de consumo

		// Botón para iniciar la simulación
		btnIniciar = new JButton("Iniciar simulación");
		btnIniciar.setEnabled(true);

		btnIniciar.addActionListener(e -> {

		    btnIniciar.setEnabled(false);
		    btnParar.setEnabled(true);
		    btnActualizarStock.setEnabled(false);

		    // Creo y arranco el Thread
		    hiloConsumo = new HiloConsumo();
		    hiloConsumo.start();
		});

		// Botón para detener la simulación
		btnParar = new JButton("Detener simulación");
		btnParar.setEnabled(false);

		btnParar.addActionListener(e -> {

		    if (hiloConsumo != null) {
		        hiloConsumo.interrupt();
		    }

		    btnParar.setEnabled(false);
		});

		
		
		// PONEMOS LOS COMPONENTES DE LA VENTANA 
		// Creo el panel principal 
		JPanel principal = new JPanel(new BorderLayout(10, 10));

		// Creo el JScrollPane
		JScrollPane scrollinventario =
		        new JScrollPane(this.tablaProductos);

		scrollinventario.setBorder(
		        new TitledBorder("Lista de productos"));

		this.tablaProductos.setFillsViewportHeight(true);
		
		btnVolver = new JButton("Volver"); 
		
		btnVolver.addActionListener(e -> volverAPantallaPrincipal());

		JPanel panelSuperior = new JPanel(new BorderLayout());
		panelSuperior.add(btnVolver, BorderLayout.WEST);
		// Línea morada debajo de la cabecera
		panelSuperior.setBorder(BorderFactory.createCompoundBorder(
		        BorderFactory.createMatteBorder(0, 0, 3, 0, MORADO),
		        BorderFactory.createEmptyBorder(0, 0, 10, 0)));
		principal.add(panelSuperior, BorderLayout.NORTH);

		// Añado los  componentes

		// Tabla en el centro
		principal.add(scrollinventario, BorderLayout.CENTER);


		// Panel inferior
		JPanel panelInferior = new JPanel(new BorderLayout(10, 10));

		// Panel para los botones
		JPanel panelBotones = new JPanel(new GridLayout(1, 3, 10, 10));

		panelBotones.add(btnActualizarStock);
		panelBotones.add(btnIniciar);
		panelBotones.add(btnParar);

		// Etiqueta informativa sobre las ventas realizadas durante la simulación con el Thread
		panelInferior.add(lblSeleccion, BorderLayout.NORTH);

		// Botones
		panelInferior.add(panelBotones, BorderLayout.CENTER);


		// Añado panel inferior
		principal.add(panelInferior, BorderLayout.SOUTH);
		
		principal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Añado el panel principal al JFrame
		this.add(principal);

		// Configuración de la ventana
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("INVENTARIO");
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		
		//Generado por IA
		//Sirve para detener el Thread cuando se cierra la ventana
		this.addWindowListener(new java.awt.event.WindowAdapter() {

		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		        if (hiloConsumo != null) {
		            hiloConsumo.interrupt();
		        }
		    }
		});
		//FIN con IA
	}
	
	//Metodos( el boton de volver generado por IA)
	
	//IA
	private void volverAPantallaPrincipal() {
		// Si la simulación está en marcha, la paro antes de salir
		if (hiloConsumo != null) {
			hiloConsumo.interrupt();
		}
		this.dispose();
		// La principal sigue abierta: solo la pongo delante
		if (ventanaAnterior != null) {
			ventanaAnterior.toFront();
		}
	}
	
	//FIN IA
	
	private static String textoEstado(int stock, int stockmin) {
		if (stock == 0) return "Agotado";
		if (stock <= stockmin) return "Bajo";
		return "OK";
	}
	
	//Generado con IA
	private static void pintarBarra(Graphics g, int x, int y, int ancho, int alto,
            int stock, int stockmin) {
// Fondo de la barra
g.setColor(BARRA_FONDO);
g.fillRect(x, y, ancho, alto);

if (stock == 0) {
// Agotado: solo una marca roja al principio, como en la segunda imagen
g.setColor(ROJO);
g.fillRect(x, y, 3, alto);
return;
}

int maximo = Math.max(stockmin * 2, 1);
double proporcion = Math.min(1.0, stock / (double) maximo);
int relleno = Math.max(3, (int) Math.round(ancho * proporcion));

g.setColor(stock <= stockmin ? AMBAR : VERDE);
g.fillRect(x, y, relleno, alto);
}
	
	
//FIN DE GENERADO CON IA
	
	private static class BarraStockRenderer extends JComponent implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
		private int stock;
		private int stockmin;
		private Color fondo;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			stock = (int) table.getValueAt(row, 4);
			stockmin = (int) table.getValueAt(row, 5);
			fondo = isSelected ? table.getSelectionBackground() : Color.WHITE; // fondo de la celda
			// Al pasar el ratón se ve el número exacto
			setToolTipText(stock + " unidades (mínimo " + stockmin + ")");
			return this;
		}
		
		//IA esta parte de pintar

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(fondo);
			g.fillRect(0, 0, getWidth(), getHeight());
			int alto = 8;
			pintarBarra(g, 8, (getHeight() - alto) / 2, getWidth() - 24, alto, stock, stockmin);
		}
	}
	

		private static class EstadoRenderer extends JComponent implements TableCellRenderer {

			private static final long serialVersionUID = 1L;
			private String texto;
			private Color fondo;
			private Color fondoEtiqueta;
			private Color colorTexto;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				int stock = (int) table.getValueAt(row, 4);
				int stockmin = (int) table.getValueAt(row, 5);
				texto = textoEstado(stock, stockmin);
				fondo = isSelected ? table.getSelectionBackground() : Color.WHITE; // fondo de la celda
				fondoEtiqueta = colorFondoEstado(texto);
				colorTexto = colorTextoEstado(texto);
				setFont(table.getFont().deriveFont(Font.BOLD, 12f));
				return this;
			}
			
			//PAINT IA

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.setColor(fondo);
				g2.fillRect(0, 0, getWidth(), getHeight());

				// Etiqueta rectangular (esquinas cuadradas)
				int anchoEtiqueta = 76;
				int altoEtiqueta = 22;
				int x = 8;
				int y = (getHeight() - altoEtiqueta) / 2;
				g2.setColor(fondoEtiqueta);
				g2.fillRect(x, y, anchoEtiqueta, altoEtiqueta);

				// Texto centrado dentro de la etiqueta
				g2.setFont(getFont());
				FontMetrics fm = g2.getFontMetrics();
				int tx = x + (anchoEtiqueta - fm.stringWidth(texto)) / 2;
				int ty = y + (altoEtiqueta - fm.getHeight()) / 2 + fm.getAscent();
				g2.setColor(colorTexto);
				g2.drawString(texto, tx, ty);
			}
		}
		private static Color colorFondoEstado(String estado) {
			switch (estado) {
				case "Agotado": return AGOTADO_FONDO;
				case "Bajo":    return BAJO_FONDO;
				default:        return OK_FONDO;
			}
		}
		
		private static Color colorTextoEstado(String estado) {
			switch (estado) {
				case "Agotado": return AGOTADO_TEXTO;
				case "Bajo":    return BAJO_TEXTO;
				default:        return OK_TEXTO;
			}
		}
		
		private void mostrarInfoProducto(int fila) {

			JPanel contenido = crearPanelInfoProducto(fila);
		

			JOptionPane.showOptionDialog(
					this,
					contenido,
					"Información del producto",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE,  
					null,
					new Object[] { "Cerrar" },  
					"Cerrar");
		}
		
		private JPanel crearPanelInfoProducto(int fila) {

			int id = (int) tablaProductos.getValueAt(fila, 0);
			String nombre = (String) tablaProductos.getValueAt(fila, 1);
			String categoria = (String) tablaProductos.getValueAt(fila, 2);
			double precio = (double) tablaProductos.getValueAt(fila, 3);
			int stock = (int) tablaProductos.getValueAt(fila, 4);
			int stockmin = (int) tablaProductos.getValueAt(fila, 5);
			String estado = textoEstado(stock, stockmin);

			Color gris = new Color(120, 120, 135);

			JPanel contenido = new JPanel(new BorderLayout(24, 0));
			contenido.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

			JLabel foto = new JLabel("foto del producto", JLabel.CENTER);
			foto.setPreferredSize(new Dimension(180, 210));
			foto.setOpaque(true);
			foto.setBackground(new Color(232, 228, 250));
			foto.setForeground(new Color(110, 85, 190));
			foto.setBorder(BorderFactory.createDashedBorder( new Color(165, 145, 225), 1, 4, 3, false));
			
			String archivoFoto = null;
			switch (id) {
				case 1:  archivoFoto = "cocacola.png"; break;
				case 2:  archivoFoto = "cocazero.png"; break;
				case 3:  archivoFoto = "fanta.jpeg"; break;
				case 4:  archivoFoto = "fantal.jpeg"; break;
				case 5:  archivoFoto = "sprite.jpg"; break;
				case 6:  archivoFoto = "redbull.png"; break;
				case 7:  archivoFoto = "monster.jpg"; break;
				case 8:  archivoFoto = "redbull.jpeg"; break;   
				case 9:  archivoFoto = "heineken.png"; break;
				case 10: archivoFoto = "estrella.png"; break;
				case 11: archivoFoto = "mahou.png"; break;
				case 12: archivoFoto = "cervezasin.png"; break;
				case 13: archivoFoto = "vodka.png"; break;
				case 14: archivoFoto = "ron.png"; break;
				case 15: archivoFoto = "whisky.png"; break;
				case 16: archivoFoto = "ginebra.png"; break;
				case 17: archivoFoto = "tekila.jpg"; break;
				case 18: archivoFoto = "jager.jpg"; break;
				case 19: archivoFoto = "agua.jpeg"; break;
				case 20: archivoFoto = "zumo.png"; break;      
				case 21: archivoFoto = "tonica.png"; break;
				case 22: archivoFoto = "hielo.jpg"; break;
			}

			// Pongo la foto en lugar del texto "foto del producto"
			ImageIcon icono = new ImageIcon("Resources/Images/" + archivoFoto);
	
			Image escalada = icono.getImage().getScaledInstance(180, 210, Image.SCALE_SMOOTH);
			foto.setIcon(new ImageIcon(escalada));
			foto.setText("");
			

			JPanel panelFoto = new JPanel(new BorderLayout());
			panelFoto.add(foto, BorderLayout.NORTH);
			contenido.add(panelFoto, BorderLayout.WEST);

			
			JPanel datos = new JPanel();
			datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));

			JLabel lblNombre = new JLabel(nombre);
			lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 24));

			JLabel lblCategoria = new JLabel(categoria + " · ID " + id);
			lblCategoria.setFont(lblCategoria.getFont().deriveFont(Font.PLAIN, 14));
			lblCategoria.setForeground(gris);

			JLabel lblPrecio = new JLabel(String.format("%.2f €", precio));
			lblPrecio.setFont(lblPrecio.getFont().deriveFont(Font.BOLD, 15));

			JLabel lblStock = new JLabel(stock + " unidades (mínimo " + stockmin + ")");
			lblStock.setFont(lblStock.getFont().deriveFont(Font.PLAIN, 15));
			
			// Barra de stock 
			JComponent barra = new JComponent() {
				private static final long serialVersionUID = 1L;
				@Override
				protected void paintComponent(Graphics g) {
					pintarBarra(g, 0, 0, getWidth(), getHeight(), stock, stockmin);
				}
			};
			barra.setPreferredSize(new Dimension(320, 8));
			barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

			// Etiqueta de estado 
			JLabel lblEstado = new JLabel(estado.equals("OK") ? "Stock correcto"
					: estado.equals("Bajo") ? "Stock bajo" : "Agotado");
			lblEstado.setOpaque(true);
			lblEstado.setBackground(colorFondoEstado(estado));
			lblEstado.setForeground(colorTextoEstado(estado));
			lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13));
			lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
			
			JLabel lblTituloPrecio = new JLabel("PRECIO");
			lblTituloPrecio.setFont(lblTituloPrecio.getFont().deriveFont(Font.PLAIN, 12));
			
			lblTituloPrecio.setForeground(gris);

			JLabel lblTituloStock = new JLabel("STOCK");
			
			lblTituloStock.setFont(lblTituloStock.getFont().deriveFont(Font.PLAIN, 12));
			
			lblTituloStock.setForeground(gris);

			JLabel lblTituloEstado = new JLabel("ESTADO");
			
			lblTituloEstado.setFont(lblTituloEstado.getFont().deriveFont(Font.PLAIN, 12));
			lblTituloEstado.setForeground(gris);
			
			lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblTituloPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblTituloStock.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);
			barra.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblTituloEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

			datos.add(lblNombre);
			datos.add(lblCategoria);
			datos.add(Box.createVerticalStrut(14));
			datos.add(lblTituloPrecio);
			datos.add(lblPrecio);
			datos.add(Box.createVerticalStrut(12));
			datos.add(lblTituloStock);
			datos.add(lblStock);
			datos.add(Box.createVerticalStrut(6));
			datos.add(barra);
			datos.add(Box.createVerticalStrut(12));
			datos.add(lblTituloEstado);
			datos.add(Box.createVerticalStrut(4));
			datos.add(lblEstado);

			contenido.add(datos, BorderLayout.CENTER);
			return contenido;
		}

	//CREO EL THREAD PARA DIMULAR EL CONSUMO DE PRODUCTOS
	private class HiloConsumo extends Thread {

		@Override
		public void run() {

			// Simulo 50 intentos de venta
			for (int i = 0; i < 50 && !isInterrupted(); i++) {

				// Selecciono un producto aleatoriamente
				int posicion = (int) (Math.random() * productos.size());

				// Actualizo el inventario desde el Thread
				SwingUtilities.invokeLater(() -> {

					if (isInterrupted()) {
						return;
					}

					Producto p = productos.get(posicion);

					// Compruebo que quedan existencias
					if (p.getStock() > 0) {

						// Descuento una unidad
						p.setStock(p.getStock() - 1);

						// Actualizo la tabla
						modeloDatosProductos.setValueAt(
								p.getStock(), posicion, 4);
						
						// Actualizo el renderizado de toda la fila, para que cambie el color de toda la fila del producto si es necesario 
						modeloDatosProductos.fireTableRowsUpdated(posicion, posicion);

						// Actualizo la informacion el el label de información de la ventana JFrameInventario
						lblSeleccion.setText(
								"Venta: " + p.getNombre()
								+ " - Stock restante: " + p.getStock());
					}
				});

				// Espero entre ventas 1000
				try {
					Thread.sleep(1000);

				} catch (InterruptedException e) {
					interrupt();
					break;
				}
			}

			// Al terminar, recupero el estado de los botones
			SwingUtilities.invokeLater(() -> {

				btnIniciar.setEnabled(true);
				btnParar.setEnabled(false);
				//Activo o desactivo el botón Actualizar stock dependiendo de si hay una fila seleccionada en la JTable o no.
				btnActualizarStock.setEnabled(
						tablaProductos.getSelectedRow() != -1);
			});
		}
	} //FIN THREAS
 
	

}//FIN CLASE

