
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

enum State {
    DISCONNECTED, CONNECTED, CONFIRM_REGISTER, REGISTERED, CONFIRM_QUIT
}

public class ClientNetworkHandler extends Observable implements Runnable {
    private Logger logger = Logger.getLogger("client.ClientNetworkHandler");
    private boolean running = true; // flag to stop thread
    private String message = "";    // Never modify yourself! use printMessage
    private String registeredUser = "";
    private State state = State.DISCONNECTED;
    private String serverAddress;
    private int serverPort;
    private SimpleChatProtocol protocol;
    private static final String KEYWORD_REGISTER = ":register";
    private static final String KEYWORD_QUIT = ":quit";

    public ClientNetworkHandler(String serverAddress, int serverPort) {
        this.logger.setLevel(Level.ALL);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void run() {
        printMessage("Commands " + KEYWORD_REGISTER + " <name> | " + KEYWORD_QUIT);
        logger.info("Client Network Handler started");
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            socket.setKeepAlive(true);
            protocol = new SimpleChatProtocol(socket, logger);
            state = State.CONNECTED;
            logger.fine( "Wait for messages");
            while (running) {
                Message message = protocol.listenForMessage();
                dispatchMessage(message);
            }
            protocol.disconnect();
        } catch (UnknownHostException  e) {
            logger.log(Level.SEVERE, "Failed to connect to host "
                + serverAddress + ":" + serverPort +"-", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Communication error: ", e);
        }
        logger.info("Client Network Handler stopped");
    }

    private void dispatchMessage(Message message) {
        char type = message.getType();
        logger.fine("dispatchMessage("+type+", "+ message.decodedPaylod() + ")");
        switch(type) {
            case 'C':  // confirm on client side
                if (state == State.CONFIRM_REGISTER) {
                    state = State.REGISTERED;
                    printMessage("You are successfully registered as " + registeredUser);
                    logger.info("Confirmed registration for " + registeredUser);
                } else if (state == State.CONFIRM_QUIT){
                    printMessage("You are logged out now!");
                    logger.info("Confirmed quit");
                } else {
                    logger.warning("Got unexpected confirm message");
                }
                break;
            case 'M':
                printMessage(message.decodedPaylod());
                break;
            case 'E':
                printMessage("ERROR: " + message.decodedPaylod());
                logger.warning("Error from Server: " + message.decodedPaylod());
                break;
            default:
                logger.warning("Unknown packet type '" + type + "'");
        }
    }

    public void stop() {
        running = false;
    }

    /*
     * This method is called by the GUI to send a message
     */
    public void sendMessage(String text) {
        // Send message to server, if connection is open.
        if (protocol != null) {
            try {
                if (text.toLowerCase().startsWith(KEYWORD_REGISTER)) {
                    registeredUser = text.substring(KEYWORD_REGISTER.length()).trim();
                    if (registeredUser.isEmpty()) {
                        this.printMessage("Username must not be empty");
                    } else {
                        protocol.sendCommand("REGISTER " + registeredUser);
                        logger.fine("Sent registration request for " + registeredUser);
                        state = State.CONFIRM_REGISTER;
                    }
                } else if (text.toLowerCase().startsWith(KEYWORD_QUIT)) {
                    protocol.sendCommand("QUIT");
                    logger.fine("Sent quit request");
                    state = State.CONFIRM_QUIT;
                } else {
                    protocol.sendMessage(text);
                    logger.fine("Sent message: " + text);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Sending Message failed!", e);
            }
        } else {
            logger.info("Connection closed");
        }
    }

    /*
     * Call this to print a received message in the GUI text pane
     */
    private synchronized void printMessage(String message) {
        this.message = message;
        setChanged();
        notifyObservers();
    }

    /*
     * This method is called by the GUI-Thread
     */
    public synchronized String getMessage() {
        return message;
    }
}
