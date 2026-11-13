package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import domain.Producto;

public class JFrameFacturas extends JFrame {
    private JPanel pNorte, pEncabezado, pDatos, pDatos1, pDatos2;
    private JPanel pCentro, pNuevoProducto, pSur, pTotal, pBotones;
    private JLabel lblInformacion, lblNumeroFactura;
    private JLabel lblFecha, lblHora, lblMesa, lblCliente, lblDNI;
    private JLabel lblBase, lblBaseDato, lblIVA, lblIVADato;
    private JLabel lblTotal, lblTotalDato;
    
    // Solo necesitamos nombre y cantidad
    private JLabel lblProdNombre, lblProdCantidad;
    private JTextField txtProdNombre, txtProdCantidad;
    private JButton btnAnadirProducto;

    // Contador correlativo para el número de factura
    private int contadorFactura = 1;

    private JTextField txtNumeroFactura, txtFecha, txtHora;
    private JTextField txtMesa, txtCliente, txtDNI;
    private ArrayList<Producto> lProducto;
    private ArrayList<Producto> catalogoProductos;
    private ModeloTablaFacturas modelo;
    private JTable tabla;
    private JScrollPane spConsumo;
    private JButton btnImprimir, btnGmail, btnNuevaFactura, btnCerrar;

    public JFrameFacturas() {
        super();
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lProducto = new ArrayList<Producto>();
        cargarCatalogo();

        // pNorte
        pNorte = new JPanel(new GridLayout(2, 1));
        pNorte.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(pNorte, BorderLayout.NORTH);

        pEncabezado = new JPanel();
        pEncabezado.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pNorte.add(pEncabezado);

        lblInformacion = new JLabel("Información de la discoteca - Bilbao");
        lblInformacion.setHorizontalAlignment(JLabel.CENTER);
        pEncabezado.add(lblInformacion);

        // pDatos
        pDatos = new JPanel(new GridLayout(2, 1));
        pDatos.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pNorte.add(pDatos);

        pDatos1 = new JPanel();
        pDatos.add(pDatos1);

        lblNumeroFactura = new JLabel("Nº Factura: ");
        pDatos1.add(lblNumeroFactura);

        txtNumeroFactura = new JTextField(10);
        txtNumeroFactura.setEditable(false);
        txtNumeroFactura.setText(generarNumeroFactura());
        pDatos1.add(txtNumeroFactura);

        SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
        Date ahora = new Date();

        lblFecha = new JLabel("Fecha: ");
        pDatos1.add(lblFecha);

        txtFecha = new JTextField(7);
        txtFecha.setText(sdfFecha.format(ahora));
        txtFecha.setEditable(false);
        pDatos1.add(txtFecha);

        lblHora = new JLabel("Hora: ");
        pDatos1.add(lblHora);

        txtHora = new JTextField(7);
        txtHora.setText(sdfHora.format(ahora));
        txtHora.setEditable(false);
        pDatos1.add(txtHora);

        lblMesa = new JLabel("Mesa: ");
        pDatos1.add(lblMesa);

        txtMesa = new JTextField(8);
        pDatos1.add(txtMesa);

        pDatos2 = new JPanel();
        pDatos.add(pDatos2);

        lblCliente = new JLabel("Cliente: ");
        pDatos2.add(lblCliente);

        txtCliente = new JTextField(30);
        pDatos2.add(txtCliente);

        lblDNI = new JLabel("DNI/CIF: ");
        pDatos2.add(lblDNI);

        txtDNI = new JTextField(9);
        pDatos2.add(txtDNI);

        // pCentro
        pCentro = new JPanel(new BorderLayout());
        pCentro.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(pCentro, BorderLayout.CENTER);

        pNuevoProducto = new JPanel();
        pNuevoProducto.setBorder(BorderFactory.createTitledBorder("Añadir artículo a la factura"));

        lblProdNombre = new JLabel("Producto:");
        txtProdNombre = new JTextField(15);

        lblProdCantidad = new JLabel("Cantidad:");
        txtProdCantidad = new JTextField(5);

        btnAnadirProducto = new JButton("Añadir");

        pNuevoProducto.add(lblProdNombre);
        pNuevoProducto.add(txtProdNombre);
        pNuevoProducto.add(lblProdCantidad);
        pNuevoProducto.add(txtProdCantidad);
        pNuevoProducto.add(btnAnadirProducto);

        pCentro.add(pNuevoProducto, BorderLayout.NORTH);

        btnAnadirProducto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anadirArticulo();
            }
        });

        modelo = new ModeloTablaFacturas(lProducto);
        tabla = new JTable(modelo);
        tabla.setShowGrid(true);
        tabla.setGridColor(Color.GRAY);

        spConsumo = new JScrollPane(tabla);
        spConsumo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pCentro.add(spConsumo, BorderLayout.CENTER);

        // pSur
        pSur = new JPanel(new GridLayout(2, 1, 5, 5));
        pSur.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(pSur, BorderLayout.SOUTH);

        pTotal = new JPanel();
        pTotal.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pSur.add(pTotal);

        lblBase = new JLabel("Base Imponible: ");
        pTotal.add(lblBase);

        lblBaseDato = new JLabel("          ");
        lblBaseDato.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pTotal.add(lblBaseDato);

        lblIVA = new JLabel("IVA(21%): ");
        pTotal.add(lblIVA);

        lblIVADato = new JLabel("          ");
        lblIVADato.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pTotal.add(lblIVADato);

        lblTotal = new JLabel("Total a pagar: ");
        pTotal.add(lblTotal);

        lblTotalDato = new JLabel("          ");
        lblTotalDato.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pTotal.add(lblTotalDato);

        // pBotones
        pBotones = new JPanel();
        pSur.add(pBotones);

        btnImprimir = new JButton("Imprimir");
        pBotones.add(btnImprimir);
        btnImprimir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tabla.print();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error al imprimir");
                }
            }
        });

        btnGmail = new JButton("Enviar por mail");
        pBotones.add(btnGmail);
        btnGmail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Preparando envío de factura");
            }
        });

        btnNuevaFactura = new JButton("Nueva factura");
        pBotones.add(btnNuevaFactura);
        btnNuevaFactura.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opcion = JOptionPane.showConfirmDialog(JFrameFacturas.this,
                        "¿Seguro que quieres empezar una nueva factura?", "Nueva factura", JOptionPane.YES_NO_OPTION);
                if (opcion != JOptionPane.YES_OPTION) {
                    return;
                }
                txtNumeroFactura.setText(generarNumeroFactura());
                txtMesa.setText("");
                txtCliente.setText("");
                txtDNI.setText("");
                
                SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
                Date ahora = new Date();
                txtFecha.setText(sdfFecha.format(ahora));
                txtHora.setText(sdfHora.format(ahora));
                
                lProducto.clear();
                modelo.fireTableDataChanged();
                calcularTotales();
                txtMesa.requestFocus();
            }
        });

        btnCerrar = new JButton("Cerrar");
        pBotones.add(btnCerrar);
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        calcularTotales();
        setVisible(true);
    }

    //Generado por Inteligencia Artificial
    private String generarNumeroFactura() {
        SimpleDateFormat sdfFechaFactura = new SimpleDateFormat("yyyyMMdd");
        String fechaStr = sdfFechaFactura.format(new Date());
        
        String numeroFormateado = "";
        if (contadorFactura < 10) {
            numeroFormateado = "000" + contadorFactura;
        } else if (contadorFactura < 100) {
            numeroFormateado = "00" + contadorFactura;
        } else if (contadorFactura < 1000) {
            numeroFormateado = "0" + contadorFactura;
        } else {
            numeroFormateado = "" + contadorFactura;
        }
        
        contadorFactura++;
        return "F-" + fechaStr + "-" + numeroFormateado;
    }
    
    //Generado por Inteligencia Artificial
    private void cargarCatalogo() {
        catalogoProductos = new ArrayList<Producto>();
        catalogoProductos.add(new Producto(1, "Copa de ron", "Bebida", 10, 20, 0));
        catalogoProductos.add(new Producto(2, "Coca-Cola", "Refresco", 4, 30, 0));
        catalogoProductos.add(new Producto(3, "Cerveza", "Bebida", 5, 40, 0));
    }
    
    //Generado por Inteligencia Artificial
    private void anadirArticulo() {
        String nombre = txtProdNombre.getText().trim();
        String cantidadStr = txtProdCantidad.getText().trim();

        if (nombre.isEmpty() || cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce el nombre del producto y la cantidad.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto encontrado = null;
            for (Producto p : catalogoProductos) {
                if (p.getNombre().equalsIgnoreCase(nombre)) {
                    encontrado = p;
                    break;
                }
            }

            if (encontrado == null) {
                JOptionPane.showMessageDialog(this, "El producto '" + nombre + "' no existe en el catálogo.",
                        "Producto no encontrado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto paraFactura = new Producto(
                encontrado.getId(),
                encontrado.getNombre(),
                encontrado.getCategoria(),
                encontrado.getPrecio(),
                cantidad,
                0
            );

            lProducto.add(paraFactura);
            modelo.fireTableDataChanged();
            calcularTotales();

            txtProdNombre.setText("");
            txtProdCantidad.setText("");
            txtProdNombre.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero válido.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Generado por Inteligencia Artificial
    public void calcularTotales() {
        double base = modelo.calcularBase();
        double iva = base * 0.21;
        double total = base + iva;
        lblBaseDato.setText((Math.round(base * 100.0) / 100.0) + " €");
        lblIVADato.setText((Math.round(iva * 100.0) / 100.0) + " €");
        lblTotalDato.setText((Math.round(total * 100.0) / 100.0) + " €");
    }

    public static void main(String[] args) {
        new JFrameFacturas();
    }
}