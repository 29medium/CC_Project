import java.net.DatagramSocket;

public class BeaconGateway implements Runnable{
    private DatagramSocket ds;
    private ServerList servers;
    public static final long SLEEP_TIME = 15000;

    public BeaconGateway(DatagramSocket ds, ServerList servers) {
        this.ds = ds;
        this.servers = servers;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(SLEEP_TIME);
                servers.removeIdle();
            } catch (InterruptedException ignored) { }
        }
    }
}
