import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

public class RemoteBankUdp {

	public static DatagramSocket sock;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {

		Scanner scan = new Scanner(System.in);

		InetAddress destAddr;
		int destPort;

		String challenge, md5, username, password, transaction;
		Double transactionAmount, balance;
		boolean isResponse, isAuthentication, isAuthenticated, isDeposit;

		/* Parse Command Line arguments */

		throwIllegalArgumentException(args);

		// Array that is expected to hold the ipaddress [0] and the port number [1]
	    String[] ipPort = args[0].split(":");

	   	destAddr = InetAddress.getByName(args[0].split(":")[0]); // Destination Address
	   	destPort = Integer.parseInt(args[0].split(":")[1]); // Destination port

	    username = args[1];
	    password = args[2];
	    transaction = args[3];
	    transactionAmount = Double.parseDouble(args[4]);
		
	    // Check for debugging flag
	    if (args.length == 6 && args[5].equals("-d")) {
	    	// Consume -d for debugger
	    	Debugger.setEnabled(true);
	    }
	    
	    /* Begin Authentication */

	    sock = new DatagramSocket();
	    sock.connect(destAddr, destPort);

	    // Create Authentication Request message
		isResponse = false;
		isAuthentication = true;
		isAuthenticated = false;
		isDeposit = transaction.equals("deposit");
	    BankMsg msg = new BankMsg(isResponse, isAuthentication, isAuthenticated, isDeposit, "challengeRequest", "challengeRequest", 0.0, 0.0);

	    // Send authentication Request message and receive challenge
	    msg = sendAndReceive(msg);
	    challenge = msg.getPassword();

	    // Compute MD5 hash, hash = MD5(username, password, challenge)
	    md5 = MD5Hash.computeMD5(username + password + challenge);

	    Debugger.log(msg);
	    Debugger.log("RemoteBankUdp: md5: " + md5);

	    // Send MD5 hash and Receive Balance & Authentication
	    msg = new BankMsg(isResponse, isAuthentication, isAuthenticated, isDeposit, username, md5, 0.0, 0.0);
	    msg = sendAndReceive(msg);

	    /* Complete Transaction */

	    // If the user is authenticated complete the transaction
	    if (msg.isAuthenticated()) {

	    	System.out.println("Welcome " + username +".");

	    	// Extract balance from msg
	    	balance = msg.getBalance();

	    	// Username/Password accepted - Mark Authenticated Flag as True
	    	isAuthenticated = true;
	    	isAuthentication = false; // This message is not seeking authentication

			// Send Transaction Message, Receive new balance
			msg = new BankMsg(isResponse, isAuthentication, isAuthenticated, isDeposit, username, password, balance, transactionAmount);
		    msg = sendAndReceive(msg);
		    
		    Debugger.log(msg);

		    if (msg.getTransactionAmount() > -1) { // If the transaction amount was valid it will not be -1
		    	
		    	System.out.println("Your " + transaction + " of " 
		    					+ msg.getTransactionAmount() + " is successfully recorded.");
		    	System.out.println("Your new account balance is " + msg.getBalance());
		    	System.out.println("Thank you for banking with us");
		    }
		    else {
		    	System.out.println("Invalid transaction.");
		    	System.out.println("Your account balance is " + msg.getBalance());
		    }
	    } else {
	    	System.out.println("User authorization failed for Username: " + username);
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

	public static BankMsg sendAndReceive(BankMsg msg) throws IOException {
	
		// Change text to Bin
	    BankMsgCoder coder = new BankMsgTextCoder();
		byte[] encodedAuth = coder.toWire(msg);

		Debugger.log("Sending Text-Encoded Request (" + encodedAuth.length + " bytes): ");
		Debugger.log(encodedAuth);

		// Encapsulate bin within DatagramPacket and Send
		DatagramPacket message = new DatagramPacket(encodedAuth, encodedAuth.length);
		sock.send(message);

		// New DatagramPacket to store received message
	    message = new DatagramPacket(new byte[BankMsgTextCoder.MAX_WIRE_LENGTH],
	    							 BankMsgTextCoder.MAX_WIRE_LENGTH);
	    // Store received message in datagram packet
	    sock.receive(message);

	    // Get text encoded string from DatagramPacket
	    encodedAuth = Arrays.copyOfRange(message.getData(), 0, message.getLength());

	    Debugger.log("Received Text-Encoded Response (" + encodedAuth.length
	    													  + " bytes): ");
	    // Decode Message
	    msg = coder.fromWire(encodedAuth);

	    return msg;
	}

	public static boolean isNotNumber(String s) {
		try {
			Double.parseDouble(s);
			return false;
		} catch (NumberFormatException n) {
			return true;
		}
	}
}