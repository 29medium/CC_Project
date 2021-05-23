import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
                // Deviamos trocar o facto de estar vazio para uma condition para nao termos uma espera ativa
                if(!pq.isEmpty()) {
                    Packet p = pq.remove();

                    byte[] buf = p.packetToBytes();
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("10.1.1.1"), 8888);

                    ds.send(dp);
                }
            } catch (IOException ignored) {}
        }
    }
}
