/*
 * If Login fails, set Balance to -1
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class BankMsgTextCoder implements BankMsgCoder {
	/*
	 * Wire Format "BANKPROTO" <"a" | <"d" | "w">> [<RESPFLAG>] <USERNAME> [<PASSWORD>] <BALANCE> [<TRANSAMT>]
	 * Charset is fixed by the wire format.
	 */

	// Manifest constants for encoding
	public static final String MAGIC = "Bankin";
	public static final String AUTHSTR = "a";
	public static final String DEPOSITSTR = "d";
	public static final String WITHDRAWSTR = "w";
	public static final String RESPONSESTR = "R";

	public static final String CHARSETNAME = "US-ASCII";
  	public static final String DELIMSTR = " ";
  	public static final int MAX_WIRE_LENGTH = 2000;

  	public byte[] toWire(BankMsg msg) throws IOException {
  		String msgString = MAGIC + DELIMSTR
  					     + (msg.isAuthentication() ? AUTHSTR : (msg.isDeposit() ? DEPOSITSTR : WITHDRAWSTR))
  						 + DELIMSTR + (msg.isResponse() ? RESPONSESTR + DELIMSTR : "")
  						 + msg.getUsername() + DELIMSTR
  						 + (msg.isAuthentication() ? msg.getPassword() + DELIMSTR : "")
  						 + Double.toString(msg.getBalance()) + DELIMSTR
  						 + (msg.isAuthentication() ? "" : msg.getTransactionAmount());
  		byte data[] = msgString.getBytes(CHARSETNAME);
  		return data;
  	}

  	public BankMsg fromWire(byte[] message) throws IOException {
  		ByteArrayInputStream msgStream = new ByteArrayInputStream(message);
  		Scanner scan = new Scanner(new InputStreamReader(msgStream, CHARSETNAME));

  		// BankMsg Properties
  		boolean isResponse, isAuthentication = false, isDeposit = false;
  		String username, password = "";
  		Double balance, transactionAmount = 0.0;

  		String token;

  		try {
  			token = scan.next();
  			if (!token.equals(MAGIC)) {
  				throw new IOException("Bad magic string: " + token);
  			}
  			
  			token = scan.next();
  			if (token.equals(AUTHSTR)) {
  				isAuthentication = true;
  			} else {
  				// Last conditional is unecessary 
  				if (!token.equals(DEPOSITSTR) && !token.equals(WITHDRAWSTR)) {
  					throw new IOException("Bad deposit/withdraw indicator: " + token);
  				} else if (token.equals(DEPOSITSTR)) {
	  				isDeposit = true;
	  			} else if (token.equals(WITHDRAWSTR)) {
	  				isDeposit = false;
	  			}
  			}

  			token = scan.next();
  			if (token.equals(RESPONSESTR)) {
  				isResponse = true;
  				token = scan.next();
  			} else {
  				isResponse = false;
  			}

  			// Current token is username
  			username = token;
  			token = scan.next();

  			// If auth check password
  			if (isAuthentication) {
  				password = token;
  				token = scan.next();
  			}

  			// If response store balance if correct
  			// Right now I'm just going to store balance either way
  			balance = Double.parseDouble(token);

  			// If not response and balance will be correct after transaction, commit transaction
  			if (!isAuthentication) {
  				transactionAmount = Double.parseDouble(token);
  			}
  		} catch (IOException ioe) {
  			throw new IOException("Parse error...");
  		}

  		return new BankMsg(isResponse, isAuthentication, isDeposit, username, password, balance, transactionAmount);
  	}
}