import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SimpleChatProtocol {

    public static final byte HEADER_SEPARATOR = '@';
    public static final int HEADER_SEPARATOR_COUNT = 2;
    public static final int HEADER_MAX_SIZE = 256;
    public static final int DEFAULT_PORT = 22243;

    private Logger logger;
    private Socket socket;

    public SimpleChatProtocol(Socket socket, Logger logger) {
        this.logger = logger;
        this.socket = socket;
    }

    public Message listenForMessage() throws IOException {
        Message message = null;
        ByteBuffer data = ByteBuffer.allocate(HEADER_MAX_SIZE);
        int separatorCount = 0;
        InputStream in = socket.getInputStream();
        logger.fine("Wait for Message");
        while (message == null && socket.isConnected()) {
            if (!data.hasRemaining()) {
                throw new IOException("Header is to long! (>"
                    + HEADER_MAX_SIZE + ")");
            }
            byte singleByte;
            if ((singleByte = (byte) in.read()) != -1) {
                data.put(singleByte);
                logger.finest("read = " + (char) singleByte);
                if (HEADER_SEPARATOR == singleByte
                    && HEADER_SEPARATOR_COUNT == ++separatorCount) {
                    logger.finest("data = " + new String(data.array()));
                    char command = parseCommand(data);
                    logger.finer("command = " + command);
                    int payloadLength = parsePayloadLength(data);
                    logger.finer("payloadLength = " + payloadLength);
                    if (payloadLength > 0) {
                        byte[] payload = new byte[payloadLength];
                        int totalBytesRcvd = 0;
                        int bytesRcvd;
                        while (totalBytesRcvd < payloadLength) {
                            if ((bytesRcvd = in.read(payload, totalBytesRcvd,
                                payloadLength - totalBytesRcvd)) == -1) {
                                throw new SocketException(
                                    "Connection closed prematurely");
                            }
                            totalBytesRcvd += bytesRcvd;
                        }
                        message = new SimpleChatMessage(command, payload);
                    } else {
                        message = new SimpleChatMessage(command, (byte[]) null);
                    }
                    data.clear();
                    separatorCount = 0;
                }
            }
        }
        return message;
    }

    private char parseCommand(ByteBuffer data) {
        return (char) (data.get(0));
    }

    private int parsePayloadLength(ByteBuffer data) throws IOException {
        int payloadLength = 0;
        int lengthStringLength = data.position() - 3;
        if (lengthStringLength > 0 ) {
            String dataString = new String(data.array());
            String lengthString =
                dataString.substring(1, 1 + lengthStringLength);
            try {
                payloadLength = Integer.parseInt(lengthString);
            } catch (NumberFormatException e) {
                throw new IOException("Error in content length header: "+ lengthString);
            }
        }
        return payloadLength;
    }

    public void sendError(String message) throws IOException {
        try {
            sendMessage(new SimpleChatMessage('E', message));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send error: " + message);
            throw e;
        }
    }

    public void sendConfirm() throws IOException{
        try {
            sendMessage(new SimpleChatMessage('C', (byte[])null));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send confirm");
            throw e;
        }
    }

    public void sendCommand(String command) throws IOException{
        try {
            sendMessage(new SimpleChatMessage('C', command));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send command: " + command);
            throw e;
        }
    }

    public void sendMessage(String text) throws IOException {
        try {
            sendMessage(new SimpleChatMessage('M', text));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send message");
            throw e;
        }
    }

    private synchronized void sendMessage(Message message)
        throws IOException, SocketException
    {
        // Send message to server, if connection is open.
        if (socket != null && socket.isConnected()) {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(message.createRequest());
                out.flush();
                logger.finer("Sent Message (" + message + ")");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Send Failed: ");
                throw e;
            }
        } else {
            logger.log(Level.SEVERE, "Connection closed");
            throw new SocketException("Connection closed");
        }
    }

    public void disconnect() throws IOException{
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Disconnect failed");
                throw e;
            }
        }
    }



    class SimpleChatMessage implements Message{
        private char type;
        private byte[] payload;

        public SimpleChatMessage(char type, byte[] payload) {
            this.type = type;
            this.payload = (payload != null) ? payload : new byte[0];
        }

        public SimpleChatMessage (char type, String text) {
            this.type = type;
            try {
                this.payload = (text != null)? text.getBytes("utf-8") : new byte[0];
            } catch (UnsupportedEncodingException e) {
                logger.log(Level.WARNING, "UTF-8 encoding failed", e);
                this.payload = text.getBytes();
            }
        }

        public char getType() {
            return type;
        }

        public byte[] getPayload() {
            return payload;
        }

        public String decodedPaylod() {
            String message = null;
            if (payload != null) {
                try {
                    message = new String(payload, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.log(Level.WARNING, "UTF-8 decoding failed", e);
                    message = new String(payload);
                }
            }
            return message;
        }

        public byte[] createRequest() {
            byte[] length = ("" + payload.length).getBytes();
            ByteBuffer requestBuffer = ByteBuffer.allocate(1 + length.length
                + HEADER_SEPARATOR_COUNT + payload.length);
            requestBuffer.put((byte) type).put(length).put(HEADER_SEPARATOR)
                .put(HEADER_SEPARATOR).put(payload);
            return requestBuffer.array();
        }

        public String toString() {
            return getType() + " : " + decodedPaylod() +
                " [length = " + this.getPayload().length + "]";
        }
    }
}
