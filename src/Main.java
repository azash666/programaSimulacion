import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

//Hecho por Daniel Delgado Gomis

public class Main {

	public static void main(String[] args) {
		new Programa(args);
	}

}

class Programa{
	public Programa(String[] args) {
		String fileInput;
		String fileOutput;
		if (args.length>=2) {
			fileInput = args[0];
			fileOutput = args[1];
		}else if (args.length==1) {
			fileInput = args[0];
			fileOutput = "salida.txt";
		}else {
			fileInput = "config.txt";
			fileOutput = "salida.txt";
		}
		
		System.out.println("Leyendo archivo "+fileInput+" ... ");
		buildDataFromFile(fileInput);
		System.out.println("Datos adquiridos.");

		System.out.println("Escribiendo archivo "+fileOutput+" ... ");
		buildFileFromData(fileOutput);
		System.out.println("Finalizado.");
	}

	private void buildFileFromData(String fileOutput) {
		BufferedWriter writer = null;
		try {
			File file = new File(fileOutput);
			writer = new BufferedWriter(new FileWriter(file));
	
			introduccion(writer);
			nudo(writer);

			desenlace(writer);
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void desenlace(BufferedWriter writer) throws IOException {
		writer.write("\n");
		writer.write("Configuracion para consumo minimo:\n");
		double total = 0;
		for(int parte_index=0; parte_index<Datos.partes.size(); parte_index++) {
			Datos.Parte parte = Datos.partes.get(parte_index);
			writer.write("##### Parte: "+(parte_index+1)+"\n");
			for(int j=0; j<Datos.numCores;j++) {
				if (j<parte.porcentaje.length) writer.write("P"+Datos.mejor_pestado[parte_index][j]+" ");
				else writer.write("-- ");
			}
			writer.write(" :");
			for(int core = 0; core<Datos.numCores; core++) {
				
				writer.write(redondear(Datos.mejor_tiempos[parte_index][core], 6, 2));
			}
			writer.write("  tmax =");
			writer.write(redondear(Datos.mejor_t_max[parte_index], 5, 2));
			writer.write("  Energia=");
			writer.write(redondear(Datos.mejor_energia[parte_index], 2, 6));
			writer.write(" kWh\n");
			total += Datos.mejor_energia[parte_index];
		}
		writer.write("\n");
		writer.write("Consumo total minimo:"+redondear(total,2,6)+" kWh\n");
	}

	private void nudo(BufferedWriter writer) throws IOException {
		Datos.mejor_pestado = new Integer[Datos.partes.size()][];
		Datos.mejor_tiempos = new double[Datos.partes.size()][];
		Datos.mejor_t_max = new double[Datos.partes.size()];
		Datos.mejor_energia = new double[Datos.partes.size()];
		Datos.mejor_vacio = new boolean[Datos.partes.size()];
		for(int i=0; i<Datos.partes.size();i++) {
			Datos.mejor_vacio[i]=true;
		}
		
		for(int parte_index=0; parte_index<Datos.partes.size(); parte_index++) {
			writer.write("\n");
			writer.write("##### Parte: "+(parte_index+1)+"\n");
			Datos.Parte parte = Datos.partes.get(parte_index);
			Integer[] pestados = new Integer[Datos.numCores];
			for(int i=0; i<pestados.length; i++) pestados[i] = i<parte.porcentaje.length?0:null;
			int cantidad = 1;
			for (int i=0; i<parte.porcentaje.length; i++)
				cantidad = cantidad * Datos.p.length;
			for (int i=0; i<cantidad; i++) {
				for(int j=0; j<Datos.numCores;j++) {
					if (j<parte.porcentaje.length) writer.write("P"+pestados[j]+" ");
					else writer.write("-- ");
				}
				writer.write(" :");
				double t_max = -1.0;
				double[] tiempos = new double[Datos.numCores];
				for(int core = 0; core<Datos.numCores; core++) {
					double tiempo = calculaTiempos(parte, core, pestados[core]);
					tiempos[core] = tiempo;
					if (tiempo>t_max) t_max=tiempo;
					writer.write(redondear(tiempo, 6, 2));
				}
				writer.write("  tmax =");
				writer.write(redondear(t_max, 5, 2));
				double energia = calculaEnergias(parte, tiempos, t_max, pestados)/3600.0/1000.0;
				writer.write("  Energia=");
				writer.write(redondear(energia, 2, 6));
				if(Datos.mejor_vacio[parte_index] || Datos.mejor_energia[parte_index]>energia) {
					Datos.mejor_vacio[parte_index]=false;
					Datos.mejor_energia[parte_index]=energia;
					Datos.mejor_t_max[parte_index]=t_max;
					Datos.mejor_pestado[parte_index]=pestados.clone();
					Datos.mejor_tiempos[parte_index]=tiempos.clone();
				}
				
				
				writer.write(" kWh\n");
					pestados[parte.porcentaje.length-1]++;
				for(int j=parte.porcentaje.length-1; j>=0; j--) {
					if(j>0) pestados[j-1]+=pestados[j]/Datos.p.length;
					pestados[j] = pestados[j]%Datos.p.length;
				}
			}
		}
	}

	private void introduccion(BufferedWriter writer) throws IOException {
		writer.write("cores = "+ Datos.numCores+"\n");
		writer.write("P-states\n");
		for(int i=0; i<Datos.p.length; i++) {
			writer.write("P-estado "+i+": V="+redondear(Datos.p[i].voltaje,1,6)+", f="+redondear(Datos.p[i].frecuencia,1,6)+"\n");
		}
		for(int i=0; i<Datos.partes.size(); i++) {
			Datos.Parte parte = Datos.partes.get(i);
			writer.write("\n");
			writer.write("##### Parte: "+(i+1)+"\n");
			writer.write("T sec     ="+redondear(parte.tSecuencial,5,2)+"\n");
			writer.write("P estado  = "+parte.estadoSecuencialReferencia+"\n");
			for(int j=0; j<parte.porcentaje.length; j++) {
				writer.write("particion "+j+": trabajo = "+redondear(parte.porcentaje[j],-1,0)+"\n");
			}
		}
	}
	
	private double calculaEnergias(Datos.Parte parte, double[] tiempos, double t_max, Integer[] pestados) {
		double energia_total = 0;
		for(int core = 0; core<parte.porcentaje.length; core++) {
			double p_pasivo=Datos.potenciaInactivo;//-0.000001;
			double p_activo=p_pasivo+Datos.C*Datos.p[pestados[core]].voltaje*Datos.p[pestados[core]].voltaje*Datos.p[pestados[core]].frecuencia;
			double energia = tiempos[core]*p_activo + (t_max-tiempos[core])*p_pasivo;
			energia_total += energia;
		}
		energia_total += (Datos.numCores-parte.porcentaje.length)*Datos.potenciaInactivo*t_max;
		energia_total += Datos.potenciaBase*t_max;
		return energia_total;
	}
	
	private double calculaTiempos(Datos.Parte parte, int particion_index, Integer p_index) {
		if (particion_index>=parte.porcentaje.length) return 0.0;
		double devolver = parte.tSecuencial*parte.porcentaje[particion_index]/100.0;
		devolver = devolver *(Datos.p[parte.estadoSecuencialReferencia].frecuencia/Datos.p[p_index].frecuencia);
		return devolver;
	}
	
	private String redondear(Double numero, int digitosEnteros, int digitosDecimales) {
		String espacios = "          ";
		String ceros = "0000000000";
		Double redondeo = 0.50000000003;
		for(int i=0; i<digitosDecimales; i++) {
			redondeo = redondeo*0.1;
		}
		Double num = numero+redondeo;
		String cadena = num.toString();
		cadena = cadena.substring(0, cadena.length()-1);
		String entero = cadena.split("\\.")[0];
		String decimal = cadena.split("\\.").length>1?(cadena.split("\\.")[1] + ceros):ceros;
		if(digitosEnteros>=0) {
			entero = espacios+entero;
			entero=entero.substring(entero.length()-digitosEnteros, entero.length());
		}
		decimal = decimal.substring(0, digitosDecimales);
		if(digitosDecimales>0)
			return entero+"."+decimal;
		else
			return entero;
	}

	private void buildDataFromFile(String fileString){
		try {
			File file = new File(fileString); 

			BufferedReader br = new BufferedReader(new FileReader(file)); 
			br.readLine();
			String st = br.readLine();
			Datos.numCores = Integer.parseInt(st.split("=")[1].trim());
			br.readLine();
			st = br.readLine();
			Datos.potenciaBase = Double.parseDouble(st.split("=")[1].trim());
			br.readLine();
			st = br.readLine();
			Datos.potenciaInactivo = Double.parseDouble(st.split("=")[1].trim().trim());
			br.readLine();
			st = br.readLine();
			Datos.C = Double.parseDouble(st.split("=")[1].trim());
			br.readLine();
			st = br.readLine();
			String[] frecuencias = st.split("=")[1].split(";");
			br.readLine();
			st = br.readLine();
			String[] voltajes = st.split("=")[1].split(";");
			Datos.p = new Datos.P[frecuencias.length];
			for(int i=0; i<frecuencias.length; i++) {
				Datos.p[i]= new Datos.P(Double.parseDouble(frecuencias[i].trim()), Double.parseDouble(voltajes[i].trim()));
			}
			Datos.partes = new LinkedList<Datos.Parte>();
			while ((st = br.readLine()) != null) {
				if((st = br.readLine()) == null) break;
				if((st = br.readLine()) == null) break;
				double t_sec = Double.parseDouble(st.split("=")[1].trim());
				if((st = br.readLine()) == null) break;
				if((st = br.readLine()) == null) break;
				int pestado = Integer.parseInt(st.split("=")[1].trim());
				if((st = br.readLine()) == null) break;
				if((st = br.readLine()) == null) break;
				int grado = Integer.parseInt(st.split("=")[1].trim());
				double[] distr = new double[grado];
				if((st = br.readLine()) == null) break;
				if((st = br.readLine()) == null) break;
				String[] division = st.split("=")[1].split(";");
				for(int i=0; i<grado; i++) {
					distr[i] = Double.parseDouble(division[i].trim());
				}
				Datos.partes.add(new Datos.Parte(t_sec, pestado, grado, distr));
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
