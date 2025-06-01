package servent.message;

public class TokenRequestMessage extends BasicMessage {

    private static final long serialVersionUID = -37265143217654321L;

    private int requestingNodeId;
    private int requestNumber;

    public TokenRequestMessage(int senderPort, int receiverPort, int requestingNodeId, int requestNumber) {
        super(MessageType.TOKEN_REQUEST, senderPort, receiverPort, requestingNodeId + ":" + requestNumber);
        this.requestingNodeId = requestingNodeId;
        this.requestNumber = requestNumber;
    }

    public int getRequestingNodeId() {
        return requestingNodeId;
    }

    public int getRequestNumber() {
        return requestNumber;
    }
}