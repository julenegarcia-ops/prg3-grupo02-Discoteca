package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import domain.Entrada;

public class EntradaTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final String[] COLUMNAS = { "Nº", "Nombre", "DNI", "Tipo", "Evento", "Canal", "Precio" };

	private List<Entrada> entradas;

	public EntradaTableModel(List<Entrada> entradas) {
		this.entradas = entradas;
	}

	@Override
	public int getRowCount() {
		return entradas.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNAS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNAS[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Entrada e = entradas.get(rowIndex);

		return switch (columnIndex) {
			case 0 -> e.getNumero();
			case 1 -> e.getNombre();
			case 2 -> e.getDni();
			case 3 -> e.getTipo();
			case 4 -> e.getEvento();
			case 5 -> e.getCanal();
			case 6 -> e.getPrecio() + " €";
			default -> null;
		};
	}

	public Entrada getEntrada(int fila) {
		return entradas.get(fila);
	}
}
