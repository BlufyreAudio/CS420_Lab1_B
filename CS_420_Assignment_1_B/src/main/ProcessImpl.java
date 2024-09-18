package main;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ProcessImpl extends UnicastRemoteObject implements ProcessInterface {

    private String processId;
    private BSSManagerInterface bssManager;
    private Map<String, Integer> vectorClock;
    private Queue<Map.Entry<String, Map<String, Integer>>> messageQueue;

    public ProcessImpl(String processId, BSSManagerInterface bssManager) throws RemoteException {
        super();
        this.processId = processId;
        this.bssManager = bssManager;
        this.vectorClock = new HashMap<>();
        this.messageQueue = new LinkedList<>();
        this.vectorClock.put(processId, 0);
    }

    @Override
    public void send(String message) throws RemoteException {
        // Increment local clock
        vectorClock.put(processId, vectorClock.get(processId) + 1);

        // Send message with current vector clock
        bssManager.send(processId, message, new HashMap<>(vectorClock));
    }

    @Override
    public void deliver(String message, Map<String, Integer> messageClock) throws RemoteException {
        // Ensure causality - check vector clock condition
        boolean causallyDeliverable = true;
        for (String process : messageClock.keySet()) {
            if (!vectorClock.containsKey(process) || vectorClock.get(process) < messageClock.get(process) - 1) {
                causallyDeliverable = false;
                break;
            }
        }

        if (causallyDeliverable) {
            // Update local vector clock
            for (String process : messageClock.keySet()) {
                vectorClock.put(process, Math.max(vectorClock.getOrDefault(process, 0), messageClock.get(process)));
            }

            // Deliver the message
            System.out.println("Process " + processId + " delivered message: " + message);
        } else {
            // Queue message if causality cannot be guaranteed
            messageQueue.add(Map.entry(message, messageClock));
        }
    }

    @Override
    public void getToken() throws RemoteException {
        bssManager.acquireToken(processId);
    }

    // Additional method to process the message queue
    public void processQueue() throws RemoteException {
        while (!messageQueue.isEmpty()) {
            Map.Entry<String, Map<String, Integer>> entry = messageQueue.poll();
            deliver(entry.getKey(), entry.getValue());
        }
    }
}
