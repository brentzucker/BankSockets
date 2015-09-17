import java.io.IOException;

public interface BankMsgCoder {
	byte[] toWire(BankMsg msg) throws IOException;
	BankMsg fromWire(byte[] input) throws IOException;
}