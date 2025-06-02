package servent.message;

public class PongMessage extends BasicMessage {
    private int senderChordId;

    public PongMessage(int senderPort, int receiverPort, int senderChordId) {
        super(MessageType.PONG, senderPort, receiverPort);
        this.senderChordId = senderChordId;
    }

    public int getSenderChordId() {
        return senderChordId;
    }
}