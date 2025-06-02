package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NodeFailedMessage;

public class NodeFailedHandler implements MessageHandler {
    private Message clientMessage;

    public NodeFailedHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.NODE_FAILED) {
            NodeFailedMessage nfm = (NodeFailedMessage) clientMessage;
            int failedNodePort = nfm.getFailedNodePort();
            AppConfig.chordState.removeNodeByPort(failedNodePort);
            AppConfig.timestampedStandardPrint("Izbačen mrtav node: " + failedNodePort);
        }
    }
}