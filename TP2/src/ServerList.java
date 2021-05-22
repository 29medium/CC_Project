import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class ServerData {
    private int port;
    private String ip;

    public ServerData(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }
}

public class ServerList {
    private List<ServerData> servers;
    private ReentrantLock lock;

    public ServerList() {
        servers = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public boolean isServer(String ip) {
        lock.lock();
        try {
            for(ServerData sd : servers)
                if(sd.getIp().equals(ip))
                    return true;

            return false;
        } finally {
            lock.unlock();
        }
    }

    public void addServer(int port, String ip) {
        lock.lock();
        try {
            servers.add(new ServerData(port,ip));
        } finally {
            lock.unlock();
        }
    }

    public void removeServer(String ip) {
        lock.lock();
        try {
            servers.removeIf(sd -> sd.getIp().equals(ip));
        } finally {
            lock.unlock();
        }
    }
}
