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

    public String toString() {
        return "port=" + port + ", ip='" + ip + '\'';
    }
}

public class ServerList {
    private List<ServerData> servers;
    private ReentrantLock lock;
    private int nextServer;

    public ServerList() {
        servers = new ArrayList<>();
        lock = new ReentrantLock();
        nextServer = 0;
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
            if(nextServer==servers.size())
                nextServer = 0;
        } finally {
            lock.unlock();
        }
    }

    public ServerData getServer() {
        lock.lock();
        try {
            ServerData res = servers.get(this.nextServer);
            nextServer = (nextServer + 1) % servers.size();
            return res;
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public String toString() {
        return "ServerList{" + "servers=" + servers.toString() + '}';
    }
}
