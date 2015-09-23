public class SourceAddress {

	private String sourceAddress;
	private int sourcePort;
	private String challenge;
	private int messagesReceivedCount;

	public SourceAddress(String sourceAddress, int sourcePort, String challenge) {
		this.sourceAddress = sourceAddress;
		this.sourcePort = sourcePort;
		this.challenge = challenge;
		this.messagesReceivedCount = 1; // Received Challenge Request Message
	}

	public String getSourceAddress() {
	    return this.sourceAddress;
	}

	public int getSourcePort() {
	    return this.sourcePort;
	}

	public String getChallenge() {
	    return this.challenge;
	}

	public int getMessagesReceivedCount() {
	    return this.messagesReceivedCount;
	}

	public void setSourceAddress(String sourceAddress) {
	    this.sourceAddress = sourceAddress;
	}

	public void setSourcePort(int sourcePort) {
	    this.sourcePort = sourcePort;
	}

	public void setChallenge(String challenge) {
	    this.challenge = challenge;
	}

	public void setMessagesReceivedCount(int messagesReceivedCount) {
	    this.messagesReceivedCount = messagesReceivedCount;
	}

	public void incrementMessagesReceivedCount() {
		this.messagesReceivedCount++;
	}
}