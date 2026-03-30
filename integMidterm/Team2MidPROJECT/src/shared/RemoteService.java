package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteService extends Remote {
    String processRequest(String xmlRequest) throws RemoteException;
}