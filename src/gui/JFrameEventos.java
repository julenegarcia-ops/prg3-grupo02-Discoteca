package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import domain.Evento;
import domain.ReservaEvento;


public class JFrameEventos extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final Color COLOR_LIBRE = new Color(76, 175, 80);    // verde (IA)
	private static final Color COLOR_RESERVADO = new Color(229, 57, 53); // rojo (IA)

	
	private static final String[] TIPOS_EVENTO = { "Fiesta ibicenca", "Fiesta tecno", "Fiesta de máscaras",
			"Fiesta de roles", "Graduación", "Privado" };

	
	private HashMap<String, String[]> fechasPorEvento = new HashMap<>();
	private HashMap<String, ReservaEvento[]> reservasPorEvento = new HashMap<>();
    private int siguienteId = 100;
	
	private String eventoSeleccionado = null;

	
	private JLabel lblTitulo;
	private JLabel lblEventoActual;
	private JLabel[] lblFechas = new JLabel[5];

	 private ReservaEvento reservaParaMontar = null;
     private HiloMontaje hiloMontaje;
     private JProgressBar barraMontaje;
     private JLabel lblEstadoMontaje;
     private JButton btnMontar;
     private JButton btnParar;

	
	
	
	//JFRAME
	public JFrameEventos() {

		setTitle("Gestión de eventos");
		setSize(900, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		cargarDatos();

		JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
		panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		
		lblTitulo = new JLabel("¡Elige el evento!", SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Arial", Font.BOLD, 36));
		panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

	
		panelPrincipal.add(crearPanelEventos(), BorderLayout.WEST);

		
		panelPrincipal.add(crearPanelFechas(), BorderLayout.CENTER);
		panelPrincipal.add(crearPanelMontaje(), BorderLayout.SOUTH);
		
		//esto es por si cierran la ventana con el montaje del evento (mi hilo) en marcha, asi se para
		 addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                     if (hiloMontaje != null) {
                             hiloMontaje.interrupt();
                     }
             }
     });

		
		
		
		add(panelPrincipal);
	}

	//DATOS HECHO CON IA

	
    private void cargarDatos() {
        fechasPorEvento.put("Fiesta ibicenca",
                        new String[] { "03-07-2027", "10-07-2027", "17-07-2027", "24-07-2027", "31-07-2027" });
        fechasPorEvento.put("Fiesta tecno",
                        new String[] { "05-12-2026", "12-12-2026", "19-12-2026", "09-01-2027", "16-01-2027" });
        fechasPorEvento.put("Fiesta de máscaras",
                        new String[] { "13-02-2027", "20-02-2027", "27-02-2027", "06-03-2027", "13-03-2027" });
        fechasPorEvento.put("Fiesta de roles",
                        new String[] { "28-11-2026", "23-01-2027", "30-01-2027", "06-02-2027", "20-03-2027" });
        fechasPorEvento.put("Graduación",
                        new String[] { "29-05-2027", "05-06-2027", "12-06-2027", "19-06-2027", "26-06-2027" });
        fechasPorEvento.put("Privado",
                        new String[] {"21-11-2026", "27-11-2026", "11-12-2026", "18-12-2026", "15-01-2027" });

        // Al principio todas las fechasestán libres (false = no reservada)
        for (String tipo : TIPOS_EVENTO) {
            reservasPorEvento.put(tipo, new ReservaEvento[5]);
        }
}
		
	

