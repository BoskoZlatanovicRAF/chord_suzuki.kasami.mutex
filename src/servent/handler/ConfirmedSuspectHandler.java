package servent.handler;

import app.AppConfig;
import servent.message.ConfirmedSuspectMessage;
import servent.message.Message;
import servent.message.MessageType;

public class ConfirmedSuspectHandler implements MessageHandler {
    private Message clientMessage;

    public ConfirmedSuspectHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.CONFIRMED_SUSPECT) {
            ConfirmedSuspectMessage csm = (ConfirmedSuspectMessage) clientMessage;
            int suspectNodeId = csm.getSuspectNodeId();

            AppConfig.timestampedStandardPrint("Node " + suspectNodeId + " potvrđen kao sumnjiv od strane trećeg čvora.");

        }
    }
}