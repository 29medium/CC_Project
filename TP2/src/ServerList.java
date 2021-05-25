import java.beans.beancontext.BeanContext;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class ServerData {
    private int port;
    private String ip;
    private LocalTime lastUpdate;
    private ReentrantLock lock;

    public ServerData(int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
        this.lock = new ReentrantLock();
    }

    public String getIp() {
        lock.lock();
        try {
            return this.ip;
        } finally {
            lock.unlock();
        }
    }

    public int getPort() {
        lock.lock();
        try {
            return this.port;
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        lock.lock();
        try {
            return "port=" + port + ", ip='" + ip + '\'';
        } finally {
            lock.unlock();
        }
    }

    public LocalTime getLastUpdate() {
        lock.lock();
        try {
            return this.lastUpdate;
        } finally {
            lock.unlock();
        }
    }

    public void updateTime() {
        lock.lock();
        try {
            this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
        } finally {
            lock.unlock();
        }
    }
}

public class ServerList {
    private Map<String, ServerData> servers;
    private ReentrantLock lock;
    private int nextServer;

    public ServerList() {
        servers = new HashMap<>();
        lock = new ReentrantLock();
        nextServer = 0;
    }

    public boolean isServer(String ip) {
        lock.lock();
        try {
            return servers.containsKey(ip);
        } finally {
            lock.unlock();
        }
    }

    public void addServer(int port, String ip) {
        lock.lock();
        try {
            servers.put(ip, new ServerData(port,ip));
        } finally {
            lock.unlock();
        }
    }

    public void removeServer(String ip) {
        lock.lock();
        try {
            servers.remove(ip);
            if(nextServer==servers.size())
                nextServer = 0;
        } finally {
            lock.unlock();
        }
    }

    public ServerData getServer() {
        lock.lock();
        try {
            if(servers.size()==0)
                return null;

            ServerData res = (ServerData) servers.values().toArray()[nextServer];
            nextServer = (nextServer + 1) % servers.size();
            return res;
        } finally {
            lock.unlock();
        }
    }

    public void updateTime(String ip) {
        lock.lock();
        try {
            servers.get(ip).updateTime();
        } finally {
            lock.unlock();
        }
    }

    public void removeIdle() {
        lock.lock();
        try {
            List <String> forRemove = new ArrayList<>();
            for(ServerData sa : servers.values())
                if(sa.getLastUpdate().until(LocalTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS)>(BeaconGateway.SLEEP_TIME / 1000))
                    forRemove.add(sa.getIp());

            for(String ip : forRemove) {
                removeServer(ip);
                System.out.println("FFS " + ip + " removido por idle\n");
            }
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        lock.lock();
        try {
            return "ServerList{" + "servers=" + servers.toString() + '}';
        } finally {
            lock.unlock();
        }
    }
}
