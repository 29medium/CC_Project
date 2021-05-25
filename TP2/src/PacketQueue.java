import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class PacketQueue {
    private LinkedList<Packet> packets;
    private ReentrantLock lock;
    private Condition con;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        con = lock.newCondition();
    }

    public void add(Packet packet) {
        lock.lock();
        try {
            packets.add(packet);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void addFirst(Packet packet) {
        lock.lock();
        try {
            packets.addFirst(packet);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Packet remove() throws InterruptedException {
        lock.lock();
        try {
            while (packets.isEmpty())
                con.await();

            return packets.isEmpty() ? null : packets.remove();
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

    public void signalCon() {
        lock.lock();
        try {
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
