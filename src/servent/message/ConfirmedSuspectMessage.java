package servent.message;

public class ConfirmedSuspectMessage extends BasicMessage {
    private int suspectNodeId;

    public ConfirmedSuspectMessage(int senderPort, int receiverPort, int suspectNodeId) {
        super(MessageType.CONFIRMED_SUSPECT, senderPort, receiverPort);
        this.suspectNodeId = suspectNodeId;
    }

    public int getSuspectNodeId() {
        return suspectNodeId;
    }
}