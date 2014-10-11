package incognitects.incognitooth;

import java.util.ArrayList;
import java.util.List;

public class Packet {

    private final String recipient;
    private final String payload;

    public List<String> deliveredTo = new ArrayList<String>();

    // Recipient - Public Key of the intended recipient
    // Payload - Some kind of message
    public Packet(String recipient, String payload) {
        this.recipient = recipient;
        this.payload = payload;
    }

    public String getPayload() {
        return this.payload;
    }
}
