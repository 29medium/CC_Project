import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

class PacketQueue {
    private Queue<Packet> packets;
    private ReentrantLock lock;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
    }

    public void add(Packet packet) {
        lock.lock();
        try {
            packets.add(packet);
        } finally {
            lock.unlock();
        }
    }

    public Packet remove() {
        lock.lock();
        try {
            return packets.remove();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return packets.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
