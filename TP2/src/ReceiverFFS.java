import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class ReceiverFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;

    public ReceiverFFS(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    public void run() {
        while (true) {
            try {
                byte[] arr = new byte[1000];
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);
                pq.add(dp);
            } catch (IOException ignored) {}
        }
    }
}
