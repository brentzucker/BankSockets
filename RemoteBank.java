/* Common methods used for Tcp and Udp implementations of Remote Bank. */

public class RemoteBank {

	public static BankMsg getRequestToConnectMsg() {
		
		// Create Authentication Request message
		Boolean isResponse = false;
		Boolean isAuthentication = true;
		Boolean isAuthenticated = false;
		Boolean isDeposit = false;
		int sequenceNumber = 1;
	    return new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, "challengeRequest", "challengeRequest", 0.0, 0.0);
	}

	public static BankMsg getUsernameAndHashMsg(String username, String md5) {
		
		Boolean isResponse = false;
		Boolean isAuthentication = true;
		Boolean isAuthenticated = false;
		Boolean isDeposit = false;
		int sequenceNumber = 2;
	    return new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, username, md5, 0.0, 0.0);
	}

	public static BankMsg getTransactionRequestMsg(String username, Double balance, String transactionType, Double transactionAmount) {
		
		Boolean isResponse = false;

		// Username/Password accepted - Mark Authenticated Flag as True
	    Boolean isAuthenticated = true;
	    Boolean isAuthentication = false; // This message is not seeking authentication
	    Boolean isDeposit = transactionType.equals("deposit");
	    int sequenceNumber = 3;
	    return new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, username, "password", balance, transactionAmount);
	}

	public static void printTransactionResults(BankMsg msgReceieved) {

		String transaction = msgReceieved.isDeposit() ? "deposit" : "withdraw";
		if (msgReceieved.getTransactionAmount() > -1) { // If the transaction amount was valid it will not be -1
	    	
	    	System.out.println("Your " + transaction + " of " 
	    					+ msgReceieved.getTransactionAmount() + " is successfully recorded.");
	    	System.out.println("Your new account balance is " + msgReceieved.getBalance());
	    	System.out.println("Thank you for banking with us");
	    }
	    else {
	    	System.out.println("Invalid transaction.");
	    	System.out.println("Your account balance is " + msgReceieved.getBalance());
	    }
	}

	public static void throwIllegalArgumentException(String[] args) {

	    if (args.length != 5 && args.length != 6) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    // Check if ip & port were entered properly
	    if (args[0].split(":").length != 2) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    // Check if transaction is deposit or withdraw
	    if (!args[3].equals("deposit") && !args[3].equals("withdraw")) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    // Check if TransactionAmount is a Number
	    if (isNotNumber(args[4])) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	}

	public static boolean isNotNumber(String s) {
		try {
			Double.parseDouble(s);
			return false;
		} catch (NumberFormatException n) {
			return true;
		}
	}

	public static boolean isDebuggingMode(String[] args) {

		return args.length == 6 && args[5].equals("-d");
	}
}