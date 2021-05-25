import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class UserData {
    private int user_id;
    private Socket s;
    private int chuncks;
    private Map<Integer, Packet> fragments;
    private ReentrantLock lock;
    private Condition isFull;

    public UserData(int user_id, Socket s) {
        this.user_id = user_id;
        this.s = s;
        this.chuncks = 0;
        this.fragments = new HashMap<>();
        this.lock = new ReentrantLock();
        this.isFull = lock.newCondition();
    }

    public int getUser_id() {
        lock.lock();
        try {
            return user_id;
        } finally {
            lock.unlock();
        }
    }

    public Socket getSocket() {
        lock.lock();
        try {
            return s;
        } finally {
            lock.unlock();
        }
    }

    public void setChunks(int chunks) {
        lock.lock();
        try {
            this.chuncks = chunks;
        } finally {
            lock.unlock();
        }
    }

    public int getChucks() {
        lock.lock();
        try {
            return chuncks;
        } finally {
            lock.unlock();
        }
    }

    public void addFragment(Packet p) {
        lock.lock();
        try {
            fragments.put(p.getChucnkTransferencia(), p);

                isFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void isFull() {
        lock.lock();
        try {
            try {
                while (fragments.size() != chuncks)
                    isFull.await();
            } catch (InterruptedException e) {
            }
        } finally {
            lock.unlock();
        }
    }

    public byte[] getDataChunk(int chunk) {
        lock.lock();
        try {
            return fragments.get(chunk).getData();
        } finally {
            lock.unlock();
        }
    }
}

public class UserList {
    private Map<Integer, UserData> users;
    private ReentrantLock lock;

    public UserList() {
        this.users = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public Socket getSocket(int i) {
        lock.lock();
        try {
            return users.get(i).getSocket();
        } finally {
            lock.unlock();
        }
    }

    public void addSocket(int i, Socket s) {
        lock.lock();
        try {
            users.put(i, new UserData(i, s));
        } finally {
            lock.unlock();
        }
    }

    public void remove(int i) {
        lock.lock();
        try {
            users.remove(i);
        } finally {
            lock.unlock();
        }
    }

    public void setChunks(int i, int chunks) {
        lock.lock();
        try {
            users.get(i).setChunks(chunks);
        } finally {
            lock.unlock();
        }
    }

    public void addFragment(int i, Packet p) {
        lock.lock();
        try {
            users.get(i).addFragment(p);
        } finally {
            lock.unlock();
        }
    }

    public UserData getUserData(int i) {
        lock.lock();
        try {
            return users.get(i);
        } finally {
            lock.unlock();
        }
    }
}
