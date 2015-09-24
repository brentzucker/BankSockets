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
		boolean isDebuggingMode;
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

	    // Create Request to Connect Message - Essentially Requesting a challenge
	    msgToSend = RemoteBank.getRequestToConnectMsg();

	    // Send connection Request message and receive challenge
	    Debugger.log("Sending Authentication Request to the Server " + args[0]);
	    msgReceieved = sendAndReceive(msgToSend);
	    challenge = msgReceieved.getPassword();
	    md5 = MD5Hash.computeMD5(username + password + challenge); // Compute MD5 hash, hash = MD5(username, password, challenge)

	    // Send username & MD5 hash and Receive Balance & Authentication
	    Debugger.log("Sending Username " + username + " and hash " + md5 + " to the Server ");
	   	msgToSend = RemoteBank.getUsernameAndHashMsg(username, md5);
	   	msgReceieved = sendAndReceive(msgToSend);

	    /* Complete Transaction */

	    if (msgReceieved.getBalance() > -1) { // If the user has an account balance they are authenticated.

	    	System.out.println("Welcome " + username +".");

	    	// Extract balance from msg
	    	balance = msgReceieved.getBalance();

			// Send Transaction Message, Receive new balance
			Debugger.log("Authentication complete. Sending " + transaction + " request of " + transactionAmount + " to server");
			sequenceNumber = 3;
    	    msgToSend = RemoteBank.getTransactionRequestMsg(username, balance, transaction, transactionAmount);
    	 	msgReceieved = sendAndReceive(msgToSend);

		    Debugger.log(msgReceieved);

			RemoteBank.printTransactionResults(msgReceieved);
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