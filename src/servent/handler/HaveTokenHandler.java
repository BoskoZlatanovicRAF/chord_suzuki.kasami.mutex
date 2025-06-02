package servent.handler;

import app.AppConfig;
import mutex.SuzukiKasamiMutex;
import servent.message.HaveTokenResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class HaveTokenHandler implements MessageHandler {
    private Message clientMessage;

    public HaveTokenHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HAVE_TOKEN) {// prilagodi ako imaš getter
            ((SuzukiKasamiMutex)AppConfig.mutexManager).hasToken();
            HaveTokenResponseMessage response = new HaveTokenResponseMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderPort(),
                    ((SuzukiKasamiMutex)AppConfig.mutexManager).hasToken()
            );
            MessageUtil.sendMessage(response);
        }
    }
}