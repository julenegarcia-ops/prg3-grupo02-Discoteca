package domain;

public class Evento {

	private int id;
	private String nombre;
	private String fecha;
	private int aforo;
	private double ajustePrecio; // 1.0 normal, 0.8 descuento, 1.2 recargo

	public Evento(int id, String nombre, String fecha, int aforo, double ajustePrecio) {
		this.id = id;
		this.nombre = nombre;
		this.fecha = fecha;
		this.aforo = aforo;
		this.ajustePrecio = ajustePrecio;
	}

	public int getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getFecha() {
		return fecha;
	}

	public int getAforo() {
		return aforo;
	}

	public double getAjustePrecio() {
		return ajustePrecio;
	}

	// lo que se ve en el combo y en la tabla
	@Override
	public String toString() {
		return nombre + " (" + fecha + ")";
	}
}
