import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class BeaconFFS implements Runnable{
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;
    private final int SLEEP_TIME = 10000;
    private volatile boolean exit;

    public BeaconFFS(PacketQueue pq, String ipGateway, int portaGateway) {
        this.pq = pq;
        this.ipGateway = ipGateway;
        this.portaGateway = portaGateway;
        this.exit = false;
    }

    public void run() {
        while(!exit) {
            try {
                Thread.sleep(SLEEP_TIME);

                pq.addFirst(new Packet(9, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, -1, 0, "I'm Alive".getBytes(StandardCharsets.UTF_8)));
            } catch (UnknownHostException | InterruptedException e) {}
        }
    }

    @Deprecated
    public void stop() {
        exit = true;
    }
}
