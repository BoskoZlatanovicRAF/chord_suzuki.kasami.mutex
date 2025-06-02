package mutex;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.TokenMessage;
import servent.message.TokenRequestMessage;
import servent.message.util.MessageUtil;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SuzukiKasamiMutex implements Mutex {

    private int[] RN;  // Request numbers for all nodes
    private TokenMessage currentToken = null;
    private boolean hasToken = false;
    private int myNodeId;
    private int nodeCount;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition tokenReceived = lock.newCondition();
    private boolean waitingForToken = false;

    public SuzukiKasamiMutex(boolean isFirstNode) {
        this.nodeCount = ChordState.CHORD_SIZE;
        this.RN = new int[nodeCount];
        this.myNodeId = AppConfig.myServentInfo.getChordId();

        if (isFirstNode) {
            int myPort = AppConfig.myServentInfo.getListenerPort();
            currentToken = new TokenMessage(myPort, myPort, nodeCount);
            hasToken = true;
            AppConfig.timestampedStandardPrint("I am node " + AppConfig.myServentInfo.getChordId() + " - starting with TOKEN");
        }
    }


    @Override
    public void lock() {
        lock.lock();
        try {
            if (hasToken) {
                AppConfig.timestampedStandardPrint("Already have TOKEN - entering critical section");
                return;
            }

            // Update my request number
            RN[myNodeId]++;
            AppConfig.timestampedStandardPrint("Requesting TOKEN (request #" + RN[myNodeId] + ")");

            broadcastTokenRequest();

            waitingForToken = true;
            while (!hasToken) {
                tokenReceived.await();
            }
            waitingForToken = false;

            AppConfig.timestampedStandardPrint("Acquired TOKEN - entering critical section");

        } catch (InterruptedException e) {
            AppConfig.timestampedErrorPrint("Thread interrupted while waiting for TOKEN: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        }

        finally {
            lock.unlock();
        }
    }

    @Override
    public void unlock() {
        lock.lock();
        try {
            if (!hasToken) {
                AppConfig.timestampedErrorPrint("Cannot unlock – don't have TOKEN");
                return;
            }

            AppConfig.timestampedStandardPrint("Releasing TOKEN");

            // 1) update LN
            currentToken.getLN()[myNodeId] = RN[myNodeId];

            // 2) queue anyone whose RN == LN+1
            Queue<Integer> queue = currentToken.getRequestQueue();
            for (int i = 0; i < nodeCount; i++) {
                if (i != myNodeId && RN[i] == currentToken.getLN()[i] + 1 && !queue.contains(i)) {
                    queue.add(i);
                    AppConfig.timestampedStandardPrint("Enqueued node " + i); // ovo se ne ispisuje nikad
                }
            }

            // 3) if there’s someone waiting, send the token and _then_ clear your state
            if (!queue.isEmpty()) {
                int nextId = queue.poll();
                int nextPort = getPortForNodeId(nextId);
                AppConfig.timestampedStandardPrint("Sending TOKEN to node " + nextId);

                TokenMessage tm = new TokenMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        nextPort,
                        currentToken.getLN(),
                        currentToken.getRequestQueue()
                );
                MessageUtil.sendMessage(tm);

                // only now do we truly give up the token
                hasToken = false;
                currentToken = null;
            }
            // else: keep the token locally and don’t clear it

        } finally {
            lock.unlock();
        }
    }

    private void broadcastTokenRequest() {
        int myId = AppConfig.myServentInfo.getChordId();
        int myPort = AppConfig.myServentInfo.getListenerPort();
        int requestNumber = RN[myId];
        ServentInfo successor = AppConfig.chordState.getSuccessorTable()[0];

        // If I am the only node or my successor is myself, no need to send a request
        if (successor.getChordId() == myId) {
            return;
        }

        TokenRequestMessage req = new TokenRequestMessage(
                myPort,
                successor.getListenerPort(),
                myId,
                requestNumber
        );
        MessageUtil.sendMessage(req);
    }


    // hardcoded port numbers for nodes
    private int getPortForNodeId(int nodeId) {
//        switch (nodeId) {
//            case 0: return 1100;
//            case 1: return 1200;
//            case 2: return 1300;
//            case 3: return 1400;
//            case 4: return 1600;
//            default: return -1;
//        }
        AppConfig.timestampedStandardPrint("AAAAAA" + AppConfig.chordState.getAllNodeInfo());
        for (ServentInfo node : AppConfig.chordState.getAllNodeInfo()) {
            if (node.getChordId() == nodeId) {
                return node.getListenerPort();
            }
        }
        throw new IllegalArgumentException("Node ID not found: " + nodeId);
    }

    public void onTokenRequest(int fromNodeId, int requestNumber, int fromPort) {
        lock.lock();
        try {
            AppConfig.timestampedStandardPrint("Processing TOKEN_REQUEST from node " + fromNodeId +
                    " with request #" + requestNumber);

            RN[fromNodeId] = Math.max(RN[fromNodeId], requestNumber);

            // if I have the token and the request is higher than my LN, I send the token
            if (hasToken && !waitingForToken) {
                AppConfig.timestampedStandardPrint("Have TOKEN and not using it - sending to node " + fromNodeId);

                TokenMessage tokenToSend = new TokenMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        fromPort,
                        currentToken.getLN(),
                        currentToken.getRequestQueue()
                );

                MessageUtil.sendMessage(tokenToSend);

                hasToken = false;
                currentToken = null;
            }

        } finally {
            lock.unlock();
        }
    }

    public void onTokenReceived(TokenMessage token) {
        lock.lock();
        try {
            AppConfig.timestampedStandardPrint("Received TOKEN from " + token.getSenderPort());

            currentToken = token;
            hasToken = true;

            if (waitingForToken) {
                tokenReceived.signalAll();
            }

        } finally {
            lock.unlock();
        }
    }

    public boolean hasToken() {
        return hasToken;
    }
}