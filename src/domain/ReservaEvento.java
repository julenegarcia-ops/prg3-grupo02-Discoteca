package domain;

public class ReservaEvento {

	private Evento evento;
	private String cliente;
	private String dni;
	public ReservaEvento(Evento evento, String cliente, String dni) {
		super();
		this.evento = evento;
		this.cliente = cliente;
		this.dni = dni;
	}
	public Evento getEvento() {
		return evento;
	}
	
	public String getCliente() {
		return cliente;
	}

	public String getDni() {
		return dni;
	}
	
	public int getPersonas() {
		return evento.getAforo();
	}
	@Override
	   public String toString() {
        return evento.getNombre() + " " + evento.getFecha() + " - " + cliente + " (" + getPersonas() + " pers.)";
}
	
	
	
}
