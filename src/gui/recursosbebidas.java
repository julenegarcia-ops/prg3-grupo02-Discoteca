  package gui;

	import java.awt.Color;
	import java.awt.Graphics2D;
	import java.awt.Image;
	import java.awt.image.BufferedImage;
	import javax.swing.ImageIcon;
	//Clase mayormente generada con IA.
	public class recursosbebidas {

		public static final String CARPETA_BASE = "./Resources/Images/";

		public static final String LOGO_DISCOTECA = CARPETA_BASE + "LogoDiscoteca+.jpeg";

		public static final String CAT_ALCOHOL = CARPETA_BASE + "cat_alcohol.png";
		public static final String CAT_REFRESCOS = CARPETA_BASE + "cat_refrescos.png";
		public static final String CAT_OTROS = CARPETA_BASE + "cat_otros.png";

		public static final String HEINEKEN = CARPETA_BASE + "heineken.png";
		public static final String ESTRELLA = CARPETA_BASE + "estrella.png";
		public static final String MAHOU = CARPETA_BASE + "mahou.png";
		public static final String VODKA = CARPETA_BASE + "vodka.png";
		public static final String RON = CARPETA_BASE + "ron.png";
		public static final String WHISKY = CARPETA_BASE + "whisky.png";
		public static final String GINEBRA = CARPETA_BASE + "ginebra.png";
		public static final String TEQUILA = CARPETA_BASE + "tequila.png";
		public static final String JAGER = CARPETA_BASE + "jager.png";

		public static final String COCA_COLA = CARPETA_BASE + "cocacola.png";
		public static final String COCA_ZERO = CARPETA_BASE + "cocazero.png";
		public static final String FANTA_NARANJA = CARPETA_BASE + "fantanaranja.png";
		public static final String FANTA_LIMON = CARPETA_BASE + "fantalimon.png";
		public static final String SPRITE = CARPETA_BASE + "sprite.png";
		public static final String RED_BULL = CARPETA_BASE + "redbull.png";
		public static final String MONSTER = CARPETA_BASE + "monster.png";
		public static final String RED_BULL_SF = CARPETA_BASE + "redbullsf.png";

		// Productos: Otros
		public static final String CERVEZA_SIN = CARPETA_BASE + "cervezasin.png";
		public static final String AGUA = CARPETA_BASE + "agua.png";
		public static final String ZUMO_PINA = CARPETA_BASE + "zumo.png";
		public static final String TONICA = CARPETA_BASE + "tonica.png";
		public static final String HIELO = CARPETA_BASE + "hielo.png";

		// Método utilitario reutilizable para cargar y escalar cualquier imagen de forma segura
		public static ImageIcon obtenerIcono(String ruta, int ancho, int alto) {
			ImageIcon iconoOriginal = new ImageIcon(ruta);
			if (iconoOriginal.getIconWidth() <= 0) {
				BufferedImage imgVacia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = imgVacia.createGraphics();
				g2d.setColor(new Color(230, 230, 230));
				g2d.fillRect(0, 0, ancho, alto);
				g2d.setColor(Color.DARK_GRAY);
				g2d.drawRect(0, 0, ancho - 1, alto - 1);
				g2d.drawString("Sin img", 10, alto / 2);
				g2d.dispose();
				return new ImageIcon(imgVacia);
			}
			Image imgEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
			return new ImageIcon(imgEscalada);
		}
	}


