import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import com.google.gson.stream.JsonReader;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;

public class MainServer {
	public static final String configFile = "server.properties"; // File di configurazione per il server
	private static ServerSocket socket_list; // Socket del server
	private static String address; // Indirizzo del server
	private static int port; // Porta del server
	private static int mcport; // Porta del multicast
	private static String mcadd; // Indirizzo del multicast
	private static int maxDelay; // Tempo massimo di attesa per la terminazione del server
	private static ExecutorService pool = Executors.newCachedThreadPool(); // Pool di thread
	private static ServerSocket serverSocket; // Socket del server
	private static String upath; // Path del file json degli utenti
	private static String hpath; // Path del file json degli hotel

	public static void main(String[] args) {
		// SERVER PRINCIPALE
		try {
			System.out.println("Lettura file json...");
			readConfig();																													// Leggo il file di configurazione

			ConcurrentHashMap<String, User> Umap = new ConcurrentHashMap<>();
			user_Reader(Umap); 																										// Leggo il file json degli utenti
			ConcurrentHashMap<String, ArrayList<Hotel>> Hmap = new ConcurrentHashMap<>();
			hotel_Reader(Hmap); 																									// Leggo il file json degli hotel

			socket_list = new ServerSocket(port);																	// Creo la socket e faccio in modo che sia riutilizzabile
			socket_list.setReuseAddress(true);

			// Quando ricevo il segnale di terminazione (ctrl + C) chiudo il server
			Runtime.getRuntime().addShutdownHook(new TerminationHandler(maxDelay, pool, serverSocket, Umap, Hmap));

			// Server aperto
			System.out.println("\n ***Server aperto*** \n");
			
			// Creazione di un threadpool per le varie connessioni
			while (true) {  			
				// Accetto la connessione
				pool.execute(new ServerTask(socket_list.accept(), Umap, Hmap, port, address, mcadd, mcport));
			}

		} catch (IOException e) {
			System.out.println("Errore nella connessione con il server");
		}
	}

	public static void readConfig() throws FileNotFoundException, IOException {
		// Leggo il file di configurazione
		InputStream input = MainServer.class.getResourceAsStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		address = prop.getProperty("address");
		port = Integer.parseInt(prop.getProperty("port"));
		mcadd = prop.getProperty("multicastAddress");
		mcport = Integer.parseInt(prop.getProperty("multicastPort"));
		upath = prop.getProperty("upath");
		hpath = prop.getProperty("hpath");
		input.close();
	}

	public static void user_Reader(ConcurrentHashMap<String, User> map) throws FileNotFoundException, IOException {
		try {
			JsonReader reader = new JsonReader(new FileReader(upath));
			if (reader.hasNext()) {
				reader.beginArray();
				while (reader.hasNext()) {
					reader.beginObject();
					User u;
					// Leggo l'username
					reader.nextName();
					String username = reader.nextString();
					// leggo la password
					reader.nextName();
					String password = reader.nextString();
					// leggo i punti exp
					reader.nextName();
					int exp = reader.nextInt();
					// leggo il badge ma non lo salvo perchè viene calcolato in set_exp
					reader.nextName();
					reader.nextString();
					// fine lettura oggetto, creazione utente e inserimento nella hash
					reader.endObject();
					u = new User(username, password);
					u.set_exp(exp);
					map.put(username, u);
				}
				reader.endArray();
			}
			reader.close();
			System.out.println("Hash Map utenti ripristinata!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void hotel_Reader(ConcurrentHashMap<String, ArrayList<Hotel>> map)throws FileNotFoundException, IOException {
		try {
			JsonReader reader = new JsonReader(new FileReader(hpath));
			reader.beginArray();
			while (reader.hasNext()) {
				reader.beginObject();

				Hotel h;
				reader.nextName();							// lettura id
				int id = reader.nextInt();
				reader.nextName();							// lettura nome hotel
				String nome = reader.nextString();
				reader.nextName();							// lettura descrizione
				String descr = reader.nextString();
				reader.nextName();							// lettura citta
				String citta = reader.nextString();
				reader.nextName();
				String tel = reader.nextString();// lettura numero di telefono

				// leggo i servizi apro un array e leggo le stringhe all'interno
				reader.nextName();
				reader.beginArray();
				ArrayList<String> servizi = new ArrayList<String>();
				while (reader.hasNext()) {
					servizi.add(reader.nextString());
				}
				reader.endArray();
				
				reader.nextName();
				double voto = reader.nextDouble();// leggo il giudizio complessivo

				// leggo l'oggetto contente i giudizi
				reader.nextName();
				ArrayList<Giudizi> lis = new ArrayList<>();
				String peek = reader.peek().toString();
				//Controllo se si tratta di un array o di un oggetto
				if (peek.equals("BEGIN_ARRAY")) {
					// se è un array leggo tutti gli oggetti al suo interno
					reader.beginArray();
					while (reader.hasNext()) {
						Giudizi giud;
						reader.beginObject();
						reader.nextName();
						int pul = reader.nextInt();
						reader.nextName();
						int pos = reader.nextInt();
						reader.nextName();
						int ser = reader.nextInt();
						reader.nextName();
						int qua = reader.nextInt();
						reader.nextName();
						String use = reader.nextString();
						reader.nextName();
						String s_data = reader.nextString();

						// Siccome non è possibile salvare direttamente il tipo Date, la data è salvata come stringa e poi convertita
						Date data;
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try{
							data = formatter.parse(s_data);
							reader.endObject();
							giud = new Giudizi(pul, pos, ser, qua, use, data);
							lis.add(giud);
						} catch (Exception e){
							e.printStackTrace();
						}
					}
					reader.endArray();

					// Invece se si tratta di un oggetto con i voti settati a 0 questo non viene letto e creo un array vuoto
				} else if (peek.equals("BEGIN_OBJECT")) {
					reader.beginObject();
					reader.nextName();
					reader.nextName();
					reader.nextName();
					reader.nextName();
					reader.endObject();
				}

				// Se è presente il rank lo leggo altrimenti lo setto a 0
				double rank = 0.0;
				if(reader.peek().toString().equals("NAME")){
					reader.nextName();
					rank = reader.nextDouble();
				}

				// Creazione hotel ed inserimento nella hash
				h = new Hotel(id, nome, descr, citta, tel, servizi, voto, lis, rank);
				ArrayList<Hotel> hlis = map.get(citta);
				if (hlis == null) {
					hlis = new ArrayList<Hotel>();
				}
				hlis.add(h);
				map.put(citta, hlis);
				reader.endObject();
			}
			System.out.println("Hash Map Hotel ripristinata!");
			reader.endArray();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}