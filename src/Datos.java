import java.util.List;

//Hecho por Daniel Delgado Gomis

public class Datos {
	
	public static int numCores;
	public static double potenciaBase;
	public static double C;
	public static double potenciaInactivo;
	public static P[] p;
	public static List<Parte> partes;

	public static Integer[][] mejor_pestado;
	public static double[][] mejor_tiempos;
	public static double[] mejor_t_max;
	public static double[] mejor_energia;
	public static boolean[] mejor_vacio;

	static class P{
		double frecuencia;
		double voltaje;
		
		public P(double frecuencia, double voltaje) {
			this.frecuencia = frecuencia;
			this.voltaje = voltaje;
			
		}
	}
	
	static class Parte{
		double tSecuencial;
		int estadoSecuencialReferencia;
		int gradoParalelizacion;
		double[] porcentaje;
		
		public Parte(double t_sec, int estadoSecuencialReferencia, int gradoParalelizacion, double[] distr) {
			this.tSecuencial=t_sec;
			this.estadoSecuencialReferencia = estadoSecuencialReferencia;
			this.gradoParalelizacion = gradoParalelizacion;
			this.porcentaje = distr;
		}
	}
}
