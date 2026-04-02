package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteService extends Remote {
    String processRequest(String xmlRequest) throws RemoteException;
    void registerCallback(String username, ClientCallback callback) throws RemoteException;
    void unregisterCallback(String username) throws RemoteException;
}