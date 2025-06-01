package servent.handler;

import app.AppConfig;
import servent.message.LeaveNodeMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

import java.util.Map;

public class LeaveNodeHandler implements MessageHandler {
    private Message clientMessage;

    public LeaveNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LEAVE_NODE) {
            AppConfig.mutexManager.lock();
            try {
                LeaveNodeMessage leaveMsg = (LeaveNodeMessage) clientMessage;
                int leavingNodePort = clientMessage.getSenderPort();

                AppConfig.timestampedStandardPrint("Node " + leavingNodePort + " is leaving - taking over its data");

                // Preuzmi podatke od čvora koji odlazi
                Map<Integer, Integer> incomingValues = leaveMsg.getTransferredValues();
                Map<Integer, Integer> myValues = AppConfig.chordState.getValueMap();

                // Dodaj sve njegove podatke u moje
                for (Map.Entry<Integer, Integer> entry : incomingValues.entrySet()) {
                    myValues.put(entry.getKey(), entry.getValue());
                    AppConfig.timestampedStandardPrint("Inherited key " + entry.getKey() + " with value " + entry.getValue());
                }
                AppConfig.chordState.setValueMap(myValues);

                // Pokreni reorganizaciju sistema - obavesti sve ostale čvorove
                AppConfig.timestampedStandardPrint("Starting system reorganization after node " + leavingNodePort + " left");

                UpdateMessage um = new UpdateMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.chordState.getNextNodePort(),
                        "REMOVE:" + leavingNodePort
                );
                MessageUtil.sendMessage(um);
            } finally {
                AppConfig.mutexManager.unlock();
            }

        } else {
            AppConfig.timestampedErrorPrint("LEAVE_NODE handler got message that is not LEAVE_NODE");
        }
    }
}
