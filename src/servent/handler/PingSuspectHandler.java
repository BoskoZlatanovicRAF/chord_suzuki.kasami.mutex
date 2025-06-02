package servent.handler;

import app.AppConfig;
import servent.message.*;
import servent.message.util.MessageUtil;

public class PingSuspectHandler implements MessageHandler {

    private Message clientMessage;

    public PingSuspectHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PING_SUSPECT) {
            PingSuspectMessage msg = (PingSuspectMessage) clientMessage;
            int suspectNodeId = msg.getSuspectNodeId();
            int suspectPort = AppConfig.chordState.getPortForNodeId(suspectNodeId);

            // Šaljemo ping sumnjivom čvoru
            PingMessage ping = new PingMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    suspectPort
            );
            MessageUtil.sendMessage(ping);

            // Čekamo da li dobijamo PONG u roku SOFT_LIMIT (4s)
            boolean received = waitForPong(suspectNodeId, 4000); // Implementacija ove metode ispod

            if (!received) {
                // Pošalji ConfirmedSuspectMessage tražiocu
                ConfirmedSuspectMessage csm = new ConfirmedSuspectMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        clientMessage.getSenderPort(),
                        suspectNodeId
                );
                MessageUtil.sendMessage(csm);
            }
            // Ako dobiješ PONG, ništa ne šalješ (ili šalješ opcioni SuspectOkMessage)
        }
    }

    // Simple busy-wait; možeš koristiti i fensi Java Future/Condition/Timer
    private boolean waitForPong(int nodeId, int timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            Long last = AppConfig.chordState.getHealthCheckThread().getLastPongTime(nodeId);
            if (last != null && System.currentTimeMillis() - last < 2000) {
                return true; // Primili smo skoro PONG
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return false;
    }
}