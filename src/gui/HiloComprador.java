package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import domain.Canal;
import domain.Entrada;
import domain.Evento;
import domain.GestorEntradas;
import domain.TipoEntrada;

public class HiloComprador extends Thread {

	//IAG (herramienta: Claude): nombres y DNI de prueba de los compradores
	private static final String[] NOMBRES = { "Ane", "Jon", "Maite", "Unai", "Leire", "Mikel", "Nerea", "Asier" };

	private GestorEntradas gestor;
	private JFrameEntradas ventana;
	private Random random = new Random();

	public HiloComprador(GestorEntradas gestor, JFrameEntradas ventana) {
		this.gestor = gestor;
		this.ventana = ventana;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				Thread.sleep(500 + random.nextInt(1000));
			} catch (InterruptedException e) {
				break; // le han dado a cerrar web mientras dormia
			}

			List<Evento> conPlazas = new ArrayList<>();
			for (Evento ev : gestor.getEventos()) {
				if (gestor.plazasLibres(ev) > 0) {
					conPlazas.add(ev);
				}
			}

			if (conPlazas.isEmpty()) {
				SwingUtilities.invokeLater(() -> ventana.avisarSoldOut());
				break;
			}

			Evento evento = conPlazas.get(random.nextInt(conPlazas.size()));
			TipoEntrada tipo = random.nextBoolean() ? TipoEntrada.NORMAL : TipoEntrada.VIP;
			String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
			//IAG (herramienta: Claude)
			String dni = String.format("%08d", random.nextInt(100000000)) + (char) ('A' + random.nextInt(26));

			// puede devolver null si otro hilo se ha llevado la ultima plaza justo antes
			Entrada vendida = gestor.vender(evento, tipo, Canal.ONLINE, nombre, dni);
			if (vendida != null) {
				// el hilo no toca la tabla: se lo pide al EDT con invokeLater
				SwingUtilities.invokeLater(() -> ventana.actualizarTabla());
			}
		}
	}
}
