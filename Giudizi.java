import java.util.Date;

public class Giudizi {
    private int pulizia;
    private int posizione;
    private int servizi;
    private int qualità;
    private String utente;
    private Date data;

    Giudizi(int pulizia, int posizione, int servizi, int qualità, String utente, Date data){
        this.pulizia = pulizia;
        this.posizione = posizione;
        this.servizi = servizi;
        this.qualità = qualità;
        this.utente = utente;
        this.data = data;
    }

    public int get_pulizia(){
        return this.pulizia;
    }

    public int get_posizione(){
        return this.posizione;
    }

    public int get_servizi(){
        return this.servizi;
    }

    public int get_qualità(){
        return this.qualità;
    }

    public String get_utente(){
        return this.utente;
    }

    public Date get_data(){
        return this.data;
    }

    public void set_pulizia(int p){
        this.pulizia = p;
    }

}
