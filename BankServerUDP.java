import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class BankServerUDP {

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			throw new IllegalArgumentException("Parameter(s): <Port>");
		}

		int port = Integer.parseInt(args[0]);

		Datagram sock = new DatagramSocket(port);

		byte[] inBuffer = new byte[BankMsgTextCoder.MAX_WIRE_LENGTH];
		// Change Bin to Text for a different coding approach
		BankMsgCoder coder = new BankMsgTextCoder();
		BankService service = new BankService();

		while (true) {
			DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
			sock.receive(packet);
			byte[] encodedMsg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
			System.out.println("Handling request from " + packet.getSocketAddress() + " ("
														+ econdedMsg.length + " bytes)");
		
			try {
				// Receive
				BankMsg msg = coder.fromWire(encodedMsg);

				// Make transaction/authentication
				msg = service.handleRequest(msg);

				// Put data in packet
				packet.setData(coder.toWire(msg));

				System.out.println("Sending response (" + packet.getLength() + " bytes):");
				System.out.println(msg);
				
				// Send packet
				sock.send(packet);
			} catch (IOException ioe) {
				System.err.println("Parse error in message: " + ioe.getMessage());
			}
		}
	}
}