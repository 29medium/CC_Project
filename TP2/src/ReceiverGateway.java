import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiverGateway implements Runnable {
    private DatagramSocket ds;
    private DataOutputStream out;

    public ReceiverGateway(DatagramSocket ds, DataOutputStream out){
        this.ds = ds;
        this.out = out;
    }

    public void run() {
        try {
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            ds.receive(packet);
            Packet p = new Packet(buf);
            System.out.println(p.toString());
        } catch (Exception ignored) {}
    }
}
