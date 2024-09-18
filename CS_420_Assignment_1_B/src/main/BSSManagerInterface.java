package main;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface BSSManagerInterface extends Remote {
    void send(String processId, String message, Map<String, Integer> clock) throws RemoteException;
    void acquireToken(String processId) throws RemoteException;  // Optional token acquisition
    void releaseToken() throws RemoteException;  // Optional token release
}