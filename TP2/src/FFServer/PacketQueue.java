package FFServer;

import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

class PacketQueue {
    private Queue<DatagramPacket> packets;
    private ReentrantLock lock;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
    }

    public void add(DatagramPacket packet) {
        lock.lock();
        try {
            packets.add(packet);
        } finally {
            lock.unlock();
        }
    }

    public DatagramPacket remove() {
        lock.lock();
        try {
            return packets.remove();
        } finally {
            lock.unlock();
        }
    }
}
