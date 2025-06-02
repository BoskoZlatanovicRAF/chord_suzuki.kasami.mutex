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

            // sending ping to suspect node
            PingMessage ping = new PingMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    suspectPort
            );
            MessageUtil.sendMessage(ping);

            // wait for SOFT_LIMIT (4s)
            boolean received = waitForPong(suspectNodeId, 4000); // Implementacija ove metode ispod

            if (!received) {
                ConfirmedSuspectMessage csm = new ConfirmedSuspectMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        clientMessage.getSenderPort(),
                        suspectNodeId
                );
                MessageUtil.sendMessage(csm);
            }
        }
    }

    // Simple busy-wait
    private boolean waitForPong(int nodeId, int timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            Long last = AppConfig.chordState.getHealthCheckThread().getLastPongTime(nodeId);
            if (last != null && System.currentTimeMillis() - last < 2000) {
                return true; // received a pong within 2 seconds
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return false;
    }
}