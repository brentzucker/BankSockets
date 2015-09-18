public class BankAccount {

	private String username;
	private String password;
	private Double balance;

	// Transaction counter?
	
	public BankAccount(String username, String password, Double balance) {
		this.username = username;
		this.password = password;
		this.balance = balance;
	}

	public String getUsername() {
	    return this.username;
	}

	public String getPassword() {
	    return this.password;
	}

	public Double getBalance() {
	    return this.balance;
	}

	public void setUsername(String username) {
	    this.username = username;
	}

	public void setPassword(String password) {
	    this.password = password;
	}

	public void setBalance(Double balance) {
	    this.balance = balance;
	}
}