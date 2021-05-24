import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class SenderFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;
    private volatile boolean exit;

    public SenderFFS(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
        this.exit = false;
    }

    public void run() {
        while(!exit) {
            try {
                Packet p = pq.remove();

                byte[] buf = p.packetToBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(p.getIpDestino()), p.getPortaDestino());

                ds.send(dp);
            } catch (IOException ignored) {}
        }
    }

    public void stop() {
        exit = true;
    }
}
