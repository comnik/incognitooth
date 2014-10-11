package incognitects.incognitooth;

import android.content.SharedPreferences;
import android.util.Log;

import java.security.PublicKey;
import java.util.LinkedList;
import java.util.Queue;

public class PacketStore {

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEditor;
    private static EncryptUtil encryptor;
    public Queue<Packet> packets;

    public PacketStore (SharedPreferences prefs, EncryptUtil encryptUtil) {
        this.prefs = prefs;
        this.prefEditor = this.prefs.edit();
        this.encryptor = encryptUtil;

        this.packets = new LinkedList<Packet>();
    }

    public void add(Packet p) {
        Log.d("[PACKET STORE]", "Ingesting new packet for " + p.getRecipient());
        PublicKey recipientKey = encryptor.getPublicKey();//encryptor.getPublicKey(p.getRecipient());
        if (recipientKey != null) {
            String encryptedPayload = new String(encryptor.encrypt(p.getPayload(), recipientKey));
            p.setPayload(encryptedPayload);
            this.packets.add(p);
        } else {
            Log.e("[PACKET STORE]", "PublicKey for "+p.getRecipient()+" not known!");
        }
    }

}
