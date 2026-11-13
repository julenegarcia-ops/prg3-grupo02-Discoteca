
package gui;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import domain.Producto;

public class ModeloTablaFacturas extends DefaultTableModel {
    private ArrayList<Producto> lista;
    public ModeloTablaFacturas(ArrayList<Producto> lista) {
        this.lista = lista;
        addColumn("ID");
        addColumn("Nombre");
        addColumn("Categoría");
        addColumn("Precio");
        addColumn("Cantidad");
        addColumn("Importe");
        for(Producto p : lista) {
            Object[] fila = {
                p.getId(),
                p.getNombre(),
                p.getCategoria(),
                p.getPrecio(),
                1,
                p.getPrecio()
            };
            addRow(fila);
        }
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return columna == 4;
    }

    public double calcularBase() {
        double base = 0;
        for(int i = 0; i < getRowCount(); i++) {
            double precio = (double) getValueAt(i, 3);
            int cantidad = Integer.parseInt(getValueAt(i, 4).toString());
            base = base + precio * cantidad;
        }
        return base;
    }

    public void actualizarImportes() {
        for(int i = 0; i < getRowCount(); i++) {
            double precio = (double) getValueAt(i, 3);
            int cantidad = Integer.parseInt(getValueAt(i, 4).toString());
            setValueAt(precio * cantidad, i, 5);
        }
    }
}
