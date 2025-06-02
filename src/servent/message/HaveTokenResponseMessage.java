package servent.message;

public class HaveTokenResponseMessage extends BasicMessage {
    private final boolean hasToken;

    public HaveTokenResponseMessage(int senderPort, int receiverPort, boolean hasToken) {
        super(MessageType.HAVE_TOKEN_RESPONSE, senderPort, receiverPort);
        this.hasToken = hasToken;
    }

    public boolean hasToken() {
        return hasToken;
    }
}