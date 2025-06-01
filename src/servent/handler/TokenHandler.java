package servent.handler;


import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TokenMessage;

public class TokenHandler implements MessageHandler {

    private Message clientMessage;

    public TokenHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.TOKEN) {
            TokenMessage tokenMessage = (TokenMessage) clientMessage;

            AppConfig.timestampedStandardPrint("TokenHandler: Received TOKEN from " + tokenMessage.getSenderPort() );


        }
    }
}
