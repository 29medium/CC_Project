import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class BeaconFFS implements Runnable{
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;
    private final int SLEEP_TIME = 60000;

    public BeaconFFS(PacketQueue pq, String ipGateway, int portaGateway) {
        this.pq = pq;
        this.ipGateway = ipGateway;
        this.portaGateway = portaGateway;
    }

    public void run() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) { }

        while(!FFServer.EXIT) {
            try {
                pq.add(new Packet(9, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, -1, 0, "I'm Alive".getBytes(StandardCharsets.UTF_8)));

                Thread.sleep(SLEEP_TIME);
            } catch (UnknownHostException | InterruptedException e) {}
        }
    }
}
