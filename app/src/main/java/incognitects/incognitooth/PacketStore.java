package incognitects.incognitooth;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PacketStore {

    private final String PACKETS = "packets";

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEditor;
    private static EncryptUtil encryptor;
    public ArrayList<Packet> packets;

    public PacketStore (SharedPreferences prefs, EncryptUtil encryptUtil) {
        this.prefs = prefs;
        this.prefEditor = this.prefs.edit();
        this.encryptor = encryptUtil;

        this.packets = new ArrayList<Packet>();
    }

    public void store() {
        Log.d("PACKET STORE", "Storing " + packets.size() + " packets.");
        try {
            prefEditor.putString(PACKETS, ObjectSerializer.serialize(packets));
        } catch (Exception ex) {
            Log.e("PACKET STORE", ex.getMessage());
        }
        boolean success = prefEditor.commit();
    }

    public void load() {
        try {
            packets = (ArrayList<Packet>) ObjectSerializer.deserialize(prefs.getString(PACKETS, ObjectSerializer.serialize(new ArrayList<Packet>())));
            Log.d("PACKET STORE", "Loaded  from " + prefs.getString(PACKETS, "Fuck"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encrypt(Packet p) {
        if (p.isEncrypted) {
            return;
        }

        PublicKey recipientKey = encryptor.getPublicKey();//encryptor.getPublicKey(p.getRecipient());
        if (recipientKey != null) {
            //String encryptedPayload = new String(encryptor.encrypt(p.getPayload(), recipientKey));
            //p.setPayload(encryptedPayload);
            p.isEncrypted = true;
        } else {
            Log.e("[PACKET STORE]", "PublicKey for "+p.getRecipient()+" not known!");
        }
    }

    public void decrypt(Packet p) {
        if (!p.isEncrypted) {
            return;
        }

        PrivateKey myKey = encryptor.getPrivateKey();
        if (myKey != null) {
            //String decryptedPayload = new String(encryptor.decrypt(p.getPayload().getBytes(), myKey));
            //p.setPayload(decryptedPayload);
            p.isEncrypted = false;
        } else {
            Log.e("PACKET STORE", "PrivateKey missing.");
        }
    }

    public void add(Packet p) {
        Log.d("[PACKET STORE]", "Ingesting new packet for " + p.getRecipient());
        this.packets.add(p);
    }

}
