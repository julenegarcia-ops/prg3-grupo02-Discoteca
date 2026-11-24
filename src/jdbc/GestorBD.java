package jdbc;
//ESTE GESTOR SE ENCARGA DE CONECTAR INVENTARIO Y RESERVADOS

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import domain.Producto;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

public class GestorBD {

	private static final String SQLITE_FILE = "./src/jdbc/discoteca.db";
	private static final String CONNECTION_STRING = "jdbc:sqlite:" + SQLITE_FILE;

	public GestorBD() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.format("* Error al cargar el driver de la BBDD: %s\n", e.getMessage());
		}
	}

	public void inicializarBD() {
		String sqlTabla = "CREATE TABLE IF NOT EXISTS PRODUCTO ("
				+ "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "NOMBRE TEXT NOT NULL UNIQUE, "
				+ "CATEGORIA TEXT NOT NULL, "
				+ "PRECIO REAL NOT NULL, "
				+ "STOCK INTEGER NOT NULL, "
				+ "STOCK_MIN INTEGER NOT NULL)";

		String sqlCount = "SELECT COUNT(*) FROM PRODUCTO";

		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
			 Statement stmt = con.createStatement()) {

			stmt.execute(sqlTabla);

			try (ResultSet rs = stmt.executeQuery(sqlCount)) {
				if (rs.next() && rs.getInt(1) == 0) {
					insertarDatosIniciales(con);
				}
			}
		} catch (SQLException e) {
			System.err.format("* Error inicializando la BBDD: %s\n", e.getMessage());
		}
	}

	private void insertarDatosIniciales(Connection con) throws SQLException {
		String sqlInsert = "INSERT INTO PRODUCTO (NOMBRE, CATEGORIA, PRECIO, STOCK, STOCK_MIN) VALUES (?, ?, ?, ?, ?)";
		Object[][] productosIniciales = {
			{"Coca-Cola", "Refrescos", 3.0, 80, 15},
			{"Coca-Cola Zero", "Refrescos", 3.0, 45, 15},
			{"Fanta Naranja", "Refrescos", 3.0, 12, 15},
			{"Fanta Limón", "Refrescos", 3.0, 30, 10},
			{"Sprite", "Refrescos", 3.0, 0, 10},
			{"Red Bull", "Energéticas", 4.5, 60, 20},
			{"Monster", "Energéticas", 4.0, 8, 15},
			{"Red Bull Sugarfree", "Energéticas", 4.5, 20, 10},
			{"Heineken", "Cervezas", 4.0, 100, 25},
			{"Estrella Galicia", "Cervezas", 3.5, 20, 25},
			{"Mahou", "Cervezas", 3.5, 55, 20},
			{"Cerveza sin alcohol", "Cervezas", 3.0, 0, 10},
			{"Vodka Absolut", "Licores", 8.0, 15, 5},
			{"Ron Barceló", "Licores", 8.0, 4, 6},
			{"Whisky Ballantines", "Licores", 9.0, 12, 5},
			{"Ginebra Beefeater", "Licores", 9.0, 3, 5},
			{"Tequila José Cuervo", "Licores", 7.0, 10, 5},
			{"Jägermeister", "Licores", 6.0, 0, 5},
			{"Agua mineral", "Otros", 2.0, 90, 20},
			{"Zumo de piña", "Otros", 3.0, 5, 10},
			{"Tónica", "Refrescos", 2.5, 25, 10},
			{"Hielo", "Otros", 1.0, 40, 15}
		};

		try (PreparedStatement pst = con.prepareStatement(sqlInsert)) {
			for (Object[] p : productosIniciales) {
				pst.setString(1, (String) p[0]);
				pst.setString(2, (String) p[1]);
				pst.setDouble(3, (Double) p[2]);
				pst.setInt(4, (Integer) p[3]);
				pst.setInt(5, (Integer) p[4]);
				pst.executeUpdate();
			}
		}
		
		
	}
	
	public List<Producto> cargarproductos() {
		List<Producto> productos = new ArrayList<>();
		String sql = "SELECT * FROM PRODUCTO";

		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
			 PreparedStatement pst = con.prepareStatement(sql);
			 ResultSet rs = pst.executeQuery()) {

			while (rs.next()) {
				int id = rs.getInt("ID");
				String nombre = rs.getString("NOMBRE");
				String categoria = rs.getString("CATEGORIA");
				double precio = rs.getDouble("PRECIO");
				int stock = rs.getInt("STOCK");
				int stockmin = rs.getInt("STOCK_MIN");

				productos.add(new Producto(id, nombre, categoria, precio, stock, stockmin));
			}
		} catch (SQLException e) {
			System.err.format("* Error recuperando productos: %s.\n", e.getMessage());
		}

		return productos;
	}

	public boolean actualizarstock(int idprod, int nuevoStock) {
		String sqlUpdate = "UPDATE PRODUCTO SET STOCK = ? WHERE ID = ?";
		boolean actualizado = false;

		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
			 PreparedStatement pst = con.prepareStatement(sqlUpdate)) {

			pst.setInt(1, nuevoStock);
			pst.setInt(2, idprod);

			int filas = pst.executeUpdate();
			if (filas == 1) {
				System.out.printf("[DB] Stock actualizado para ID %d.%n", idprod);
				actualizado = true;
			}
		} catch (SQLException e) {
			System.err.format("* Error actualizando stock: %s.\n", e.getMessage());
		}

		return actualizado;
	}
	
	public boolean insertarproducto(Producto producto) {
		String sqlinsertar = "INSERT INTO PRODUCTO (NOMBRE, CATEGORIA, PRECIO, STOCK, STOCK_MIN) VALUES (?, ?, ?, ?, ?)";
		boolean insertado = false;

		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
			 PreparedStatement pst = con.prepareStatement(sqlinsertar)) {

			pst.setString(1, producto.getNombre());
			pst.setString(2, producto.getCategoria());
			pst.setDouble(3, producto.getPrecio());
			pst.setInt(4, producto.getStock());
			pst.setInt(5, producto.getStockmin());

			if (pst.executeUpdate() == 1) {
				System.out.printf("[DB] Producto '%s' insertado correctamente.%n", producto.getNombre());
				insertado = true;
			}
		} catch (SQLException e) {
			System.err.format("* Error insertando producto '%s': %s.\n", producto.getNombre(), e.getMessage());
		}

		return insertado;
	}

	public boolean  conectarstockynombre(String nombreproducto, int cantidad) {
		String sqlelegido = "SELECT ID, STOCK FROM PRODUCTO WHERE NOMBRE = ?";
		String sqlupdate = "UPDATE PRODUCTO SET STOCK = ? WHERE ID = ?";

		boolean actualizado = false;
		int idprod = -1;
		int stock = 0;

		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
			 PreparedStatement select = con.prepareStatement(sqlelegido);
			 PreparedStatement update = con.prepareStatement(sqlupdate)) {

			select.setString(1, nombreproducto);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					idprod = rs.getInt("ID");
					stock = rs.getInt("STOCK");
				} else {
					System.err.printf("[DB] No existe el producto '%s'.%n", nombreproducto);
					return false;
				}
			}

			if (stock < cantidad) {
				System.err.printf("[DB] Stock insuficiente para '%s' (Actual: %d, Pedido: %d).%n",
						nombreproducto, stock, cantidad);
				return false;
			}

			int nuevoStock = stock - cantidad;
			update.setInt(1, nuevoStock);
			update.setInt(2, idprod);

			int rowaf = update.executeUpdate();
			if (rowaf == 1) {
				System.out.printf("[DB] Se han descontado %d unidades de '%s'. Stock restante: %d.%n",
						cantidad, nombreproducto, nuevoStock);
				actualizado = true;
			}

		} catch (SQLException e) {
			System.err.printf("[DB] Error al descontar stock de '%s': %s.%n", nombreproducto, e.getMessage());
		}

		return actualizado;
	}
}