import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class PacketQueue {
    private LinkedList<Packet> packets;
    private ReentrantLock lock;
    private Condition con;
    private ReentrantLock lock2;
    private Condition notEmpty;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        lock2 = new ReentrantLock();
        con = lock.newCondition();
        notEmpty = lock2.newCondition();
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

            return packets.remove();
        } finally {
            lock.unlock();
        }
    }

    public void checkEmpty() {
        lock2.lock();
        try {
            if (packets.isEmpty())
                notEmpty.signalAll();
        } finally {
            lock2.unlock();
        }
    }

    public void notEmpty() throws InterruptedException {
        lock2.lock();
        try {
            while(!packets.isEmpty())
                notEmpty.await();
        } finally {
            lock2.unlock();
        }
    }
}
