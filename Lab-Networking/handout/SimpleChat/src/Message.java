public interface Message {
    public char getType();
    public byte[] getPayload();
    public String decodedPaylod();
    public byte[] createRequest();
}
