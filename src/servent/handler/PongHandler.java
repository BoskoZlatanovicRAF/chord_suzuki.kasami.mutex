package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PongMessage;

public class PongHandler implements MessageHandler {

    private Message clientMessage;

    public PongHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PONG) {
            PongMessage pm = (PongMessage) clientMessage;
            AppConfig.chordState.getHealthCheckThread().onPongReceived(pm.getSenderChordId());
        } else {
            System.err.println("Received unexpected message type: " + clientMessage.getMessageType());
        }
    }
}
