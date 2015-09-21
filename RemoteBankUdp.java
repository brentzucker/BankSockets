import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class RemoteBankUdp {

	public static void main(String[] args) throws IOException {

		Scanner scan = new Scanner(System.in);

		/* Parse Command Line arguments */
		
		// remotebank 127.0.0.1:8591 “DrEvil” “minime123” deposit 27.50
	    if (args.length < 5) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    
	    // Array that is expected to hold the ipaddress [0] and the port number [1]
	    String[] ipPort = args[0].split(":");

	    // Check if ip & port were entered properly
	    if (ipPort.length != 2) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }

	    InetAddress destAddr = InetAddress.getByName(args[0].split(":")[0]); // Destination Address
	   	int destPort = Integer.parseInt(args[0].split(":")[1]); // Destination port

	    String username = args[1];

	    String password = args[2];

	    // Check if transaction is deposit or withdraw
	    if (!args[3].equals("deposit") && !args[3].equals("withdraw")) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    String transaction = args[3];

	    // Check if TransactionAmount is a Number
	    if (isNotNumber(args[4])) {
	    	throw new IllegalArgumentException("Parameter(s): <ip-address:port> <\"username\">"
	    									+ "<\"password\"> <deposit/withdraw> <amount>");
	    }
	    Double transactionAmount = Double.parseDouble(args[4]);

	    // Check for debugging flag
	    if (args.length == 6 && args[5].equals("-d")) {
	    	// Consume -d for debugger
	    	Debugger.setEnabled(true);
	    }
	    
	    /* Begin Authentication */

	    DatagramSocket sock = new DatagramSocket();
	    sock.connect(destAddr, destPort);

	    // Create Authentication message
	    // BankMsg(boolean isResponse, boolean isAuthentication, boolean isAuthenticated, boolean isDeposit, String username, String password, Double balance, Double transactionAmount)
		boolean isResponse = false,
				isAuthentication = true,
				isAuthenticated = false,
				isDeposit = transaction.equals("deposit");

	    BankMsg msg = new BankMsg(isResponse, isAuthentication, isAuthenticated, isDeposit, username, password, 0.0, 0.0);

	    // Change Text to Bin
	    BankMsgCoder coder = new BankMsgTextCoder();

	    // Send request
	    byte[] encodedAuth = coder.toWire(msg);
	    Debugger.log("Sending Text-Encoded Request (" + encodedAuth.length
	    													+ " bytes): ");
	    Debugger.log(encodedAuth);
	    DatagramPacket message = new DatagramPacket(encodedAuth, encodedAuth.length);
	    sock.send(message);

	    // Receive response
	    message = new DatagramPacket(new byte[BankMsgTextCoder.MAX_WIRE_LENGTH],
	    							 BankMsgTextCoder.MAX_WIRE_LENGTH);
	    sock.receive(message);
	    encodedAuth = Arrays.copyOfRange(message.getData(), 0, message.getLength());

	    Debugger.log("Received Text-Encoded Response (" + encodedAuth.length
	    													  + " bytes): ");
	    msg = coder.fromWire(encodedAuth);
	    Debugger.log(msg);

	    // Extract balance from msg
	    Double balance = msg.getBalance();

	    Debugger.log("BankClientUDP.java");
	    Debugger.log("msg.isAuthenticated(): " + msg.isAuthenticated());

	    // If the user is authenticated, let them do transactions until they want to exit
	    if (msg.isAuthenticated()) {

	    	// Username/Password accepted - Mark Authenticated Flag as True
	    	isAuthenticated = true;
	    	isAuthentication = false; // This message is not seeking authentication

	    	System.out.println("Welcome " + username +".");

			// Create transaction message
			msg = new BankMsg(isResponse, isAuthentication, isAuthenticated, isDeposit, 
							  username, password, balance, transactionAmount);
		
			// Change text to Bin
			encodedAuth = coder.toWire(msg);

			Debugger.log("Sending Text-Encoded Request (" + encodedAuth.length + " bytes): ");
			Debugger.log(encodedAuth);

			// Encapsulate bin within DatagramPacket and Send
			message = new DatagramPacket(encodedAuth, encodedAuth.length);
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
}