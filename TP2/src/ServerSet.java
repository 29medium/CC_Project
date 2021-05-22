import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ServerSet {
    private Map<String, Socket> servers;
    private ReentrantLock lock;

    public ServerSet() {
        servers = new HashMap<>();
        lock = new ReentrantLock();
    }

    public boolean isServer(String server) {
        lock.lock();
        try {
            return servers.values().contains(server);
        } finally {
            lock.unlock();
        }
    }

    public void addServer(String server, Socket s) {
        lock.lock();
        try {
            servers.put(server, s);
        } finally {
            lock.unlock();
        }
    }

    public void removeServer(String server) {
        lock.lock();
        try {
            servers.remove(server);
        } finally {
            lock.unlock();
        }
    }
}
