import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Properties;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

public class MainClient {
	public static final String configFile = "client.properties"; // File di configurazione per il client
	private static Socket socket; // Socket per la connessione al server
	private static Scanner scan; // Scanner per l'input
	private static BufferedReader reader; // BufferedReader per l'input
	private static DataInputStream in; // DataInputStream per l'input e l'output
	private static DataOutputStream out; // DataOutputStream per l'input e l'output
	private static String address; // Indirizzo del server collegarsi col server
	private static int port; // Porta del server
	private static int mcport; // Porta del multicast
	private static String mcadd; // Indirizzo del multicast
	private static String user; // Username dell'utente che viene mostrato nel menu quando si è loggati
	private static int timer; // timer settato per il timeout nel newsThread
	private static boolean log = false; // Variabile booleana per il controllo dello stato di log
	private static Thread newsThread; // Thread per la ricezione delle news

	public static void main(String[] args) {
		try {
			// Lettura del file di configurazione
			readConfig();

			socket = new Socket(address, port); // Creazione della socket
			scan = new Scanner(System.in); // Creazione scanner
			in = new DataInputStream(socket.getInputStream()); // Creazione stream input e output
			out = new DataOutputStream(socket.getOutputStream());
			// Menu principale
			do {
				// Ci sono due menu in base allo stato di log
				if (log == false) {
					String menu_osp = "\n==========<HOTELIER>==========\n[ Status: Ospite ]\nMENU PRINCIPALE:\n 1: Registrati; \n 2: Effettua Login;\n 3: Effettua Logout\n 4: Ricerca Hotel;\n 5: Ricerca tutti gli Hotel in una Città;\n 6: Inserisci Recensione;\n 7: Mostra Distintivi;\n 8: Esci;\n";
					System.out.println(menu_osp);
				} else {
					String menu_in = "\n=========<HOTELIER>==========\n[ Status: Utente | User:" + user
							+ " ]\nMENU PRINCIPALE:\n 1: Registrati; \n 2: Effettua Login;\n 3: Effettua Logout\n 4: Ricerca Hotel;\n 5: Ricerca tutti gli Hotel in una Città;\n 6: Inserisci Recensione;\n 7: Mostra Distintivi;\n 8: Esci;\n";
					System.out.println(menu_in);
				}

				int scelta = 0; // Intero che rappresenta la scelta del menu
				synchronized (System.out) { // Sincronizzazione per evitare conflitti con il newsThread
					System.out.printf(">SCELTA MENU: ");
					scelta = scan.nextInt();
				}
				// Invio della scelta al server
				out.writeInt(scelta);
				out.flush();

				// Switch per la scelta del menu
				switch (scelta) {
					case 1:
						register();
						break;
					case 2:
						login();
						break;
					case 3:
						logout();
						break;
					case 4:
						searchHotel();
						break;
					case 5:
						searchAllHotels();
						break;
					case 6:
						insertReview();
						break;
					case 7:
						showBadges();
						break;
					case 8:
						// Chiusura del client, interrompo il newsThread e chiudo la socket
						if (log == true) {
							newsThread.interrupt();
							socket.close();
						}
						synchronized (System.out) {
							System.out.println("Arrivederci!");
						}
						System.exit(0);
						break;
					default:
						synchronized (System.out) {
							System.out.println("ATTENZIONE: Scelta non valida!");
						}
						break;
				}
			} while (true);
		} catch (Exception e) {
			System.out.println("<!> Errore nel collegamento al server <!>");
			e.printStackTrace();
		}
	}

	public static void readConfig() throws FileNotFoundException, IOException {
		// Leggo il file di configurazione
		InputStream input = MainClient.class.getResourceAsStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		address = prop.getProperty("address");
		mcadd = prop.getProperty("multicastAddress");
		port = Integer.parseInt(prop.getProperty("port"));
		mcport = Integer.parseInt(prop.getProperty("multicastPort"));
		timer = Integer.parseInt(prop.getProperty("timer"));
		input.close();
	}

