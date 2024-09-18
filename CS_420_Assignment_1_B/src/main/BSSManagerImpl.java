package main;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BSSManagerImpl extends UnicastRemoteObject implements BSSManagerInterface {

    private String tokenHolder;
    private Map<String, Integer> globalClock;
    private Queue<Map.Entry<String, Map.Entry<String, Map<String, Integer>>>> messageQueue;

    public BSSManagerImpl(String initialTokenHolder) throws RemoteException {
        super();
        this.tokenHolder = initialTokenHolder;
        this.globalClock = new HashMap<>();
        this.messageQueue = new LinkedList<>();
    }

    @Override
    public void send(String processId, String message, Map<String, Integer> clock) throws RemoteException {
        if (processId.equals(tokenHolder)) {
            // Token holder processes the message
            deliverMessagesCausally(processId, message, clock);
        } else {
            // Queue the message for later delivery
            messageQueue.add(Map.entry(processId, Map.entry(message, clock)));
        }
    }

    private void deliverMessagesCausally(String processId, String message, Map<String, Integer> clock) throws RemoteException {
        // Update global clock
        for (String process : clock.keySet()) {
            globalClock.put(process, Math.max(globalClock.getOrDefault(process, 0), clock.get(process)));
        }

        // Deliver the message to the respective process
        ProcessInterface process = (ProcessInterface) java.rmi.Naming.lookup("//localhost/" + processId);
        process.deliver(message, clock);

        // Process any other queued messages if causality is met
        while (!messageQueue.isEmpty()) {
            Map.Entry<String, Map.Entry<String, Map<String, Integer>>> entry = messageQueue.poll();
            process.deliver(entry.getValue().getKey(), entry.getValue().getValue());
        }

        // Optionally release the token
        releaseToken();
    }

    @Override
    public void acquireToken(String processId) throws RemoteException {
        if (tokenHolder.equals(processId)) {
            System.out.println("Process " + processId + " already holds the token.");
        } else {
            System.out.println("Process " + processId + " acquiring token from " + tokenHolder);
            tokenHolder = processId;
        }
    }

    @Override
    public void releaseToken() throws RemoteException {
        // Example logic: Token passed in round-robin order (or any other policy)
        System.out.println("Token released.");
        // Token assignment logic can be implemented as needed
    }
}
