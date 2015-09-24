import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

public class RemoteBankUdp {

	public static DatagramSocket sock;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {

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

	    sock = new DatagramSocket();
	    sock.connect(destAddr, destPort);

	    // Create Request to Connect Message - Essentially Requesting a challenge
		sequenceNumber = 1;
	    msgToSend = RemoteBank.getRequestToConnectMsg();

	    // Send connection Request message and receive challenge
	    Debugger.log("Sending Authentication Request to the Server " + args[0]);
	    while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
	    	Debugger.log("Retransmitting Request after timeout");
	    }
	    challenge = msgReceieved.getPassword();
	    md5 = MD5Hash.computeMD5(username + password + challenge); // Compute MD5 hash, hash = MD5(username, password, challenge)

	    // Send username & MD5 hash and Receive Balance & Authentication
	    Debugger.log("Sending Username " + username + " and hash " + md5 + " to the Server ");
	    sequenceNumber = 2;
	   	msgToSend = RemoteBank.getUsernameAndHashMsg(username, md5);
	   	while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
	    	Debugger.log("Retransmitting Request after timeout: 1000ms");
	    }

	    /* Complete Transaction */
	    
	    if (msgReceieved.isAuthenticated()) { // If the user is authenticated complete the transaction

	    	System.out.println("Welcome " + username +".");

	    	// Extract balance from msg
	    	balance = msgReceieved.getBalance();

			// Send Transaction Message, Receive new balance
			Debugger.log("Authentication complete. Sending " + transaction + " request of " + transactionAmount + " to server");
			sequenceNumber = 3;
    	    msgToSend = RemoteBank.getTransactionRequestMsg(username, balance, transaction, transactionAmount);
    	    while ((msgReceieved = sendAndReceive(msgToSend)).isTimedout() || msgReceieved.getSequenceNumber() != sequenceNumber) { // If timeout or the Sequence Number is not what is expected then resend
		    	Debugger.log("Retransmitting Request after timeout");
		    }
		    
		    Debugger.log(msgReceieved);

		    RemoteBank.printTransactionResults(msgReceieved);
	    } else {
	    	System.out.println("User authorization failed for Username: " + username);
	    }
	}

	public static BankMsg sendAndReceive(BankMsg msg) throws IOException {
		
		boolean received = false;
		// Change text to Bin
	    BankMsgCoder coder = new BankMsgTextCoder();
		byte[] encodedAuth = coder.toWire(msg);

		Debugger.log("Sending Text-Encoded Request (" + encodedAuth.length + " bytes): ");

		// Encapsulate bin within DatagramPacket and Send
		DatagramPacket message = new DatagramPacket(encodedAuth, encodedAuth.length);
		sock.send(message);

		// New DatagramPacket to store received message
	    message = new DatagramPacket(new byte[BankMsgTextCoder.MAX_WIRE_LENGTH],
	    							 BankMsgTextCoder.MAX_WIRE_LENGTH);

	    // Set Timeout for Socket to Receive to 1 second
	    // http://stackoverflow.com/a/10056866
	    sock.setSoTimeout(1000);
	    try {
		    // Store received message in datagram packet
		    sock.receive(message);
		    received = true;
	    } catch (SocketTimeoutException e) {
	    	Debugger.log("Timeout");
	    	msg.setTimedout(true);
	    	return msg;
	    }	    	

	    // Get text encoded string from DatagramPacket
	    encodedAuth = Arrays.copyOfRange(message.getData(), 0, message.getLength());

	    Debugger.log("Received Text-Encoded Response (" + encodedAuth.length
	    													  + " bytes): ");
	    // Decode Message
	    msg = coder.fromWire(encodedAuth);

	    return msg;
	}
}