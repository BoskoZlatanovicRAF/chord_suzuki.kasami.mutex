package servent.message;

public class NodeFailedMessage extends BasicMessage {
    private int failedNodePort;

    public NodeFailedMessage(int senderPort, int receiverPort, int failedNodePort) {
        super(MessageType.NODE_FAILED, senderPort, receiverPort);
        this.failedNodePort = failedNodePort;
    }

    public int getFailedNodePort() {
        return failedNodePort;
    }
}