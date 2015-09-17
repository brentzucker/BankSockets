// Protocol
public class BankMsg 
{
	private boolean isResponse; // true if response from server
	private boolean isDeposit; // true if deposit; false if withdrawl
	private String username; // length restriction
	private String password; // length restriction
	private Double balance;

	public static final int MAX_USERNAME_LENGTH = 12; 
	public static final int MAX_PASSWORD_LENGTH = 12;

	public BankMsg(boolean isResponse, boolean isDeposit, String username, String password, Double balance) throws IllegalArgumentException {
		// Check invariants
		if (username.length() == 0 || username.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException("Bad Username: " + username);
		}
		if (password.length() == 0 || password.length() > MAX_PASSWORD_LENGTH) {
			throw new IllegalArgumentException("Bad Password: " + password);			
		}
		this.isResponse = isResponse;
		this.isDeposit = isDeposit;
		this.username = username;
		this.password = password;
		this.balance = balance;
	}
}