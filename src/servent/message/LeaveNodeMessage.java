package servent.message;

import java.util.Map;

public class LeaveNodeMessage extends BasicMessage {

    private static final long serialVersionUID = -1234567890123456789L;

    private Map<Integer, Integer> transferredValues;

    public LeaveNodeMessage(int senderPort, int receiverPort, Map<Integer, Integer> values) {
        super(MessageType.LEAVE_NODE, senderPort, receiverPort);
        this.transferredValues = values;
    }

    public Map<Integer, Integer> getTransferredValues() {
        return transferredValues;
    }
}