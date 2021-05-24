import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class PacketQueue {
    private LinkedList<Packet> packets;
    private ReentrantLock lock;
    private Condition con;
    private Condition notEmpty;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        con = lock.newCondition();
        notEmpty = lock.newCondition();
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

            Packet p = packets.remove();

            if(packets.isEmpty())
                notEmpty.signalAll();

            return p;
        } finally {
            lock.unlock();
        }
    }

    public void notEmpty() throws InterruptedException {
        lock.lock();
        try {
            while(!packets.isEmpty())
                notEmpty.await();
        } finally {
            lock.unlock();
        }
    }
}