//BOTONES DE EVENTOS
	private JPanel crearPanelEventos() {
		JPanel panelEventos = new JPanel(new GridLayout(TIPOS_EVENTO.length, 1, 5, 5));
		panelEventos.setBorder(BorderFactory.createTitledBorder("Tipos de evento"));
		panelEventos.setPreferredSize(new Dimension(200, 0));

		for (String tipo : TIPOS_EVENTO) {
			JButton btnEvento = new JButton(tipo);
			btnEvento.addActionListener(e -> {
				eventoSeleccionado = tipo;
				mostrarFechas();
			});
			panelEventos.add(btnEvento);
		}
		return panelEventos;
	}

	//FECHAS
	private JPanel crearPanelFechas() {
		JPanel panelCentro = new JPanel(new BorderLayout(5, 5));

		lblEventoActual = new JLabel("Ningún evento seleccionado", SwingConstants.CENTER);
		lblEventoActual.setFont(new Font("Arial", Font.BOLD, 18));
		panelCentro.add(lblEventoActual, BorderLayout.NORTH);

		JPanel panelFechas = new JPanel(new GridLayout(5, 1, 5, 5));
		panelFechas.setBorder(BorderFactory.createTitledBorder("Fechas disponibles"));

		for (int i = 0; i < lblFechas.length; i++) {
			JLabel lblFecha = new JLabel("", SwingConstants.CENTER);
			lblFecha.setOpaque(true); 
			lblFecha.setFont(new Font("Arial", Font.BOLD, 20));
			lblFecha.setForeground(Color.WHITE);
			lblFecha.setVisible(false); 

			final int posicion = i; 

			lblFecha.addMouseListener(new MouseAdapter() {

				
				@Override
				public void mouseEntered(MouseEvent e) {
                    if (eventoSeleccionado == null) {
                            return;
                    }
                    if (estaReservada(posicion)) {
                            lblFecha.setText("Anular reserva");
                    } else {
                            lblFecha.setText("Reservar");
                    }
                    lblFecha.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

				
				@Override
				public void mouseExited(MouseEvent e) {
					actualizarFecha(posicion);
					lblFecha.setCursor(Cursor.getDefaultCursor());
				}

				
				@Override
				public void mouseClicked(MouseEvent e) {
                    if (eventoSeleccionado == null) {
                            return;
                    }
                    if (estaReservada(posicion)) {
                            anularReserva(posicion);
                    } else {
                            reservarFecha(posicion);
                    }
            }

			});

			lblFechas[i] = lblFecha;
			panelFechas.add(lblFecha);
		}

		panelCentro.add(panelFechas, BorderLayout.CENTER);
		return panelCentro;
	}

	private JPanel crearPanelMontaje() {
		JPanel panelMontaje = new JPanel(new BorderLayout (5,5));
		panelMontaje.setBorder(BorderFactory.createTitledBorder("Montaje del evento"));
		lblEstadoMontaje = new JLabel ("Reserva una fecha para montar el evento",SwingConstants.CENTER);
        barraMontaje = new JProgressBar(0, 100);
        barraMontaje.setStringPainted(true);
        
        btnMontar = new JButton("Montar evento");
        btnParar = new JButton("Detener montaje");
        btnMontar.setEnabled(false);
        btnParar.setEnabled(false);
        
        btnMontar.addActionListener(e -> {
            barraMontaje.setValue(0);
            btnMontar.setEnabled(false);
            btnParar.setEnabled(true);
            hiloMontaje = new HiloMontaje(reservaParaMontar);
            hiloMontaje.start();
    });

    btnParar.addActionListener(e -> {
            if (hiloMontaje != null) {
                    hiloMontaje.interrupt();
            }
            btnParar.setEnabled(false);
    });

    JPanel panelBotones = new JPanel();
    panelBotones.add(btnMontar);
    panelBotones.add(btnParar);

    panelMontaje.add(lblEstadoMontaje, BorderLayout.NORTH);
    panelMontaje.add(barraMontaje, BorderLayout.CENTER);
    panelMontaje.add(panelBotones, BorderLayout.SOUTH);
    return panelMontaje;
}
	
	
	
	
	
	// pinta las 5 fechas del evento seleccionado
	private void mostrarFechas() {
		lblEventoActual.setText("Evento: " + eventoSeleccionado);
		for (int i = 0; i < lblFechas.length; i++) {
			lblFechas[i].setVisible(true);
			actualizarFecha(i);
		}
	}

	
	private void actualizarFecha(int posicion) {
		if (eventoSeleccionado == null) {
			return;
		}
		String fecha = fechasPorEvento.get(eventoSeleccionado)[posicion];
		if (estaReservada(posicion)) {
            ReservaEvento r = reservasPorEvento.get(eventoSeleccionado)[posicion];
            lblFechas[posicion].setText(fecha + " - RESERVADO (" + r.getCliente() + ")");
			lblFechas[posicion].setBackground(COLOR_RESERVADO);
		} else {
			lblFechas[posicion].setText(fecha);
			lblFechas[posicion].setBackground(COLOR_LIBRE);
		}
	}

	private boolean estaReservada(int posicion) {
		  return reservasPorEvento.get(eventoSeleccionado)[posicion] != null;

	}

    private void reservarFecha(int posicion) {
        String fecha = fechasPorEvento.get(eventoSeleccionado)[posicion];

        JDialogReservaEvento dialogo = new JDialogReservaEvento(this, siguienteId, eventoSeleccionado, fecha);
        dialogo.setVisible(true); // modal: el código se para aquí hasta que se cierre

        ReservaEvento nueva = dialogo.getReserva();
        if (nueva != null) {
                siguienteId++;
                reservasPorEvento.get(eventoSeleccionado)[posicion] = nueva;
                reservaParaMontar = nueva;
                lblEstadoMontaje.setText("Pendiente de montar: " + nueva);
                btnMontar.setEnabled(hiloMontaje == null || !hiloMontaje.isAlive());
        }
        actualizarFecha(posicion);
}
    // anular reserva 
    private void anularReserva(int posicion) {
        ReservaEvento r = reservasPorEvento.get(eventoSeleccionado)[posicion];
        if (hiloMontaje != null && hiloMontaje.isAlive() && hiloMontaje.getReserva() == r) {
            JOptionPane.showMessageDialog(this, "No se puede anular: se está montando ese evento", "Aviso",
                            JOptionPane.WARNING_MESSAGE);
            return;
    }
        int respuesta = JOptionPane.showConfirmDialog(this,
                        "¿Anular la reserva de " + r.getCliente() + " (DNI " + r.getDni() + ") para el "
                                        + r.getEvento().getFecha() + "?",
                        "Anular reserva", JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            reservasPorEvento.get(eventoSeleccionado)[posicion] = null;
            if (r == reservaParaMontar) {
                    reservaParaMontar = null;
                    btnMontar.setEnabled(false);
                    lblEstadoMontaje.setText("Reserva anulada");
            }
    }
            
        actualizarFecha(posicion);
}
	private class HiloMontaje extends Thread{
        //IA pasos del monjaje, productos de la clase de JFrameInventario asi cuadra con julene
	
		  private final String[] PASOS = { "Montando luces y escenario", "Probando el sonido del DJ",
                  "Preparando la barra (Hielo, Agua mineral, Coca-Cola, Heineken)", "Colocando seguridad en la puerta",
                  "Limpiando la pista", "Abriendo puertas" };
		  
		  private ReservaEvento reserva;
		  public HiloMontaje(ReservaEvento reserva) {
			  this.reserva = reserva;
	  }
		  public ReservaEvento getReserva() {
              return reserva;
      }

      @Override
      public void run() {
          for (int i = 0; i < PASOS.length && !isInterrupted(); i++) {
                  String paso = PASOS[i];
                  SwingUtilities.invokeLater(() -> lblEstadoMontaje.setText(reserva + ": " + paso + "..."));

                  try {
                          Thread.sleep(1500);
                  } catch (InterruptedException e) {
                          interrupt(); // el catch borra la marca, la vuelvo a poner
                          break;
                  }

                  int progreso = (i + 1) * 100 / PASOS.length;
                  SwingUtilities.invokeLater(() -> barraMontaje.setValue(progreso));
          }

          boolean terminado = !isInterrupted();
          // el hilo no toca la ventana directamente: se lo pide al EDT
          SwingUtilities.invokeLater(() -> {
                  if (terminado) {
                          lblEstadoMontaje.setText("¡" + reserva.getEvento().getNombre() + " del "
                                          + reserva.getEvento().getFecha() + " listo!");
                  } else {
                          lblEstadoMontaje.setText("Montaje detenido");
                  }
                  btnMontar.setEnabled(reservaParaMontar != null);
                  btnParar.setEnabled(false);
          });
  }
}


	}

