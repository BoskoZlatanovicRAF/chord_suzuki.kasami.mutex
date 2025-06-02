package app;

import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HealthCheckThread implements Runnable, Cancellable {

    private static final long PING_INTERVAL = 2000;     // ping every 2 seconds
    private static final long SOFT_LIMIT = 4000;        // 4 seconds - suspicious
    private static final long HARD_LIMIT = 10000;       // 10 seconds - dead

    private Map<Integer, Long> lastPongTime = new HashMap<>();
    private Map<Integer, Boolean> isSuspect = new HashMap<>();

    private volatile boolean working = true;


    @Override
    public void run() {
        while (working) {
            try {

                // predecessor and successor are null in some cases cant find out why
                AppConfig.timestampedErrorPrint("HealthCheckThread sucessor ceo" + Arrays.toString(AppConfig.chordState.getSuccessorTable()));
                AppConfig.timestampedErrorPrint("HealthCheckThread prvi successor" + AppConfig.chordState.getSuccessorTable()[0]);
                AppConfig.timestampedErrorPrint("HealthCheckThread predecessor" + AppConfig.chordState.getPredecessor());

                ServentInfo succ = AppConfig.chordState.getSuccessorTable()[0];
                ServentInfo pred = AppConfig.chordState.getPredecessor();

                // PING both successor and predecessor
                if (succ != null) sendPing(succ);
                if (pred != null) sendPing(pred);

                checkStatus(succ);
                checkStatus(pred);

                Thread.sleep(PING_INTERVAL);
            } catch (Exception e) {
                AppConfig.timestampedErrorPrint("HealthCheckThread exception: " + e.getMessage());
            }
        }


    }

    private void sendPing(ServentInfo node) {
        if (node == null) return;
        PingMessage ping = new PingMessage(AppConfig.myServentInfo.getListenerPort(), node.getListenerPort());
        MessageUtil.sendMessage(ping);
    }

    public void onPongReceived(int nodeId) {
        lastPongTime.put(nodeId, System.currentTimeMillis());
        isSuspect.put(nodeId, false);
    }

    private void checkStatus(ServentInfo node) {
        if (node == null) return;
        int nodeId = node.getChordId();
        long now = System.currentTimeMillis();
        long last = lastPongTime.getOrDefault(nodeId, now);

        // doesnt work
        if (isBuddyOf(nodeId)) {
            restoreBackupFor(nodeId);
        }

        if (!isSuspect.getOrDefault(nodeId, false) && now - last > SOFT_LIMIT) {
            AppConfig.timestampedStandardPrint("Node " + nodeId + " je sumnjiv!");
            isSuspect.put(nodeId, true);

            // Buddy check
            ServentInfo buddy = getBuddyForNode(nodeId);
            if (buddy != null && buddy.getChordId() != nodeId) {
                PingSuspectMessage msg = new PingSuspectMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        buddy.getListenerPort(),
                        nodeId // suspicious node ID
                );
                MessageUtil.sendMessage(msg);
            }


        }

        if (now - last > HARD_LIMIT) {
            // Dead
            AppConfig.timestampedStandardPrint("Node " + nodeId + " je mrtav!");
            handleNodeFailure(nodeId);
        }
    }

    private void handleNodeFailure(int nodeId) {
        AppConfig.timestampedStandardPrint("HANDLENODEFAILURE: Node " + nodeId + " se uklanja iz sistema!");

        // 1. Remove node from Chord state
        AppConfig.chordState.removeNodeById(nodeId);

        int failedNodePort = AppConfig.chordState.getPortForNodeId(nodeId);
        // 2. Notify other nodes about the failure
        for (ServentInfo node : AppConfig.chordState.getAllNodeInfo()) {
            if (node.getChordId() != AppConfig.myServentInfo.getChordId()) {
                NodeFailedMessage msg = new NodeFailedMessage(
                        AppConfig.myServentInfo.getListenerPort(),
                        node.getListenerPort(),
                        failedNodePort // port of the failed node
                );
                MessageUtil.sendMessage(msg);
            }
        }



        // 4. Token recovery: ask everyone if they have the token
        checkAndRecoverTokenIfNeeded(nodeId);
    }

    private final Map<Integer, Boolean> tokenResponses = new ConcurrentHashMap<>();
    private CountDownLatch tokenInquiryLatch;

    // todo: needs refactoring, this is a bit messy
    public void checkAndRecoverTokenIfNeeded(int failedNodeId) {
        List<ServentInfo> allNodes = AppConfig.chordState.getAllNodeInfo();
        int myPort = AppConfig.myServentInfo.getListenerPort();

        tokenResponses.clear();
        tokenInquiryLatch = new CountDownLatch(allNodes.size() - 1);

        for (ServentInfo node : allNodes) {
            if (node.getListenerPort() == myPort) continue;
            MessageUtil.sendMessage(new HaveTokenMessage(myPort, node.getListenerPort()));
        }

        try {
            tokenInquiryLatch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}

        boolean someoneHasToken = tokenResponses.values().stream().anyMatch(b -> b);

        if (!someoneHasToken) {
            AppConfig.timestampedStandardPrint("Token NESTAO! Kreiram novi token...");
            int nodeCount = ChordState.CHORD_SIZE;
            TokenMessage newToken = new TokenMessage(myPort, myPort, nodeCount);
            MessageUtil.sendMessage(newToken);
        } else {
            AppConfig.timestampedStandardPrint("Token postoji kod nekog, recovery NIJE potreban.");
        }
    }

    public void tokenResponseReceived(int fromPort, boolean hasToken) {
        tokenResponses.put(fromPort, hasToken);
        if (tokenInquiryLatch != null) tokenInquiryLatch.countDown();
    }

    public void restoreBackupFor(int nodeId) {
        Map<Integer, Integer> backupData = AppConfig.chordState.getBackupMap().get(nodeId);
        if (backupData == null) {
            AppConfig.timestampedStandardPrint("Nema backup podataka za node " + nodeId);
            return;
        }

        for (Map.Entry<Integer, Integer> entry : backupData.entrySet()) {
            AppConfig.chordState.getValueMap().put(entry.getKey(), entry.getValue());
            AppConfig.timestampedStandardPrint("Restored backup key: " + entry.getKey() + " value: " + entry.getValue());
        }
        AppConfig.chordState.getBackupMap().remove(nodeId);
    }

    private boolean isBuddyOf(int nodeId) {
        ServentInfo succ = AppConfig.chordState.getSuccessorTable()[0];
        ServentInfo pred = AppConfig.chordState.getPredecessor();
        return (succ != null && succ.getChordId() == nodeId) ||
                (pred != null && pred.getChordId() == nodeId);
    }

    private ServentInfo getBuddyForNode(int nodeId) {
        int myId = AppConfig.myServentInfo.getChordId();
        ServentInfo successor = AppConfig.chordState.getSuccessorTable()[0];
        ServentInfo predecessor = AppConfig.chordState.getPredecessor();

        if (successor != null && successor.getChordId() == nodeId && predecessor != null) {
            return predecessor;
        } else if (predecessor != null && predecessor.getChordId() == nodeId && successor != null) {
            return successor;
        } else {
            return null;
        }
    }

    @Override
    public void stop() {
        working = false;
    }

    public Long getLastPongTime(int nodeId) {
        return lastPongTime.get(nodeId);
    }
}
