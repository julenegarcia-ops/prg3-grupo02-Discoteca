package domain;

public enum TipoEntrada {
	//IAG (herramienta: Claude): precios de prueba
	NORMAL(15.0), VIP(25.0);

	private double precio;

	TipoEntrada(double precio) {
		this.precio = precio;
	}

	public double getPrecio() {
		return precio;
	}
}
