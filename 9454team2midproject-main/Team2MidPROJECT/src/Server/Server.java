package Server;

import Server.util.CallbackManager;
import Server.util.JsonUtils;
import Server.util.JsonWriter;
import Server.util.ServerLogger;
import shared.ClientCallback;
import shared.RemoteService; // new interface for RMI
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements RemoteService {

    private static final int RMI_PORT = 1099;
    private static volatile boolean paused = false;
    private static Registry registry;

    public Server() throws RemoteException {
        super();
    }

    @Override
    public void registerCallback(String username, ClientCallback callback) throws RemoteException {
        CallbackManager.register(username, callback);
    }

    @Override
    public void unregisterCallback(String username) throws RemoteException {
        CallbackManager.unregister(username);
    }
    @Override
    public String processRequest(String jsonRequest) throws RemoteException {
        if (paused) {
            return JsonUtils.createResponse("ERROR", "Server is paused. Please wait.");
        }
        return new RequestHandler().processRequest(jsonRequest);
    }



    public static void main(String[] args) {
        JsonWriter.initializeDataFiles();

        try {
            Server server = new Server();
            registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind("FoodWasteService", server);

            ServerLogger.logServerStart(RMI_PORT);
            System.out.println("===========================================");
            System.out.println("  Food-Waste Reducer System - Server");
            System.out.println("  SDG 12: Responsible Consumption");
            System.out.println("===========================================");
            System.out.println("RMI Server running on port " + RMI_PORT);
            System.out.println("Commands: 'pause', 'resume', 'stop'\n");

            // Console loop on main thread
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine().trim().toLowerCase();
                switch (cmd) {
                    case "pause":
                        paused = true;
                        System.out.println("Server paused. Clients will be blocked.");
                        ServerLogger.logTransaction("SYSTEM", "SERVER_PAUSED", "N/A");
                        break;
                    case "resume":
                    case "start":
                        paused = false;
                        System.out.println("Server resumed.");
                        ServerLogger.logTransaction("SYSTEM", "SERVER_RESUMED", "N/A");
                        break;
                    case "stop":
                    case "exit":
                        System.out.println("Shutting down...");
                        ServerLogger.logServerShutdown();
                        UnicastRemoteObject.unexportObject(server, true);
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unknown command. Use: pause, resume, stop");
                }
            }

        } catch (Exception e) {
            ServerLogger.logError("Server failed: " + e.getMessage());
            e.printStackTrace();
        }


    }
}