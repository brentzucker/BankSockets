import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class BankService {

	// Map of usernames to BankAccounts
	private Map<String, BankAccount> bankAccounts = new HashMap<String, BankAccount>();

	// Map of SocketAddresses, challenges, and Message Counts
	private Map<String, SourceAddress> sourceAddresses = new HashMap<String, SourceAddress>();

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

	public BankMsg handleRequest(BankMsg msg, String socketAddress, int port) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		// Increment sourceAddress.countMessageReceived()

		// If response, just send it back.
		if (msg.isResponse()) {
			return msg;
		}
		// Make message a response
		msg.setResponse(true);

		/* Handle Authentication */

		// Generate Challenge if Requesting Challenge otherwise see if Hashses Match
		if (msg.isAuthentication()) {

			// if msg.sequenceNumber() == sourceAddress.messagesReceived() % MESSAGES_REQUIRED_FOR_TRANSACTION
			if (msg.getPassword().equals("challengeRequest")) {
				msg = handleAuthenticationRequest(msg, socketAddress, port);
			} else {
				msg = handleAuthentication(msg, socketAddress, port);
			}
		} 

		/* Complete Transaction */ 

		else if (msg.isAuthenticated()) { // The Authentication is complete. Check if the User is Authenticated, then complete transaction
			msg = handleTransaction(msg);
		}

		return msg;
	}

	public BankMsg handleAuthenticationRequest(BankMsg msg, String socketAddress, int port) {

		String source = socketAddress + ":" + port,
			   challenge = "";

		if (msg.getPassword().equals("challengeRequest")) {
			// Generate Challenge
			for (int i = 0; i < 64; i++) {
				// Random character based off time
				challenge += (char)((System.currentTimeMillis() % 89) + 33);
			}
			sourceAddresses.put(source, new SourceAddress(socketAddress, port, challenge));
			msg.setPassword(challenge);
		}
		Debugger.log("Sending challenge " + challenge + " to client " + socketAddress + ":" + port);
		return msg;
	}

	public BankMsg handleAuthentication(BankMsg msg, String socketAddress, int port) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		BankAccount bankAccount;
		String username = msg.getUsername(),
			   source = socketAddress + ":" + port;
		Double balance = -1.0;
		
		if (!(msg.getPassword().equals("challengeRequest"))) {

			if ((bankAccount = bankAccounts.get(username)) != null) {

				// Compute Servers MD5 Hash
				String strToCompute = bankAccount.getUsername() + bankAccount.getPassword() + sourceAddresses.get(source).getChallenge();
				String md5 = MD5Hash.computeMD5(strToCompute);

				// Compare Clients Hash to Servers Hash
				if (msg.getPassword().equals(md5)) {
					
					// User is authenticated, return balance
					balance = bankAccount.getBalance();
					msg.setAuthenticated(true);
					msg.setBalance(balance);
					Debugger.log("Sending success message to client");
				} else {
					Debugger.log("Hash does not match: " +  msg.getPassword() + "  !=  " + md5);
					msg.setBalance(balance);
				}
			} else {
				Debugger.log("Username not present");
			}
		}
		return msg;
	}

	public BankMsg handleTransaction(BankMsg msg) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		BankAccount bankAccount = bankAccounts.get(msg.getUsername());
		Double balance = -1.0,
			   transactionAmount = -1.0;

		if (msg.isDeposit()) {

			// If msg has valid transaction amount, update transaction & balance
			if (msg.getTransactionAmount() >= 0) {
				
				transactionAmount = msg.getTransactionAmount();
				balance = bankAccount.getBalance() + msg.getTransactionAmount();

				// Update Balance in Map and Msg
				bankAccount.setBalance(balance);
				msg.setBalance(balance);
				Debugger.log("Deposit of " + msg.getTransactionAmount() + " succesful.");
			} else {

				Debugger.log("Deposit of " + msg.getTransactionAmount() + " failed.");
				// If invalid transaction amount, tAmt changed to -1 & balance stays the same
				msg.setTransactionAmount(transactionAmount);
			}
		} else { // Withdraw

			// If msg has valid transaction amount, update the transaction & balance
			if (msg.getTransactionAmount() >= 0 && msg.getTransactionAmount() <= msg.getBalance()) {

				transactionAmount = msg.getTransactionAmount();
				balance = bankAccount.getBalance() - msg.getTransactionAmount();

				// Update Balance in Map and Msg
				bankAccount.setBalance(balance);
				msg.setBalance(balance);
				Debugger.log("Withdrawl of " + msg.getTransactionAmount() + " succesful.");
			} else {

				Debugger.log("Withdrawl of " + msg.getTransactionAmount() + " failed.");
				// If invalid transaction amount, tAmt changed to -1 & balance stays the same
				msg.setTransactionAmount(transactionAmount);
			}
		}
		return msg;
	}
}