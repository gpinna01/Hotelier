import java.util.*;

public class Hotel {
	private int id;
	private String nome;
	private String descrizione;
	private String città;
	private String telefono;
	private ArrayList<String> servizi;
	private double voto_complessivo = 0.0;
	private ArrayList<Giudizi> voti_servizi;
	private Date data_ultimo_voto;
	private int n_voti;
	private double x = 0;
	private double ranking;

	public Hotel(int id, String nome, String descrizione, String città, String telefono, ArrayList<String> servizi,
			double voto, ArrayList<Giudizi> voti_servizi, double ranking) {
		this.id = id;
		this.nome = nome;
		this.descrizione = descrizione;
		this.città = città;
		this.telefono = telefono;
		this.servizi = servizi;
		this.voto_complessivo = voto;
		this.voti_servizi = voti_servizi;
		this.data_ultimo_voto = new Date();
		this.n_voti = 0;
		this.ranking = ranking;
	}

	public int get_id() {
		return this.id;
	}

	public String get_nome() {
		return this.nome;
	}

	public String get_descrizione() {
		return this.descrizione;
	}

	public String get_città() {
		return this.città;
	}

	public String get_telefono() {
		return this.telefono;
	}

	public ArrayList<String> get_servizi() {
		return this.servizi;
	}

	public double get_voto_complessivo() {
		return this.voto_complessivo;
	}

	public ArrayList<Giudizi> get_voti_servizi() {
		return this.voti_servizi;
	}

	public Date get_data() {
		return this.data_ultimo_voto;
	}

	public int get_numero_recensioni() {
		return this.n_voti;
	}

	public double get_ranking() {
		return this.ranking;
	}

	public void set_voti(Giudizi new_voti_servizi) {
		this.voti_servizi.add(new_voti_servizi);
		this.n_voti++;
		int sum = new_voti_servizi.get_posizione() + new_voti_servizi.get_pulizia() + new_voti_servizi.get_servizi()
				+ new_voti_servizi.get_qualità();
		double med = (double) sum / 4;
		x += med;
		this.voto_complessivo = (double) (x / n_voti);
		this.data_ultimo_voto = new Date();
	}

	public void set_rank(Date data_voto) {
		double w_voto = 0.5;
		double w_tempo = 0.3;
		double w_nrec = 0.2;

		double norm_voto = this.voto_complessivo / 5.0;
		if (this.n_voti == 1) {
			this.ranking = w_voto * norm_voto;
		} else {
			double norm_data = data_voto.getTime() - this.data_ultimo_voto.getTime();
			double norm_numrec = this.n_voti / 100;
			this.ranking = w_voto * norm_voto + w_tempo * norm_data + w_nrec * norm_numrec;
		}

	}
}
