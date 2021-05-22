import java.io.IOException;
import java.net.DatagramSocket;

public class FFServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(88);
        PacketQueue pq = new PacketQueue();

        Thread receiver = new Thread(new ReceiverFFS(ds, pq));
        Thread sender = new Thread(new SenderFFS(ds, pq));

        receiver.start();
        sender.start();
    }
}
