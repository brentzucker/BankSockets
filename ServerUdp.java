import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;

public class ServerUdp {

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		if (args.length != 1 && args.length !=2) {
			throw new IllegalArgumentException("Parameter(s): <Port>");
		}

		if (args.length == 2 && args[1].equals("-d"))// Consume -d for debugger
	    	Debugger.setEnabled(true);

		int port = Integer.parseInt(args[0]);

		DatagramSocket sock = new DatagramSocket(port);

		byte[] inBuffer = new byte[BankMsgTextCoder.MAX_WIRE_LENGTH];
		// Change Bin to Text for a different coding approach
		BankMsgCoder coder = new BankMsgTextCoder();
		BankService service = new BankService();

		// Load Bank Accounts fromt text file
		service.loadBankAccounts();

		while (true) {
			DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
			sock.receive(packet);
			byte[] encodedMsg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		
			try {
				// Receive
				BankMsg msg = coder.fromWire(encodedMsg);

				// Handle Authentication/Transaction Request
				msg = service.handleRequest(msg, packet.getSocketAddress().toString(), packet.getPort());

				// Put data in packet
				packet.setData(coder.toWire(msg));
				
				// Send packet
				sock.send(packet);
			} catch (IOException ioe) {
				System.err.println("Parse error in message: " + ioe.getMessage());
			}
		}
	}
}