	public static void register() {
		try {
			// creazione stream input e output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("\n==========<REGISTRAZIONE>==========\n");

			// Controllo che il client non sia già loggato
			if (log == false) {

				// Registrazione dell'utente, il client inserisce lo username
				System.out.println("Inserisci i seguenti dati per registrare il tuo account.\n");
				reader = new BufferedReader(new InputStreamReader(System.in));
				System.out.printf(">Username: ");
				String username = reader.readLine();
				out.writeUTF(username);

				// Controllo se l'username è già presente
				if (in.readUTF().equals("UNE")) {

					// Inserimento della password
					System.out.printf(">Password: ");
					String password = reader.readLine();
					out.writeUTF(password);

					if (in.readUTF().equals("NPV")) {
						System.out.println("\nRegistrazione effettuata con successo!");
						// Se la registrazione è andata a buon fine,
					} else {
						System.out.println("\n<!>Password non valida<!>");
					}
				} else {
					System.out.println("\n<!>Username già presente<!>");
				}
			} else {
				System.out.println("<!>Sei già loggato come " + user + "<!>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void login() {
		try {
			// creazione stream input e output
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			System.out.print("\n==========LOGIN==========\n");

			// Controllo che il client non sia già loggato
			if (log == false) {
				// Inserimento dell'username
				reader = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Inserisci i seguenti dati per poter accedere!\n");
				System.out.printf(">Username: ");
				String username = reader.readLine();
				out.writeUTF(username);

				String s = in.readUTF();
				if (s.equals("UEX")) {
					// Inserimento della password
					for (int t = 3; t >= 0; t--) {
						System.out.printf(">Password: ");
						String password = reader.readLine();
						out.writeUTF(password);

						s = in.readUTF();
						if (s.equals("CPA")) { //CPA = Correct PAssword
							// Se la password è corretta, il client viene loggato
							user = username;
							System.out.println("\nBentornato " + user);
							log = true;
							// Creazione del newsThread
							NewsTask newsH = new NewsTask(mcadd, mcport, timer);
							newsThread = new Thread(newsH);

							// Avvio del newsThread
							newsThread.start();

							// In caso di interruzione del client, interrompo il newsThread
							Runtime.getRuntime().addShutdownHook(new Thread() {
								public void run() {
									newsThread.interrupt();
								}
							});
							return;
						} else {
							System.out.println("<!>Password errata, tentativi rimanenti: " + (t) + "<!>");
						}
					}
				} else {
					System.out.println("\n<!>Username non presente<!>");
				}

			} else {
				System.out.println("<!>Sei già loggato<!>");
				out.writeUTF("ALOG");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logout() {
		// Controllo che il client sia loggato
		// In caso affermativo, il client viene sloggato
		synchronized (System.out) {
			System.out.print("\n==========LOGOUT==========\n");
			if (log == true) {
				System.out.println("\nLogout effettuato con successo!");
				// Non essendo più loggato interrompo il newsThread per non avere più
				// aggiornamenti
				newsThread.interrupt();
				log = false;
			} else {
				System.out.println("\n<!>Non sei loggato con nessun account<!>");
			}
		}
	}

	public static void searchHotel() {
		synchronized (System.out) {
			System.out.print("\n==========RICERCA HOTEL===========\n");
			try {
				// creazione stream input e output
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(System.in));

				// Inserisco la città
				System.out.printf("Inserisci Citta e nome dell'hotel che desideri vedere.\n");
				System.out.printf("\n>Città: ");
				String city = reader.readLine();
				out.writeUTF(city);

				// Controllo se la città è presente
				if (in.readUTF().equals("CNE")) {
					System.out.println("<!>La citta non è presente nell'elenco<!>");
					return;
				}

				// Inserisco il nome dell'hotel
				System.out.printf(">Hotel: ");
				String nome = reader.readLine();
				out.writeUTF(nome);

				// Controllo se l'hotel è presente
				String s = in.readUTF();
				if (!(s.equals("HNT"))) {
					// Stampa delle informazioni dell'hotel
					System.out.println(s);
				} else {
					System.out.println("<!>Hotel Non Trovato<!>");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void searchAllHotels() {
		synchronized (System.out) {
			System.out.print("\n==========RICERCA TUTTI GLI HOTEL==========\n");
			try {
				// creazione stream input e output
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(System.in));

				// Inserisco la città
				System.out.printf("\n>Città: ");
				String city = reader.readLine();
				out.writeUTF(city);

				// Controllo se la città è presente
				if (in.readUTF().equals("CNE")) {
					System.out.println("<!>La citta non è presente nell'elenco<!>");
					return;
				} else {
					// Stampa di tutti gli hotel presenti nella città
					// sino a quando arriva il messaggio STOP
					boolean loop = true;
					String s = "-";
					do {
						s = in.readUTF();
						if (s.equals("STOP")) {
							loop = false;
						} else {
							System.out.println(s);
						}
					} while (loop);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void insertReview() {
		synchronized (System.out) {
			System.out.println("\n==========INSERIMENTO RECENSIONE==========\n");
			try {
				// creazione stream input e output
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(System.in));

				// Controllo se il client è loggato
				if (log == true) {

					// Inserimento della città
					System.out.printf(">Città: ");
					String città = reader.readLine();
					out.writeUTF(città);

					// Controllo se la città è presente
					String s = in.readUTF();
					if (s.equals("CNE")) {
						System.out.println("<!>Citta non presente nell'elenco<!>");
						return;
					} else {

						// Inserimento del nome dell'hotel
						System.out.printf(">Nome: ");
						String nome = reader.readLine();
						out.writeUTF(nome);

						// Controllo se l'hotel è presente
						s = in.readUTF();
						if (s.equals("HT")) {
							boolean loop = true;
							// Creo un loop per l'inserimento dei voti nel caso
							// si inseriscano valori non validi

							// Voto pulizia
							while (loop) {
								System.out.printf(">Pulizia: ");
								String str = reader.readLine();
								try {
									int pul = Integer.parseInt(str);
									if (pul >= 0 && pul <= 5) {
										out.writeInt(pul);
										loop = false;
									} else {
										System.out.println("<!>Voto non valido<!>");
									}
								} catch (NumberFormatException e) {
									System.out.println("<!>Non hai inserito un numero<!>");
								}
							}

							// Voto posizione
							loop = true;
							while (loop) {
								System.out.printf(">Posizione: ");
								try {
									int pos = Integer.parseInt(reader.readLine());
									if (pos >= 0 && pos <= 5) {
										out.writeInt(pos);
										loop = false;
									} else {
										System.out.println("<!>Voto non valido<!>");
									}
								} catch (NumberFormatException e) {
									System.out.println("<!>Non hai inserito un numero<!>");
								}
							}

							// Voto servizi
							loop = true;
							while (loop) {
								System.out.printf(">Servizi: ");
								try {
									int serv = Integer.parseInt(reader.readLine());
									if (serv >= 0 && serv <= 5) {
										out.writeInt(serv);
										loop = false;
									} else {
										System.out.println("<!>Voto non valido<!>");
									}
								} catch (NumberFormatException e) {
									System.out.println("<!>Non hai inserito un numero<!>");
								}
							}

							// Voto qualità
							loop = true;
							while (loop) {
								System.out.printf(">Qualità: ");
								try {
									int qual = Integer.parseInt(reader.readLine());
									if (qual >= 0 && qual <= 5) {
										out.writeInt(qual);
										loop = false;
									} else {
										System.out.println("<!>Voto non valido<!>");
									}
								} catch (NumberFormatException e) {
									System.out.println("<!>Non hai inserito un numero<!>");
								}
							}

							// Inserimento della recensione e username dell'utente che la inserisce
							out.writeUTF(user);
							s = in.readUTF();

							// Ricevo il voti complessivo aggiornato e i punti exp
							System.out.println(s);
							System.out.println(in.readUTF());
						} else {
							System.out.println("<!>Hotel non trovato<!>");
						}

					}
				} else {
					System.err.println("<!>Non puoi inserire recensioni da ospite<!>");
					out.writeUTF("NOLOG");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void showBadges() {
		synchronized (System.out) {
			try {
				// creazione stream input e output
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("\n==========BADGES==========\n");

				// Controllo se il client è loggato
				if (log == true) {
					// Invio lo user al server e ricevo il badge associato
					out.writeUTF(user);
					String s = in.readUTF();
					System.out.println("Il tuo Badge è: " + s);
				} else {
					System.err.println("<!>Accedi per vedere i tuoi Badges<!>");
					out.writeUTF("NOLOG");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}