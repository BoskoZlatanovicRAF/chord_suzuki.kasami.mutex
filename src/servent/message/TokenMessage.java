package servent.message;

import java.util.LinkedList;
import java.util.Queue;

public class TokenMessage extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;


    private int[] LN;
    private Queue<Integer> requestQueue;

    public TokenMessage(int senderPort, int receiverPort, int[] LN, Queue<Integer> requestQueue) {
        super(MessageType.TOKEN, senderPort, receiverPort);
        this.LN = LN.clone();
        this.requestQueue = new LinkedList<>(requestQueue);
    }

    public TokenMessage(int senderPort, int receiverPort, int nodeCount) {
        super(MessageType.TOKEN, senderPort, receiverPort);
        this.LN = new int[nodeCount];
        this.requestQueue = new LinkedList<>();
    }

    public int[] getLN() {
        return LN;
    }

    public void setLN(int[] LN) {
        this.LN = LN.clone();
    }

    public Queue<Integer> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(Queue<Integer> requestQueue) {
        this.requestQueue = new LinkedList<>(requestQueue);
    }
}
