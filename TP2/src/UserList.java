import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class UserList {
    private Map<Integer, Socket> users;
    private ReentrantLock lock;

    public UserList() {
        this.users = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public Socket getSocket(int i) {
        lock.lock();
        try {
            return users.get(i);
        } finally {
            lock.unlock();
        }
    }

    public void addSocket(int i, Socket s) {
        lock.lock();
        try {
            users.put(i, s);
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
}
