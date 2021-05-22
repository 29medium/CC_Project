package FFServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class SenderFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;

    public SenderFFS(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    public void run() {
        while(true) {
            try {
                DatagramPacket dp = pq.remove();
                ds.send(dp);
            } catch (IOException ignored) {}
        }
    }
}
