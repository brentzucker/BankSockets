import java.util.HashMap;
import java.util.Map;

public class BankService {

	// Map of usernames to BankAccounts
	private Map<String, BankAccount> bankAccounts = new HashMap<String, BankAccount>();

	public BankMsg handleRequest(BankMsg msg) {

		BankAccount bankAccount;

		String username = msg.getUsername(),
			   password = "";

		Double balance = -1.0,
			   transactionAmount = -1.0;

		// If response, just send it back. Send back updated balance?
		if (msg.isResponse()) {
			return msg;
		}
		// Make message a response
		msg.setResponse(true);

		// Check if User exists
		if ((bankAccount = bankAccounts.get(username)) != null) {

			// If Authentication Check password and send back balance
			if (msg.isAuthentication() && msg.getPassword() == bankAccount.getPassword()) {
				
				// User is authenticated, return balance
				balance = bankAccount.getBalance();
				msg.setBalance(balance);
			} else {
				
				// Not authentication, depositing or withdrawing
				if (msg.isDeposit()) {

					// If msg has valid transaction amount, update transaction & balance
					if (msg.getTransactionAmount() >= 0) {
						
						transactionAmount = msg.getTransactionAmount();
						balance = bankAccount.getBalance() + msg.getTransactionAmount();

						// Update Balance in Map and Msg
						bankAccount.setBalance(balance);
						msg.setBalance(balance);
					} else {

						// If invalid transaction amount, tAmt changed to -1 & balance stays the same
						msg.setTransactionAmount(transactionAmount);
					}
				} else { // Withdraw

					// If msg has valid transaction amount, update the transaction & balance
					if (msg.getTransactionAmout() >= 0) {

						transactionAmount = msg.getTransactionAmount();
						balance = bankAccount.getBalance() - msg.getTransactionAmount();

						// Update Balance in Map and Msg
						bankAccount.setBalance(balance);
						msg.setBalance(balance);
					} else {

						// If invalid transaction amount, tAmt changed to -1 & balance stays the same
						msg.setTransactionAmount(transactionAmount);
					}
				}
			}

		} else {
			
			// Username does not exist, return balance of -1
			msg.setBalance(balance);
		}
		return msg;
	}
}