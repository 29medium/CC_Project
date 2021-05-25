import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;

    public SenderGateway(DatagramSocket ds ,PacketQueue queue) {
        this.ds = ds;
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();
                InetAddress ipDestino = InetAddress.getByName(p.getIpDestino());
                int portaDestino = p.getPortaDestino();

                byte[] buf = p.packetToBytes(p.getIpDestino());
                DatagramPacket dp = new DatagramPacket(buf, buf.length, ipDestino, portaDestino);

                ds.send(dp);
            } catch (IOException | InterruptedException ignored) {}
        }
    }
}
