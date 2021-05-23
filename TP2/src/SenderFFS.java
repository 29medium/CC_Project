import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class SenderFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;

    public SenderFFS(DatagramSocket ds, PacketQueue pq, String ipGateway, int portaGateway) {
        this.ds = ds;
        this.pq = pq;
        this.ipGateway    = ipGateway;
        this.portaGateway = portaGateway;
    }

    public void run() {

        // Ao iniciar uma ligação (quando esta thread é criada), o FFs informa o Gateway que se ligou a ele
        try {
            Packet pacoteEstabelcerLigacao = new Packet(6, ipGateway, portaGateway, 1, 1, "FFs pretende estabelecer ligaçao com o Gateway".getBytes(StandardCharsets.UTF_8));
            byte[] pacoteLigacao = pacoteEstabelcerLigacao.packetToBytes();
            DatagramPacket dpLigacao = new DatagramPacket(pacoteLigacao, pacoteLigacao.length, InetAddress.getByName(ipGateway), portaGateway);
            ds.send(dpLigacao);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                // Deviamos trocar o facto de estar vazio para uma condition para nao termos uma espera ativa
                if(!pq.isEmpty()) {
                    Packet p = pq.remove();

                    byte[] buf = p.packetToBytes();
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(ipGateway), portaGateway);

                    ds.send(dp);
                }
            } catch (IOException ignored) {}
        }
    }
}
