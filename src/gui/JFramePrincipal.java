package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

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
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));

        // Título
        JLabel lblTitulo = new JLabel(
                "GESTIÓN DE DISCOTECA",
                SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 26));

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton btnEventos = new JButton("Gestión de eventos");
        JButton btnEntradas = new JButton("Venta de entradas");
        JButton btnInventario = new JButton("Inventario");
        JButton btnEmpleados = new JButton("Empleados y turnos");
        JButton btnVIP = new JButton("Clientes VIP y palcos");

        panelBotones.add(btnEventos);
        panelBotones.add(btnEntradas);
        panelBotones.add(btnInventario);
        panelBotones.add(btnEmpleados);
        panelBotones.add(btnVIP);

        panelPrincipal.add(panelBotones, BorderLayout.CENTER);

        // Eventos de los botones
        btnEventos.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Gestión de eventos");
        });

        btnEntradas.addActionListener(e -> {JOptionPane.showMessageDialog(this, "Venta de entradas");
        });

        btnInventario.addActionListener(e -> {  JOptionPane.showMessageDialog(this, "Gestión de inventario");
        });

        btnEmpleados.addActionListener(e -> { JOptionPane.showMessageDialog(this,  "Gestión de empleados y turnos");
        });

        btnVIP.addActionListener(e -> {JOptionPane.showMessageDialog(this,    "Gestión de clientes VIP y palcos");
        });

        add(panelPrincipal);
    }
}

