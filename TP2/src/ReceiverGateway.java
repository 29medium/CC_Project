import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ReceiverGateway implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;
    private ServerList servers;
    private UserList users;

    public ReceiverGateway(DatagramSocket ds, ServerList servers, PacketQueue queue, UserList users){
        this.ds = ds;
        this.queue = queue;
        this.servers = servers;
        this.users = users;
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
                    case 2:
                        newp = packetType2(p);
                        queue.add(newp);
                        break;
                    case 5:
                        packetType5(p);
                        break;
                    case 6:
                        newp = packetType6(p);
                        queue.add(newp);
                        break;
                    case 7:
                        packetType7(p);
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

    public Packet packetType2(Packet p) throws UnknownHostException {
        return new Packet(4, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), Packet.getIdTransferenciaCounter(), p.getIdUser(), 0, p.getData());
    }

    public void packetType5(Packet p) throws IOException {
        Socket s = users.getSocket(p.getIdUser());
        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.writeUTF("Tenho o ficheiro");
        out.flush();

        out.close();
        s.close();
        users.remove(p.getIdUser());
    }

    public Packet packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        return new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), Packet.getIdTransferenciaCounter(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
    }

    public void packetType7(Packet p) {
        servers.removeServer(p.getIpOrigem());
    }
}
