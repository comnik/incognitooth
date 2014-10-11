package incognitects.incognitooth;

import java.util.LinkedList;
import java.util.Queue;

public class PacketStore {

    public Queue<Packet> packets;

    public PacketStore () {
        this.packets = new LinkedList<Packet>();
    }

}
