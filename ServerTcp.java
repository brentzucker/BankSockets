import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTcp {

	public static void main(String[] args) throws Exception {

		if (args.length != 1 && args.length !=2) { // Test for correct # of args
	      throw new IllegalArgumentException("Parameter(s): <Port>");
	    }

	    if (args.length == 2 && args[1].equals("-d"))// Consume -d for debugger
	    	Debugger.setEnabled(true);

	    int port = Integer.parseInt(args[0]); // Receiving Port
	    ServerSocket servSock = new ServerSocket(port);

		// Change Bin to Text for a different coding approach
		BankMsgCoder coder = new BankMsgTextCoder();
		BankService service = new BankService();

		// Load Bank Accounts fromt text file
		service.loadBankAccounts();

		while (true) {
			Socket clntSock = servSock.accept();
			Debugger.log("Handling client at " + clntSock.getRemoteSocketAddress());

			// Change Length to Delim for a different framing strategy
      		Framer framer = new LengthFramer(clntSock.getInputStream());
      		try {
      			byte[] req;
      			while ((req = framer.nextMsg()) != null) {
      				
      				Debugger.log("Received message (" + req.length + " bytes)");
          			BankMsg responseMsg = service.handleRequest(coder.fromWire(req), clntSock.getRemoteSocketAddress().toString(), clntSock.getPort());
          			framer.frameMsg(coder.toWire(responseMsg), clntSock.getOutputStream());
      			}
      		} catch (IOException ioe) {
      			System.err.println("Error handling client: " + ioe.getMessage());
      		} finally {
      			Debugger.log("Closing connection");
      			clntSock.close();
      		}
		}
	}
}