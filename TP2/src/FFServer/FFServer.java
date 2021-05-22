package FFServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class FFServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(88);
        PacketQueue pq = new PacketQueue();

        Thread receiver = new Thread(new ReceiverFFS(ds, pq));
        Thread sender = new Thread(new SenderFFS(ds, pq));
    }
}
