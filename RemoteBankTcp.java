import java.io.OutputStream;
import java.net.Socket;

public class RemoteBankTcp {

	public static Socket sock;

	public static void main(String[] args) throws Exception {

		throwIllegalArgumentException(args);

		String destAddr = args[0]; // Destination address
	    int destPort = Integer.parseInt(args[1]); // Destination port

	    sock = new Socket(destAddr, destPort);

	    

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