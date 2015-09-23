/* TODO
 * 
 * Could possibly use (-) to withdraw and (+) to deposit
 */

// Protocol
public class BankMsg 
{
	private boolean isResponse; // true if response from server
	private boolean isAuthentication; // true if loggin in, false if transaction
	private boolean isAuthenticated; // true if username/login exists
	private boolean isDeposit; // true if deposit; false if withdrawl
	private String username; // length restriction
	private String password; // Stores Challenge and Hashed Response
	private Double balance;
	private Double transactionAmount;
	private int sequenceNumber;

	public BankMsg(boolean isResponse, int sequenceNumber, boolean isAuthentication, boolean isAuthenticated, boolean isDeposit, String username, String password, Double balance, Double transactionAmount) throws IllegalArgumentException {
		
		// Check invariants
		if (balance > -1.0) { // Check if account exists
			if (isAuthentication && transactionAmount > 0) {
				throw new IllegalArgumentException("Transaction amount must be 0 to log in: " + transactionAmount);
			}
			if (isAuthentication && (username.length() == 0 || password.length() == 0)) {
				throw new IllegalArgumentException("Invalid Login Attempt: " + username);
			}
			if (transactionAmount < 0) {
				throw new IllegalArgumentException("Invalid Transaction Amount: " + transactionAmount);			
			}
			if (!isResponse && !isDeposit && transactionAmount > balance) {
				throw new IllegalArgumentException("Insufficient funds. Invalid Transaction Amount: " + transactionAmount);			
			}
		}

		this.isResponse = isResponse;
		this.sequenceNumber = sequenceNumber;
		this.isAuthentication = isAuthentication;
		this.isAuthenticated = isAuthenticated;
		this.isDeposit = isDeposit;
		this.username = username;
		this.password = password;
		this.balance = balance;
		this.transactionAmount = transactionAmount;
	}

	public void setDeposit(boolean isDeposit) {
	    this.isDeposit = isDeposit;
	}

	public void setIsAuthentication(boolean isAuthentication) {
	    this.isAuthentication = isAuthentication;
	}

	public void setAuthenticated(boolean isAuthenticated) {
	    this.isAuthenticated = isAuthenticated;
	}

	public void setResponse(boolean isResponse) {
	    this.isResponse = isResponse;
	}	

	public void setSequenceNumber(int sequenceNumber) {
	    this.sequenceNumber = sequenceNumber;
	}

	public void setTransactionAmount(Double transactionAmount) {
		if (transactionAmount < 0) {
			throw new IllegalArgumentException("Invalid Transaction Amount: " + transactionAmount);			
		}
		if (!isDeposit && transactionAmount > balance) {
			throw new IllegalArgumentException("Insufficient funds. Invalid Transaction Amount: " + transactionAmount);			
		}
	    this.transactionAmount = transactionAmount;
	}

	public boolean isDeposit() {
	    return this.isDeposit;
	}

	public boolean isAuthentication() {
	    return this.isAuthentication;
	} 

	public boolean isAuthenticated() {
	    return this.isAuthenticated;
	}

	public boolean isResponse() {
	    return this.isResponse;
	}

	public void setUsername(String username) {
	    this.username = username;
	}

	public String getUsername() {
	    return this.username;
	}

	public void setPassword(String password) {
	    this.password = password;
	}

	public String getPassword() {
	    return this.password;
	}

	public void setBalance(Double balance) {
	    this.balance = balance;
	}

	public Double getBalance() {
	    return this.balance;
	}

	public int getSequenceNumber() {
	    return this.sequenceNumber;
	}

	public Double getTransactionAmount() {
	    return this.transactionAmount;
	}

	public String toString() {
		String res = (isAuthentication ? "authenticate" : (isDeposit ? "deposit " : "withdraw ") + transactionAmount) + " for account " + username;
		if (isResponse) {
			res = "response to " + res 
								 + (isAuthentication && balance == -1.0 ? 
									" is invalid" : " who now has " + balance + " dollar(s)");
		}
		return res;
	}
}