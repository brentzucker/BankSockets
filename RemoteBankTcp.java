import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteBankTcp {

	public static Socket sock;

	public static void main(String[] args) throws Exception {

		BankMsg msgToSend, msgReceieved;
		InetAddress destAddr;
		int destPort;

		String challenge, md5, username, password, transaction;
		Double transactionAmount, balance;
		boolean isResponse, isAuthentication, isAuthenticated, isDeposit;
		int sequenceNumber;

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

	    sock = new Socket(destAddr, destPort);

	    // Create Authentication Request message
		isResponse = false;
		isAuthentication = true;
		isAuthenticated = false;
		isDeposit = transaction.equals("deposit");
		sequenceNumber = 1;
	    msgToSend = new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, "challengeRequest", "challengeRequest", 0.0, 0.0);

	    // Send authentication Request message and receive challenge
	    Debugger.log("Sending Authentication Request to the Server " + args[0]);
	    msgReceieved = sendAndReceive(msgToSend);
	    // while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
	    // 	Debugger.log("Retransmitting Request after timeout");
	    // }
	    challenge = msgReceieved.getPassword();

	    // Compute MD5 hash, hash = MD5(username, password, challenge)
	    md5 = MD5Hash.computeMD5(username + password + challenge);

	    // Send username & MD5 hash and Receive Balance & Authentication
	    Debugger.log("Sending Username " + username + " and hash " + md5 + " to the Server ");
	    sequenceNumber = 2;
	    msgToSend = new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, username, md5, 0.0, 0.0);
	   	msgReceieved = sendAndReceive(msgToSend);
	   	// while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
	    // 	Debugger.log("Retransmitting Request after timeout: 1000ms");
	    // }

	    /* Complete Transaction */

	    // If the user is authenticated complete the transaction
	    if (msgReceieved.isAuthenticated()) {

	    	System.out.println("Welcome " + username +".");

	    	// Extract balance from msg
	    	balance = msgReceieved.getBalance();

	    	// Username/Password accepted - Mark Authenticated Flag as True
	    	isAuthenticated = true;
	    	isAuthentication = false; // This message is not seeking authentication

			// Send Transaction Message, Receive new balance
			Debugger.log("Authentication complete. Sending " + transaction + " request of " + transactionAmount + " to server");
			sequenceNumber = 3;
			msgToSend = new BankMsg(isResponse, sequenceNumber, isAuthentication, isAuthenticated, isDeposit, username, password, balance, transactionAmount);
    	 	msgReceieved = sendAndReceive(msgToSend);
    	 //    while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
		    // 	Debugger.log("Retransmitting Request after timeout");
		    // }
		    
		    Debugger.log(msgReceieved);

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

	public static boolean isNotNumber(String s) {
		try {
			Double.parseDouble(s);
			return false;
		} catch (NumberFormatException n) {
			return true;
		}
	}

	public static BankMsg sendAndReceive(BankMsg msg) throws Exception {

	    OutputStream out = sock.getOutputStream();

		// Change Bin to Text for a different framing strategy
	    BankMsgCoder coder = new BankMsgTextCoder();

	    // Change Length to Delim for a different encoding strategy
	    Framer framer = new LengthFramer(sock.getInputStream());

		// Encode Message
		byte[] encodedMsg = coder.toWire(msg);

		// Send Message
		framer.frameMsg(encodedMsg, out);

		// Receive Response
    	encodedMsg = framer.nextMsg();
    	msg = coder.fromWire(encodedMsg);

    	return msg;
	}
}