import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ReceiverGateway implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;
    private ServerList servers;

    public ReceiverGateway(DatagramSocket ds, ServerList servers, PacketQueue queue){
        this.ds = ds;
        this.queue = queue;
        this.servers = servers;
    }

    public void run() {
        while(true) {
            try {
                byte[] arr = new byte[Packet.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                byte[] conteudoPacote = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());
                Packet p = new Packet(conteudoPacote); // Cria um pacote com as merdas recebidas do gateway

                Packet newp;

                System.out.println(p.toString());

                switch (p.getTipo()) {
                    case 6:
                        newp = packetType6(p);
                        queue.add(newp);
                        break;
                    default:
                        break;
                }

                System.out.println(servers.toString());

                // Se tiver alguma coisa para reenviar para o FFS adiciona a queue

                // Verificar o tipo de pacote de regresso e o utilizador a quem este se refere
                // Dar output ao utilizador através do map guardado no userset

            } catch (Exception ignored) {
            }
        }
    }

    public Packet packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        return new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), Packet.getIdTransferenciaCounter(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
    }
}
