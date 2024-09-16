public class User {
  private String username;
	private String password;
	private int exp_level;
	private String bagde;

	public User(String username, String password){
		this.username = username;
		this.password = password;
		this.exp_level = 0;
		this.bagde = "RECENSORE";
	}

	public String get_User(){
		return this.username;
	}

	public int get_exp(){
		return this.exp_level;
	}

	public String get_Bagde(){
		return this.bagde;				
	}

	public String get_Password(){
		return this.password;
	}

	public void set_exp(int e){
		this.exp_level+=e;
		if(this.exp_level >= 100){
			this.bagde = "RECENSORE ESPERTO";
		}
		if(this.exp_level >= 200){
			this.bagde = "RECENSORE AVANZATO";
		}
		if(this.exp_level >= 500){
			this.bagde = "CONTRIBUTORE";
		} 
		if(this.exp_level >= 750){
			this.bagde = "CONTRIBUTORE ESPERTO";
		}
		if(this.exp_level >= 1000){
			this.bagde = "CONTRIBUTORE SUPER";
		}
	}

}
