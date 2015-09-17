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

  	// public BankMsg fromWire(byte[] message) throws IOException {

  	// 	return new BankMsg(isResponse, isAuthentication, isDeposit, username, password, balance, transactionAmount);
  	// }
}