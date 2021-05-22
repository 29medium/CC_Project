import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private BufferedReader in;

    public SenderGateway(DatagramSocket ds, BufferedReader in) {
        this.ds = ds;
        this.in = in;
    }

    public void run() {
        try {
            Packet p = new Packet(1, "abc", 88, 1, 1, "Cona".getBytes(StandardCharsets.UTF_8));
            byte[] buf = p.packetToBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), 88);

            ds.send(packet);
        } catch (IOException ignored) {}
    }
}
