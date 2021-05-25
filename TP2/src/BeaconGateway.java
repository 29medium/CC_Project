import java.net.DatagramSocket;

/**
 * Classe que implementa o BeaconGateway, onde verifica Beacons de FFSs e remove idle Servers
 */
public class BeaconGateway implements Runnable{
    private ServerList servers;
    public static final long SLEEP_TIME = 15000;

    /**
     * Construtor do BeaconGateway
     * @param servers   Lista de servidores (FFS) e dos seus dados
     */
    public BeaconGateway(ServerList servers) {
        this.servers = servers;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        while(true) {
            try {
                Thread.sleep(SLEEP_TIME);
                servers.removeIdle();
            } catch (InterruptedException ignored) { }
        }
    }
}
