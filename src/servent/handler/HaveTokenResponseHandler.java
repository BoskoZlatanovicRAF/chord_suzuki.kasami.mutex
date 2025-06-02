package servent.handler;

import app.AppConfig;
import servent.message.HaveTokenResponseMessage;
import servent.message.Message;
import servent.message.MessageType;

public class HaveTokenResponseHandler implements MessageHandler {
    private Message clientMessage;

    public HaveTokenResponseHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HAVE_TOKEN_RESPONSE) {
            HaveTokenResponseMessage msg = (HaveTokenResponseMessage) clientMessage;
            AppConfig.chordState.getHealthCheckThread().tokenResponseReceived(msg.getSenderPort(), msg.hasToken());
        }
    }
}