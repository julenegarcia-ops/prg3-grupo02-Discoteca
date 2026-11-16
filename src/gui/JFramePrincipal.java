package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class JFramePrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    public JFramePrincipal() {

        setTitle("Gestión de la Discoteca");
        setSize(640, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        //Colores
        Color azulOscuro = new Color(24, 28, 50);
        Color borde = new Color(215, 215, 222);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel panelCabecera = new JPanel(new BorderLayout(0, 12));
        panelCabecera.setOpaque(false);
        
        ImageIcon fotoDiscoteca = new ImageIcon("Resources/Images/LogoDiscoteca+.jpeg");
        //Para que escalone la imagen esta generado por IA
        Image fotoEscalada = fotoDiscoteca.getImage().getScaledInstance(-1, 170, Image.SCALE_SMOOTH);
        JLabel lblFoto = new JLabel(new ImageIcon(fotoEscalada), SwingConstants.CENTER);
        panelCabecera.add(lblFoto, BorderLayout.CENTER);

        // Título
        JLabel lblTitulo = new JLabel(
                "GESTIÓN DE DISCOTECA",
                SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 26));
        
        lblTitulo.setForeground(azulOscuro);
        panelCabecera.add(lblTitulo, BorderLayout.SOUTH);

        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton btnEventos = new JButton("Gestión de eventos", new ImageIcon("Resources/Images/eventos.png"));
        JButton btnEntradas = new JButton("Venta de entradas", new ImageIcon("Resources/Images/entradas.png"));
        JButton btnInventario = new JButton("Inventario",new ImageIcon("Resources/Images/inventario.png"));
        JButton btnFacturas = new JButton("Facturas",new ImageIcon("Resources/Images/facturas.png"));
        JButton btnVIP = new JButton("Clientes VIP y palcos", new ImageIcon("Resources/Images/vip.png"));

        JButton[] botones = { btnEventos, btnEntradas, btnInventario, btnFacturas, btnVIP };

        for (JButton b : botones) {
            b.setFont(new Font("Arial", Font.BOLD, 17));
            b.setForeground(azulOscuro);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createLineBorder(borde));
            b.setVerticalTextPosition(SwingConstants.BOTTOM);
            b.setHorizontalTextPosition(SwingConstants.CENTER);
 
        }
        
        JPanel fila1 = new JPanel(new GridLayout(1, 2, 10, 0));
        fila1.add(btnEventos);
        fila1.add(btnEntradas);

        JPanel fila2 = new JPanel(new GridLayout(1, 2, 10, 0));
        fila2.add(btnInventario);
        fila2.add(btnFacturas);
        
        panelBotones.add(fila1);
        panelBotones.add(fila2);
        panelBotones.add(btnVIP);

        panelPrincipal.add(panelBotones, BorderLayout.CENTER);

        // Eventos de los botones
     
        btnEventos.addActionListener(e -> {
            JFrameEventos ventana = new JFrameEventos(JFramePrincipal.this);
            ventana.setVisible(true);
        });
        
        btnEntradas.addActionListener(e -> {
        	JFrameEntradas ventana = new JFrameEntradas();
        	ventana.setVisible(true);
        });

        btnInventario.addActionListener(e -> {  
        	JFrameInventario ventana = new JFrameInventario(JFramePrincipal.this);
        	ventana.setVisible(true);
        	
        });

        btnFacturas.addActionListener(e -> {
        	JFrameFacturas ventana = new JFrameFacturas();
		ventana.setVisible(true);
        });

        btnVIP.addActionListener(e -> {
        	Jframereservados reservado = new Jframereservados();
        	reservado.setVisible(true);
        	
        });
        
        add(panelPrincipal);
    }
};

