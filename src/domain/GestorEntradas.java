package domain;

import java.util.ArrayList;
import java.util.List;

public class GestorEntradas {

	private List<Evento> eventos = new ArrayList<>();
	private List<Entrada> entradas = new ArrayList<>();
	private int siguienteNumero = 1;

	public GestorEntradas() {
		//IAG (herramienta: Claude): eventos de prueba
		eventos.add(new Evento(1, "Halloween", "31-10-2026", 20, 1.0));
		eventos.add(new Evento(2, "Fiesta universitaria", "20-11-2026", 10, 0.8));
		eventos.add(new Evento(3, "Nochevieja", "31-12-2026", 15, 1.2));
	}

	public synchronized int plazasLibres(Evento evento) {
		int vendidas = 0;
		for (Entrada e : entradas) {
			if (e.getEvento() == evento) {
				vendidas++;
			}
		}
		return evento.getAforo() - vendidas;
	}

	public double calcularPrecio(Evento evento, TipoEntrada tipo) {
		return tipo.getPrecio() * evento.getAjustePrecio();
	}

	// VENDER
	// synchronized: solo entra un hilo a la vez, asi no se vende la misma plaza dos veces
	// devuelve null si no quedan plazas
	public synchronized Entrada vender(Evento evento, TipoEntrada tipo, Canal canal, String nombre, String dni) {
		if (plazasLibres(evento) <= 0) {
			return null;
		}
		Entrada nueva = new Entrada(siguienteNumero, evento, tipo, canal, nombre, dni, calcularPrecio(evento, tipo));
		siguienteNumero++;
		entradas.add(nueva);
		return nueva;
	}

	public synchronized void devolver(Entrada entrada) {
		entradas.remove(entrada);
	}

	public List<Evento> getEventos() {
		return eventos;
	}

	// devuelve una copia: la tabla lee la copia mientras los hilos siguen vendiendo en la original
	public synchronized List<Entrada> getEntradas() {
		return new ArrayList<>(entradas);
	}

	public boolean todoVendido() {
		for (Evento ev : eventos) {
			if (plazasLibres(ev) > 0) {
				return false;
			}
		}
		return true;
	}
}
