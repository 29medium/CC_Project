import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Classe que implementa o Sender do FFS
 */
class SenderFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;

    /**
     * Construtor da classe SenderFFS
     *
     * @param ds    DatagramSocket utilizado para o envio dos pacotes
     * @param pq    PacketQueue com os pacotes a enviar
     */
    public SenderFFS(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        while(!FFServer.EXIT || !pq.isEmpty()) {
            try {
                Packet p = pq.remove();

                if(!FFServer.EXIT) {
                    byte[] buf = p.packetToBytes();
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(p.getIpDestino()), p.getPortaDestino());

                    ds.send(dp);
                }
            } catch (IOException | InterruptedException ignored) {}
        }
    }
}
