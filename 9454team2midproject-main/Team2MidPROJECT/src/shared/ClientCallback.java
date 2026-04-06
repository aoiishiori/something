package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void onProductUpdated(String productId) throws RemoteException;
    void onServerMessage(String message) throws RemoteException;
}