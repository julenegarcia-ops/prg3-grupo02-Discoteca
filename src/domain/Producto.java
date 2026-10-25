package domain;

public class Producto {
	
	private int id;
	private String nombre;
	private String categoria;
	private double precio;
	private int stock;
	private int stockmin;
	
	public Producto(int id, String nombre, String categoria, double precio, int stock, int stockmin) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.categoria = categoria;
		this.precio = precio;
		this.stock = stock;
		this.stockmin = stockmin;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getStockmin() {
		return stockmin;
	}

	public void setStockmin(int stockmin) {
		this.stockmin = stockmin;
	}

	@Override
	public String toString() {
		return "Producto [id=" + id + ", nombre=" + nombre + ", categoria=" + categoria + ", precio=" + precio
				+ ", stock=" + stock + ", stockmin=" + stockmin + "]";
	}
	
	
	

}
