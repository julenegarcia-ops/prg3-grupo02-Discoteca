package gui;


import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
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

	public JFrameInventario() {
		
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
		                      "PRECIO", "STOCK", "STOCK MÍNIMO"));

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
		
		TableCellRenderer mirenderer = new TableCellRenderer() {

		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value,
		            boolean isSelected, boolean hasFocus, int row, int column) {

		        JLabel result = new JLabel();

		        // Mostrar el valor de la celda
		        result.setText(value != null ? value.toString() : "");

		        // Obtener el stock y el stock mínimo
		        int filaModelo = table.convertRowIndexToModel(row);

		        int stock = (int) modeloDatosProductos.getValueAt(filaModelo, 4);
		        int stockmin = (int) modeloDatosProductos.getValueAt(filaModelo, 5);

		        result.setOpaque(true);

		        // Coloreo según el stock
		        if (stock == 0) { //Rojo, cuando no hay stock del producto
		            result.setBackground(new Color(255, 180, 180));

		        } else if (stock <= stockmin) { 
		        	//Amarillo - hay stock pero menos que el sotck mínimo que debería haber
		            result.setBackground(new Color(255, 240, 170));

		        } else { 
		        	//Verde, hay stock y es mayor que el stock mínimo
		            result.setBackground(new Color(210, 245, 210));
		        }

		        // Si la fila está seleccionada
		        if (isSelected) { 
		            result.setBackground(table.getSelectionBackground());
		            result.setForeground(table.getSelectionForeground());
		        }

		        return result;
		    }
		};

		// Aplico el renderer a todas las columnas
		for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
		    tablaProductos.getColumnModel().getColumn(i)
		            .setCellRenderer(mirenderer);
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
		this.tablaProductos.getSelectionModel().addListSelectionListener(e -> {

		    if (!e.getValueIsAdjusting()) {

		        int fila = tablaProductos.getSelectedRow();

		        if (fila != -1) {

		            int filaModelo = tablaProductos.convertRowIndexToModel(fila);

		            String nombre = (String) modeloDatosProductos.getValueAt(
		                    filaModelo, 1);

		            int stock = (int) modeloDatosProductos.getValueAt(
		                    filaModelo, 4);

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

		        int filaModelo = tablaProductos.convertRowIndexToModel(fila);

		        int id = (int) modeloDatosProductos.getValueAt(filaModelo, 0);

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
		            modeloDatosProductos.setValueAt(nuevoStock, filaModelo, 4);

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

						int filaModelo =tablaProductos.convertRowIndexToModel(fila);

						int id = (int) modeloDatosProductos.getValueAt(filaModelo, 0);

						String nombre = (String) modeloDatosProductos.getValueAt(filaModelo, 1);

						int stock = (int) modeloDatosProductos.getValueAt(filaModelo, 4);

						JOptionPane.showMessageDialog(
								JFrameInventario.this,
								"ID: " + id
								+ "\nProducto: " + nombre
								+ "\nStock: " + stock,
								"Información del producto",
								JOptionPane.INFORMATION_MESSAGE);
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

