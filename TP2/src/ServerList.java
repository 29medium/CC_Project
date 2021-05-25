import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que posusi informação sobre um dado Servidor
 */
class ServerData {
    private int port;
    private String ip;
    private LocalTime lastUpdate;
    private ReentrantLock lock;

    /**
     * Construtor do ServerData
     * @param port  Porta do Servidor
     * @param ip    Ip do Servidor
     */
    public ServerData(int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
        this.lock = new ReentrantLock();
    }

    /**
     * Função que retorna Ip do servidor
     * @return  Ip do Servidor
     */
    public String getIp() {
        lock.lock();
        try {
            return this.ip;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que retorna porta do Servidor
     * @return  Porta do Servidor
     */
    public int getPort() {
        lock.lock();
        try {
            return this.port;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que transforma a classe numa String
     * @return  String final da Classe transformada
     */
    public String toString() {
        lock.lock();
        try {
            return "port=" + port + ", ip='" + ip + '\'';
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que verifica o ultimo update de tempo de Beacon enviado
     * @return  LocalTime com tempo do último Beacon enviado
     */
    public LocalTime getLastUpdate() {
        lock.lock();
        try {
            return this.lastUpdate;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que dá update do tempo do último Beacon do servidor enviado
     */
    public void updateTime() {
        lock.lock();
        try {
            this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
        } finally {
            lock.unlock();
        }
    }
}




/**
 * Classe que possui informações sobre todos os sevidores
 */
public class ServerList {
    private Map<String, ServerData> servers;
    private ReentrantLock lock;
    private int nextServer;

    /**
     * Construtor da ServerList
     */
    public ServerList() {
        servers = new HashMap<>();
        lock = new ReentrantLock();
        nextServer = 0;
    }

    /**
     * Função que verifica a existência de um servidor dado um ip
     * @param ip    String que contém ip do Servidor do qual verificar existência
     * @return      Booleano que indica se Servidor existe (está conectado) ou não
     */
    public boolean isServer(String ip) {
        lock.lock();
        try {
            return servers.containsKey(ip);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que adiciona novo Servidor
     * @param port  Porta do servidor a adicionar
     * @param ip    Ip do servidor a adicionar
     */
    public void addServer(int port, String ip) {
        lock.lock();
        try {
            servers.put(ip, new ServerData(port,ip));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que remove Servidor
     * @param ip    ip do seridor a remover
     */
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

    /**
     * Função que dá return aos dados de um Servidor escolhido de forma cíclica de modo a dar balance à utilização de todos os Utilizadores
     * @return  Dados sobre o servidor retornado
     */
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

    /**
     * Função que dado um Ip de um Servidor dá update ao seu último tempo em que enviou um beacon
     * @param ip    Ip do Servidor que pretendemos dar update ao tempo do ultimo beacon
     */
    public void updateTime(String ip) {
        lock.lock();
        try {
            servers.get(ip).updateTime();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que remove os Servidores que estejam idle
     */
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

    /**
     * Função que transforma a classe numa String
     * @return  String final da Classe transformada
     */
    public String toString() {
        lock.lock();
        try {
            return "ServerList{" + "servers=" + servers.toString() + '}';
        } finally {
            lock.unlock();
        }
    }
}
