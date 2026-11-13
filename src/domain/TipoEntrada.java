package domain;

public enum TipoEntrada {
	NORMAL(15.0), VIP(25.0);

	private double precio;

	TipoEntrada(double precio) {
		this.precio = precio;
	}

	public double getPrecio() {
		return precio;
	}
}
