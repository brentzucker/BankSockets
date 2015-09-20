import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class BankClientUDP {

	public static void main(String[] args) throws IOException {

		Scanner scan = new Scanner(System.in);
		
		if (args.length != 2) { // Test for correct # of args
	    	throw new IllegalArgumentException("Parameter(s): <Destination>" +
	    	                                    " <Port>");
	    }

	    InetAddress destAddr = InetAddress.getByName(args[0]); // Destination Address
	    int destPort = Integer.parseInt(args[1]); // Destination port

	    System.out.println("Username: ");
	    String username = scan.nextLine();

	    System.out.println("Password: ");
	    String password = scan.nextLine();

	    DatagramSocket sock = new DatagramSocket();
	    sock.connect(destAddr, destPort);

	    // Create Authentication message
	    // BankMsg(boolean isResponse, boolean isAuthentication, boolean isAuthenticated, boolean isDeposit, String username, String password, Double balance, Double transactionAmount)
	    BankMsg msg = new BankMsg(false, true, false, false, username, password, 0.0, 0.0);

	    // Change Text to Bin
	    BankMsgCoder coder = new BankMsgTextCoder();

	    // Send request
	    byte[] encodedAuth = coder.toWire(msg);
	    System.out.println("Sending Text-Encoded Request (" + encodedAuth.length
	    													+ " bytes): ");
	    System.out.println(encodedAuth);
	    DatagramPacket message = new DatagramPacket(encodedAuth, encodedAuth.length);
	    sock.send(message);

	    // Receive response
	    message = new DatagramPacket(new byte[BankMsgTextCoder.MAX_WIRE_LENGTH],
	    							 BankMsgTextCoder.MAX_WIRE_LENGTH);
	    sock.receive(message);
	    encodedAuth = Arrays.copyOfRange(message.getData(), 0, message.getLength());

	    System.out.println("Received Text-Encoded Response (" + encodedAuth.length
	    													  + " bytes): ");
	    msg = coder.fromWire(encodedAuth);
	    System.out.println(msg);

	    System.out.println("BankClientUDP.java");
	    System.out.println("msg.isAuthenticated(): " + msg.isAuthenticated());
	    while (msg.isAuthenticated()) {
	    	System.out.println("authtenticated");
	    }
	}
}