import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private final ServerList servers;
    private PacketQueue queue;

    public SenderGateway(DatagramSocket ds, ServerList servers , PacketQueue queue) throws IOException {
        this.ds = ds;
        this.servers = servers;
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();

                byte[] buf = p.packetToBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(p.getIpDestino()), p.getPortaDestino());

                ds.send(dp);
            } catch (IOException | InterruptedException ignored) {}
        }
    }
}
