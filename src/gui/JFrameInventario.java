package gui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import domain.Producto;

public class JFrameInventario extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private List<Producto>productos;
	
	

	public JFrameInventario() {
		
		
		JPanel principal= new JPanel(new GridLayout(2,1));
		
		JTable tablainventario= new JTable();
		JScrollPane scrollinventario= new JScrollPane(tablainventario);
		scrollinventario.setBorder(new TitledBorder("Lista de productos"));
		tablainventario.setFillsViewportHeight(true);
		
		this.add(principal);
		principal.add(scrollinventario);
		
		
		
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("INVENTARIO");
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		 new JFrameInventario();

	}

}
