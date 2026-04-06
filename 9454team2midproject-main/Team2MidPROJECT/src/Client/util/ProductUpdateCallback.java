package Client.util;

import shared.ClientCallback;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ProductUpdateCallback extends UnicastRemoteObject implements ClientCallback {

    private final Runnable onProductUpdate;
    private final java.util.function.Consumer<String> onMessage;

    public ProductUpdateCallback(Runnable onProductUpdate,
                                 java.util.function.Consumer<String> onMessage)
            throws RemoteException {
        super();
        this.onProductUpdate = onProductUpdate;
        this.onMessage = onMessage;
    }

    @Override
    public void onProductUpdated(String productId) throws RemoteException {
        javafx.application.Platform.runLater(onProductUpdate);
    }

    @Override
    public void onServerMessage(String message) throws RemoteException {
        if (onMessage != null) {
            javafx.application.Platform.runLater(() -> onMessage.accept(message));
        }
    }
}