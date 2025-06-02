package servent.message;

public class PingMessage extends BasicMessage{

    private static final long serialVersionUID = 65784980932L;

    public PingMessage(int senderPort, int receiverPort) {
        super(MessageType.PING, senderPort, receiverPort);
    }
}
