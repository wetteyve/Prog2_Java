import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Server {
    private volatile boolean running = true;
    // synchronization elements
    private final Lock clientMutex = new ReentrantLock();

    // information lists
    private List<ServerNetworkHandler> clients;

    private Logger logger = Logger.getLogger("server");

    // Server Socket
    private ServerSocket serverSocket;

    public Server(int serverPort) throws IOException {
        // configure logger
        InputStream config = this.getClass().getResourceAsStream("log.properties");
        LogManager.getLogManager().readConfiguration(config);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);
        FileHandler fileHandler = new FileHandler("server-%u-%g.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(Level.ALL);
        logger.addHandler(fileHandler);

        // Create Server Socket
        logger.info("Create Server");
        serverSocket = new ServerSocket(serverPort);
        logger.info("Listening on "
            + InetAddress.getLocalHost() + ":"
            + serverSocket.getLocalPort());
        // Create all needed Lists
        clients = new LinkedList<ServerNetworkHandler>();
    }

    public void stop() {
        disconnectAll();
        running = false;
    }

    public static void main(String[] args) {
        int serverPort = SimpleChatProtocol.DEFAULT_PORT;
        // parse arguments
        switch(args.length) {
            case 1: serverPort = Integer.parseInt(args[0]);
            case 0: break;
            default:
                throw new IllegalArgumentException("Parameters: <Port>");
        }
        try {
            new Server(serverPort).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void execute() {
        while (running && serverSocket.isBound()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);

                ServerNetworkHandler client =
                    new ServerNetworkHandler(clientSocket, this);
                Thread thread = new Thread(client);
                thread.start();
                clientMutex.lock();
                clients.add(client);
                clientMutex.unlock();
                logger.info("Connect new Client IP/Port <"
                    + clientSocket.getRemoteSocketAddress() + "/"
                    + clientSocket.getPort() + ">  Username <"
                    + client.getUsername() + ">");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Accept failed", e);
            }
        }
    }

    public boolean existUsername(String username) {
        boolean exist = false;
        clientMutex.lock();
        for (ServerNetworkHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(username)) {
                exist = true;
                break;
            }
        }
        clientMutex.unlock();
        return exist;
    }

    public void sendToAll(String message) {
        clientMutex.lock();
        Iterator<ServerNetworkHandler> clientIt = clients.iterator();
        while (clientIt.hasNext()) {
            ServerNetworkHandler client = clientIt.next();
            try {
                client.sendMessage(message);
                logger.fine("Sent message ("+ message + ") to " + client.getUsername());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Connection closed for " + client.getUsername());
                clientIt.remove();
                client.stop();
            }
        }
        clientMutex.unlock();
    }

    public void disconnectClient(ServerNetworkHandler client) {
        clientMutex.lock();
        if (client != null && clients.contains(client)) {
            clients.remove(client);
            client.stop();
            logger.info("Client " + client.getUsername() + " disconnected");
        }
        clientMutex.unlock();
    }

    private void disconnectAll() {
        clientMutex.lock();
        for (ServerNetworkHandler client : clients) {
            this.disconnectClient(client);
        }
        clientMutex.unlock();
        logger.info("All clients disconnected");
    }

}
