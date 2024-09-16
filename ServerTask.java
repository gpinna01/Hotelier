import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTask implements Runnable {
	// Dichiarazione delle variabili
	public static final String configFile = "server.properties"; // File di configurazione
	private Socket socket; // Socket
	private DataInputStream in; // Stream di input
	private DataOutputStream out; // Stream di output
	private int port; // Numero di Porta (salvato nel configFile)
	private String mcadd; // Indirizzo Multicast (salvato nel configFile)
	private int mcport; // Porta Multicast (salvato nel configFile)
	private ConcurrentHashMap<String, User> Umap; // hashmap degli utenti
	private ConcurrentHashMap<String, ArrayList<Hotel>> Hmap; // hashmap degli hotel

	// Costruttore
	public ServerTask(Socket socket, ConcurrentHashMap<String, User> Umap, ConcurrentHashMap<String, ArrayList<Hotel>> Hmap, int port, String address, String mcadd, int mcport) {
		this.socket = socket;
		this.Umap = Umap;
		this.Hmap = Hmap;
		this.port = port;
		this.mcadd = mcadd;
		this.mcport = mcport;
	}

	public void run() {
		try {
			// creo i flussi input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			boolean loop = true; // variabile booleana per controllare se devo continuare il ciclo
			do {
				//ricevo il numero che indica l'azione voluta dal client
				int scelta = in.readInt();
				switch (scelta) {
					case 1:
						register();
						break;
					case 2:
						login();
						break;
					case 3:
						break;
					case 4:
						// Ricerca Hotel
						searchHotel();
						break;
					case 5:
						// Ricerca tutti gli Hotel in una Città
						searchAllHotels();
						break;
					case 6:
						// Inserisci Recensione
						insertReview();
						break;
					case 7:
						// Mostra Distintivi
						showBadges();
						break;
					case 8:
						// Uscita il server chiude la socket ed il thread termina
						socket.close();
						loop = false;
						break;
					default:
						break;
				}
			} while (loop);
		} catch (Exception e) {
			System.out.println("Il cliente si è disconesso");
		}
	}

	private void register() {
		try {
			// creo i flussi input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			// ricevo l'username dal client
			String username = in.readUTF();

			// controllo se l'username è già presente
			if (Umap.containsKey(username)) {
				// Username già presente
				out.writeUTF("UEX"); //UEX = User EXist
				return;
			} else {
				// Username non presente
				out.writeUTF("UNE"); // UNE = User Not Exist
				String password = in.readUTF();
				if (password.length() <= 0) {
					out.writeUTF("PNV"); // PNV = Password Not Valid
				} else {
					// Creazione utente e inserimento nella hashmap
					User user = new User(username, password);
					Umap.putIfAbsent(username, user);
					out.writeUTF("NPV"); // NPV = New Password Valid
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.print("ERRORE in register()");
		}
	}

	private void login() {
		try {
			// creo i flussi input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			String username = in.readUTF();
			if (username.equals("ALOG")) { //ALOG = Already LOGged
				return;
			} else {
				if (Umap.containsKey(username)) {
					out.writeUTF("UEX");

					for (int t = 3; t >= 0; t--) { 
						// Il client ha 3 tentativi per inserire la password corretta
						String password = in.readUTF();
						String real_word = Umap.get(username).get_Password();
						if (real_word.equals(password)) {
							out.writeUTF("CPA"); //CPA = Correct PAssword
							return;
						} else {
							out.writeUTF("WPA"); //WPA = Wrong PAssword
						}
					}
				} else {
					out.writeUTF("UNE");
				}
			}
		} catch (Exception e) {
			System.err.print("ERRORE in login()");
			e.printStackTrace();
		}
	}

	private void searchHotel() {
		try {
			// creo i flussi input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			// ricevo la città dal client
			String city = in.readUTF();

			// controllo se la città è presente nella hashmap
			ArrayList<Hotel> list = Hmap.get(city);
			if (list == null) {
				out.writeUTF("CNE"); //CNE = City Not Exist
				return;
			}
			out.writeUTF("CE"); //CE = City Exist
			String name = in.readUTF();

			for (Hotel h : list) {
				// Scorro la lista contenente gli hotel della città
				// Controllo se esiste l'hotel cercato
				if (h.get_nome().equals(name)) {
					String S_hotel;
					// Creo la stringa contenente le informazioni dell'hotel
					S_hotel = ("\n-------------------------------\n");
					S_hotel += ("Hotel trovato: " + h.get_nome() + "\n");
					S_hotel += ("Descrizione: " + h.get_descrizione() + "\n");
					S_hotel += ("Città: " + h.get_città() + "\n");
					S_hotel += ("Telefono: " + h.get_telefono() + "\n");
					S_hotel += ("Servizi: " + h.get_servizi() + "\n");
					S_hotel += ("Voto: " + h.get_voto_complessivo() + "\n");
					S_hotel += ("Giudizi: [" + "\n");
					ArrayList<Giudizi> glis = h.get_voti_servizi();
					for (Giudizi g : glis) {
						S_hotel += ("{	Pulizia: " + g.get_pulizia() + ";");
						S_hotel += ("	Posizione: " + g.get_posizione() + ";");
						S_hotel += ("	Servizi: " + g.get_servizi() + ";");
						S_hotel += ("	Qualità: " + g.get_qualità() + "}\n");
						S_hotel += ("Inserito da: " + g.get_utente() + " in data: [" + g.get_data() + "]\n\n");
					}
					S_hotel += ("]\n");
					S_hotel += ("\n-------------------------------\n");
					out.writeUTF(S_hotel);
					// Invio la stringa e termino il metodo
					return;
				}
			}
			out.writeUTF("HNT"); //HNT = Hotel Not Found

		} catch (Exception e) {
			System.err.println("ERRORE in searchHotel()");
		}
	}

	private void searchAllHotels() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			String city = in.readUTF();

			ArrayList<Hotel> list = Hmap.get(city);
			if (list == null) {
				System.err.println("Nessun Hotel trovato in questa città");
				out.writeUTF("CNE");
				return;
			}
			out.writeUTF("CE");
			String S_hotel;
			// Scorro la lista contenente gli hotel della città
			for (Hotel h : list) {
				// per ogni hotel creo una stringa contenente le informazioni
				S_hotel = "";
				S_hotel = ("\n-------------------------------\n");
				S_hotel += ("Hotel trovato: " + h.get_nome() + "\n");
				S_hotel += ("Descrizione: " + h.get_descrizione() + "\n");
				S_hotel += ("Città: " + h.get_città() + "\n");
				S_hotel += ("Telefono: " + h.get_telefono() + "\n");
				S_hotel += ("Servizi: " + h.get_servizi() + "\n");
				S_hotel += ("Voto: " + h.get_voto_complessivo() + "\n");
				S_hotel += ("Giudizi: [" + "\n");
				ArrayList<Giudizi> glis = h.get_voti_servizi();
				for (Giudizi g : glis) {
					S_hotel += ("{	Pulizia: " + g.get_pulizia() + ";");
					S_hotel += ("	Posizione: " + g.get_posizione() + ";");
					S_hotel += ("	Servizi: " + g.get_servizi() + ";");
					S_hotel += ("	Qualità: " + g.get_qualità() + "}\n");
					S_hotel += ("Inserito da: " + g.get_utente() + " in data: [" + g.get_data() + "]\n\n");
				}
				S_hotel += ("]\n");
				S_hotel += ("\n-------------------------------\n");
				// Invio la stringa contenente le informazioni dell'hotel
				out.writeUTF(S_hotel);
			}
			// Non sapendo quanto è lunga la lista invio un messaggio di STOP
			out.writeUTF("STOP");

		} catch (Exception e) {
			System.err.println("ERRORE in searchAllHotels()");
		}
	}

	private void insertReview() {
		try {
			// creo i flussi input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			// ricevo la città dal client
			String city = in.readUTF();
			if (city.equals("NOLOG")) {
				// Se il client non è loggato non posso proseguire
				return;
			} else {
				ArrayList<Hotel> list = Hmap.get(city);
				if (list == null) {
					// Se la città non esiste
					out.writeUTF("CNE");
				} else {
					out.writeUTF("CE");
					// Se la città esiste richiedo il nome dell'hotel
					String name = in.readUTF();

					for (Hotel h : list) {
						if (h.get_nome().equals(name)) {
							out.writeUTF("HT"); // HT = Hotel Trovato
							// Prendo tutte le recensioni, non prendo il voto complessivo
							Giudizi g;

							int pulizia = in.readInt();
							int posizione = in.readInt();
							int servizi = in.readInt();
							int qualità = in.readInt();
							String user = in.readUTF();
							Date data = new Date();

							// Aggiungo la recensione alla lista di recensioni dell'hotel
							g = new Giudizi(pulizia, posizione, servizi, qualità, user, data);
							h.set_voti(g);

							double voto = (pulizia + posizione + servizi + qualità) / 4;
							// Creo la stringa contenente le informazioni della recensione
							String recensione = "";
							recensione += ("Media recensione: " + voto + "\n");
							recensione += ("Voto Complessivo Hotel Aggiornato: " + h.get_voto_complessivo() + "\n");
							recensione += ("Recensione inserita con successo!\n");

							// Invio la recensione al client
							out.writeUTF(recensione);

							// Aggiorno i punti esperienza dell'utente
							int plus_exp = 50;
							Umap.get(user).set_exp(plus_exp);

							// Aggiorno il rank dell'hotel
							h.set_rank(data);

							// Invio un messaggio che indica l'incremento dei punti esperienza
							out.writeUTF("Sono stati incrementati i tuoi punti di " + plus_exp + " punti\n Punti exp attuali: " + Umap.get(user).get_exp());

							// Controllo se l'hotel è diventato il migliore della città
							list_sort(city);

							return;
						}
					}
					out.writeUTF("HNT");
				}
			}
		} catch (Exception e) {
			System.err.println("ERRORE in insertReview()");
			e.printStackTrace();
		}
	}

	private void showBadges() {
		try {
			// creo il flusso input / output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			String username = in.readUTF();
			if (username.equals("NOLOG")) {
				// Se il client non è loggato non posso proseguire
				return;
			} else {
				// restituisco il badge dell'utente
				String badge = Umap.get(username).get_Bagde();
				out.writeUTF(badge);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void list_sort(String citta) {
		ArrayList<Hotel> new_lis = Hmap.get(citta);
		if(new_lis == null){
			// Se la città non esiste non posso fare il controllo
			return;
		}
		else{
			//Prendo la dimensione della lista della citta
			int size = new_lis.size();
			// L'Hotel migliore è l'ultimo della lista (scelta visiva dal terminale)
			String old_max = new_lis.get(size-1).get_nome();

			// Ordino la lista grazie ad un comparatore che ordina 
			// per ranking e data di inserimento
			Comparator<Hotel> comparator = new Comparator<Hotel>() {
				@Override
				public int compare(Hotel h1, Hotel h2){
					int rank_compare = Double.compare(h1.get_ranking(),h2.get_ranking());
					if(rank_compare != 0){
						return rank_compare;
					}
					// Se i voti sono uguali, ordina per data di inserimento
					return h1.get_data().compareTo(h2.get_data()); 
				}
			};
			// Eseguo l'ordinamento
			Collections.sort(new_lis, comparator);
			// Prendo il nuovo hotel migliore
			String new_max = new_lis.get(size-1).get_nome();
			if(!(new_max.equals(old_max))){
				// Se l'hotel migliore è cambiato invio un messaggio di notifica

				// Creo un datagramsocket con il try with resources
				try(DatagramSocket socket = new DatagramSocket(port)){
					//Stringa contenente il messaggio da inviare
					String news = "Attenzione, l'"+new_max+" è diventato il miglior hotel di "+citta+"!";
					InetAddress group = InetAddress.getByName(mcadd);
					// Creo il pacchetto da inviare
					DatagramPacket response = new DatagramPacket(news.getBytes(), news.getBytes().length, group,mcport);
					// Invio il pacchetto
					socket.send(response);
				} catch (Exception e){
					e.printStackTrace();
				}

			}
			// Aggiorno la lista nella hashmap
			Hmap.replace(citta, new_lis);
		}
	}

}
