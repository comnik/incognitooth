package incognitects.incognitooth;

import android.content.SharedPreferences;

import java.util.LinkedList;
import java.util.Queue;

public class PacketStore {

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEditor;
    public Queue<Packet> packets;

    public PacketStore (SharedPreferences prefs) {
        this.prefs = prefs;
        this.prefEditor = this.prefs.edit();

        this.packets = new LinkedList<Packet>();
    }

}
