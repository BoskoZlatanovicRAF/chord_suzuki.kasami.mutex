package servent.message;

public class PingSuspectMessage extends BasicMessage {
    private int suspectNodeId;

    public PingSuspectMessage(int senderPort, int receiverPort, int suspectNodeId) {
        super(MessageType.PING_SUSPECT, senderPort, receiverPort);
        this.suspectNodeId = suspectNodeId;
    }

    public int getSuspectNodeId() {
        return suspectNodeId;
    }
}