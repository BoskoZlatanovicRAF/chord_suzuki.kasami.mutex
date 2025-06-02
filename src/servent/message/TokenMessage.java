package servent.message;

import java.util.LinkedList;
import java.util.Queue;

/**
 * - TokenMessage is used in a distributed system to manage access to a shared resource
 * using a token-based approach. It contains the logical clock values (LN) and a queue
 * of requests from nodes that are waiting for the token.
 *
 *-  Deep copies of LN and requestQueue are made to ensure each TokenMessage has its own independent state.
 * This prevents shared references between nodes or threads, avoiding race conditions and unintended side
 * effects in the Suzuki-Kasami algorithm.
 */

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
