import java.net.Socket;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

class ServerData {
    private int port;
    private String ip;
    private LocalTime lastUpdate;

    public ServerData(int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
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

    public LocalTime getLastUpdate() {
        return this.lastUpdate;
    }

    public void updateTime() {
        this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
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
                if(sa.getLastUpdate().until(LocalTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS)>15)
                    forRemove.add(sa.getIp());

            for(String ip : forRemove) {
                removeServer(ip);
                System.out.println("Servidor removido por idle: " + ip);
            }
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        return "ServerList{" + "servers=" + servers.toString() + '}';
    }
}
