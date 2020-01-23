import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerNetworkHandler implements Runnable {
    private static int clientNumber = 0;
    private int clientId = clientNumber++;
    private Logger logger = Logger.getLogger("server.ServerNetworkHandler");
    private Server server;
    private SimpleChatProtocol protocol;
    private String username;
    private boolean registered;
    private volatile boolean running;

    public ServerNetworkHandler(Socket socket, Server server) {
        this.logger.setLevel(Level.ALL);
        this.username = "Anonymous-" + String.valueOf(clientId);
        this.registered = false;
        this.server = server;
        this.running = true;
        this.protocol = new SimpleChatProtocol(socket, logger);
    }

    @Override
    public void run() {
        try {
            logger.info("Listen for Messages");
            while (running) {
                Message message = protocol.listenForMessage();
                dispatchMessage(message);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Communication error: ", e);
        }

    }

    private void dispatchMessage(Message message) {
        char type = message.getType();
        logger.fine("dispatchMessage(" + message+ ")");
        try {
            switch (type) {
                case 'C':
                    if (message.getPayload() == null) {
                        logger.warning("No command in payload");
                        protocol.sendError("No command in payload");
                    } else {
                        String command = message.decodedPaylod();
                        if (command.startsWith("REGISTER")) {
                            String user =
                                command.substring("REGISTER".length()).trim();
                            if (server.existUsername(user)) {
                                logger.info("Name " + user + " already taken");
                                protocol.sendError("Name already taken");
                            } else {
                                username = user;
                                registered = true;
                                logger.info("User " + user + " registered");
                                protocol.sendConfirm();
                            }
                        } else if (command.startsWith("QUIT")) {
                            if (registered) {
                                registered = false;
                                logger.info("User " + username + " quit");
                                username = "Anonymous-" + String.valueOf(clientId);
                                protocol.sendConfirm();
                                server.disconnectClient(this);
                            } else {
                                logger.info("User not registered");
                                protocol.sendError("Not registered!");
                            }
                        }
                    }
                    break;
                case 'M':
                    server.sendToAll(username + " : " + message.decodedPaylod());
                    break;
                case 'E':
                    String error = message.decodedPaylod();
                    logger.info("Received ERROR from client (" + username + "): "
                        + error);
                    break;
                default:
                    protocol.sendError("Unknown packet type: " + type);
                    logger.warning("Unknown packet type '" + type + "'");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Sending response failed!", e);
        }
    }

    public void sendMessage(String message) throws IOException {
        protocol.sendMessage(message);
    }


    public void stop() {
        try {
            protocol.disconnect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close connection");
        }
        running = false;
    }

    public String getUsername() {
        return username;
    }
}
