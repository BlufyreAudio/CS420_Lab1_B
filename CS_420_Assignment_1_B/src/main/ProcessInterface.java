package main;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ProcessInterface extends Remote {
    void send(String message) throws RemoteException;
    void deliver(String message, Map<String, Integer> messageClock) throws RemoteException;
    void getToken() throws RemoteException;  // For token acquisition
}