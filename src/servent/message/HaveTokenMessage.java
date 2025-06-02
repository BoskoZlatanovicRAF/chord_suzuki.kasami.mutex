package servent.message;

public class HaveTokenMessage extends BasicMessage {
    public HaveTokenMessage(int senderPort, int receiverPort) {
        super(MessageType.HAVE_TOKEN, senderPort, receiverPort);
    }
}