import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiverGateway implements Runnable {
    private DatagramSocket ds;

    public ReceiverGateway(DatagramSocket ds){
        this.ds = ds;
    }

    public void run() {
        try {
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            ds.receive(packet);
            Packet p = new Packet(buf);

            // System.out.println(p.toString());

            // Verificar o tipo de pacote de regresso e o utilizador a quem este se refere
            // Dar output ao utilizador através do map guardado no userset
        } catch (Exception ignored) {}
    }
}
