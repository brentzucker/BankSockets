import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteBankTcp {

	public static Socket sock;

	public static void main(String[] args) throws Exception {

		BankMsg msgToSend, msgReceieved;
		String[] ipPort;
		InetAddress destAddr;
		int destPort;

		String challenge, md5, username, password, transaction;
		Double transactionAmount, balance;
		boolean isResponse, isAuthentication, isAuthenticated, isDeposit, isDebuggingMode;
		int sequenceNumber;

		/* Parse Command Line arguments */

		RemoteBank.throwIllegalArgumentException(args);

		/* Store Command line arguments */

	    ipPort = args[0].split(":"); // Array that is expected to hold the ipaddress [0] and the port number [1]
	   	destAddr = InetAddress.getByName(args[0].split(":")[0]); // Destination Address
	   	destPort = Integer.parseInt(args[0].split(":")[1]); // Destination port
	    username = args[1];
	    password = args[2];
	    transaction = args[3];
	    transactionAmount = Double.parseDouble(args[4]);
		
	    // Check for debugging flag
	    isDebuggingMode = RemoteBank.isDebuggingMode(args);
	    Debugger.setEnabled(isDebuggingMode);
	    
	    /* Begin Authentication */

	    sock = new Socket(destAddr, destPort);

	    // Create Authentication Request message
		isResponse = false;
		isAuthentication = true;
		isAuthenticated = false;
		isDeposit = transaction.equals("deposit");
	    msgToSend = RemoteBank.getAuthenticationRequestMsg();

	    // Send authentication Request message and receive challenge
	    Debugger.log("Sending Authentication Request to the Server " + args[0]);
	    msgReceieved = sendAndReceive(msgToSend);
	    challenge = msgReceieved.getPassword();

	    // Compute MD5 hash, hash = MD5(username, password, challenge)
	    md5 = MD5Hash.computeMD5(username + password + challenge);

	    // Send username & MD5 hash and Receive Balance & Authentication
	    Debugger.log("Sending Username " + username + " and hash " + md5 + " to the Server ");
	   	msgToSend = RemoteBank.getUsernameAndHashMsg(username, md5);
	   	msgReceieved = sendAndReceive(msgToSend);

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
	    sock.close();
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