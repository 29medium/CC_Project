import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Função que implementa o SenderGateway, onde são enviados os pacotes que constam na queue de Pacotes a enviar
 */
class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;

    /**
     * Socket utilizado para enviar Packets
     * @param ds        Datagram Socket para onde pacotes vão ser enviados
     * @param queue     Queue de pacotes que necessitam de ser enviados
     */
    public SenderGateway(DatagramSocket ds ,PacketQueue queue) {
        this.ds = ds;
        this.queue = queue;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();

                byte[] buf = p.packetToBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(p.getIpDestino()), p.getPortaDestino());

                ds.send(dp);
            } catch (IOException | InterruptedException ignored) {}
        }
    }
}
