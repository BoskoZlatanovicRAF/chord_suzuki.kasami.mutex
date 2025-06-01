package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TokenRequestMessage;
import servent.message.util.MessageUtil;

import static app.AppConfig.mutexManager;

public class TokenRequestHandler implements MessageHandler {

    private Message clientMessage;

    public TokenRequestHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TOKEN_REQUEST) {
            TokenRequestMessage trm = (TokenRequestMessage) clientMessage;
            int requestingNodeId = trm.getRequestingNodeId();
            int requestNumber = trm.getRequestNumber();
            int requestingPort = trm.getSenderPort();

            AppConfig.timestampedStandardPrint("Received TOKEN_REQUEST from node " + requestingNodeId + " with request number " + requestNumber);

            ((SuzukiKasamiMutex)mutexManager).onTokenRequest(requestingNodeId, requestNumber, requestingPort);

            int myId = AppConfig.myServentInfo.getChordId();
            if (requestingNodeId != myId) {
                ServentInfo successor = AppConfig.chordState.getSuccessorTable()[0];
                TokenRequestMessage forwardMsg = new TokenRequestMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        successor.getListenerPort(),
                        requestingNodeId,
                        requestNumber
                );
                MessageUtil.sendMessage(forwardMsg);
            } else {
                AppConfig.timestampedStandardPrint("TOKEN_REQUEST se vratio kod pošiljaoca, broadcast gotov.");
            }

        } else {
            AppConfig.timestampedErrorPrint("TOKEN_REQUEST handler got message that is not TOKEN_REQUEST");
        }
    }
}