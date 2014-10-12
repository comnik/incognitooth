package incognitects.incognitooth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Packet implements Serializable {

    private final String recipient;
    private String payload;
    public boolean isEncrypted;

    public List<String> deliveredTo = new ArrayList<String>();

    public static Packet deSerialize(String msg) {
        String parsedData[] = msg.split("#");
        Packet parsedPacket = null;

        try {
            parsedPacket = new Packet(parsedData[0], parsedData[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return parsedPacket;
    }

    // Recipient - Public Key of the intended recipient
    // Payload - Some kind of message
    public Packet(String recipient, String payload) {
        this.recipient = recipient;
        this.payload = payload;
        this.isEncrypted = false;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public void setPayload(String newPayload) { this.payload = newPayload; }
    public String getPayload() {
        return this.payload;
    }

    public String serialize() {
        return this.recipient+"#"+this.getPayload();
    }
}
