import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class BankService {

	// Map of usernames to BankAccounts
	private Map<String, BankAccount> bankAccounts = new HashMap<String, BankAccount>();

	// Load Bank Accounts
	public void loadBankAccounts() throws FileNotFoundException {
		String username, password,
			   fileName = "bankAccounts.txt";
		Double balance = 0.0;
		Scanner scan = new Scanner(new File(fileName));

		System.out.printf("%12s %12s\n", "", "Bank Accounts");
		System.out.printf("%-12s %-12s %-25s\n", "username", "password", "balance");
		while (scan.hasNext()) {
			username = scan.next();
			password = scan.next();
			balance = scan.nextDouble();
			bankAccounts.put(username, new BankAccount(username, password, balance));
			System.out.printf("%-12s %-12s %-25s\n", username, password, balance);		
		}
		scan.close();
	}

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
					if (msg.getTransactionAmount() >= 0) {

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