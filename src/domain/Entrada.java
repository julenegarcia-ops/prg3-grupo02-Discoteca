package domain;

public class Entrada {

	private int numero;
	private Evento evento;
	private TipoEntrada tipo;
	private Canal canal;
	private String nombre;
	private String dni;
	private double precio;

	public Entrada(int numero, Evento evento, TipoEntrada tipo, Canal canal, String nombre, String dni, double precio) {
		this.numero = numero;
		this.evento = evento;
		this.tipo = tipo;
		this.canal = canal;
		this.nombre = nombre;
		this.dni = dni;
		this.precio = precio;
	}

	public int getNumero() {
		return numero;
	}

	public Evento getEvento() {
		return evento;
	}

	public TipoEntrada getTipo() {
		return tipo;
	}

	public Canal getCanal() {
		return canal;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDni() {
		return dni;
	}

	public double getPrecio() {
		return precio;
	}
}
