import java.io.FileWriter;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import com.google.gson.stream.JsonWriter;
import java.text.SimpleDateFormat;

public class TerminationHandler extends Thread {
	private int maxDelay;
	private ExecutorService pool;
	private ServerSocket serverSocket;
	private String upath = "users.json";
	private String hpath = "Hotels.json";
	private ConcurrentHashMap<String, User> Umap;
	private ConcurrentHashMap<String, ArrayList<Hotel>> Hmap;

	public TerminationHandler(int maxDelay, ExecutorService pool, ServerSocket serverSocket,
			ConcurrentHashMap<String, User> Umap, ConcurrentHashMap<String, ArrayList<Hotel>> Hmap) {
		this.maxDelay = maxDelay;
		this.pool = pool;
		this.serverSocket = serverSocket;
		this.Umap = Umap;
		this.Hmap = Hmap;
	}

	public void run() {
		// Avvio la procedura di terminazione del server.
		System.out.println("\n Chiusura in corso...");
		// Chiudo la ServerSocket in modo tale da non accettare piu' nuove richieste.
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.getMessage();
		}
		// Faccio terminare il pool di thread.
		pool.shutdown();
		try {
			if (!pool.awaitTermination(maxDelay, TimeUnit.MILLISECONDS))
				pool.shutdownNow();
		} catch (Exception e) {
			System.out.println("Errore nell'attesa della terminazione del pool.");
			e.printStackTrace();
			pool.shutdownNow();
		}
		user_Writer(Umap);
		hotel_Writer(Hmap);
		System.out.println("[SERVER] Terminato.");
	}

	public void user_Writer(ConcurrentHashMap<String, User> map) {
		try {
			// Creazione JsonWriter
			JsonWriter writer = new JsonWriter(new FileWriter(upath));
			writer.beginArray();
			for (String key : map.keySet()) {
				User user = map.get(key);
				writer.beginObject();
				writer.name("username").value(user.get_User());
				writer.name("password").value(user.get_Password());
				writer.name("exp_level").value(user.get_exp());
				writer.name("bagde").value(user.get_Bagde());
				writer.endObject();
			}
			writer.endArray();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Dati utenti salvati");
	}

	public void hotel_Writer(ConcurrentHashMap<String, ArrayList<Hotel>> map) {
		try {
			if (map.isEmpty()) {
				System.out.println("Nessun hotel da salvare");
				return;
			}
			JsonWriter writer = new JsonWriter(new FileWriter(hpath));
			writer.beginArray();
			for (ArrayList<Hotel> list : map.values()) {
				for (Hotel h : list) {
					writer.beginObject();
					writer.name("id").value(h.get_id());
					writer.name("name").value(h.get_nome());
					writer.name("description").value(h.get_descrizione());
					writer.name("city").value(h.get_città());
					writer.name("phone").value(h.get_telefono());
					writer.name("services");
					writer.beginArray();
					ArrayList<String> servizi = h.get_servizi();
					for (String s : servizi) {
						writer.value(s);
					}
					writer.endArray();
					writer.name("rate").value(h.get_voto_complessivo());
					writer.name("ratings");
					writer.beginArray();
					ArrayList<Giudizi> glis = h.get_voti_servizi();
					if(glis != null){
						for (Giudizi g : glis) {
							writer.beginObject();
							writer.name("cleaning").value(g.get_pulizia());
							writer.name("position").value(g.get_posizione());
							writer.name("services").value(g.get_servizi());
							writer.name("quality").value(g.get_qualità());
							writer.name("utente").value(g.get_utente());
							Date d = g.get_data();
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String format_d = format.format(d);
							writer.name("data").value(format_d);
							writer.endObject();
						}
					}
					else{
						writer.beginObject();
						writer.endObject();
					}
					writer.endArray();
					writer.name("ranking:").value(h.get_ranking());
					writer.endObject();
				}
			}
			writer.endArray();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Dati hotel salvati");
	}
}
