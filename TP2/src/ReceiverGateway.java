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
                        packetType2(p);
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
                    case 9:
                        packetType9(p);
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

    public void packetType2(Packet p) throws UnknownHostException {
        String[] tokens = p.getDataString().split("#SIZE#");

        int chuncks = Integer.parseInt(tokens[1]) / Packet.MAX_SIZE_DATA;

        if(Integer.parseInt(tokens[1]) / Packet.MAX_SIZE_DATA > 0)
            chuncks++;

        users.setChunks(p.getIdUser(), chuncks);

        Packet pnew;
        for(int i=0; i<chuncks; i++) {
            ServerData sd = servers.getServer();
            pnew = new Packet(4, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), p.getIdUser(), i, tokens[0].getBytes());
            queue.add(pnew);
        }

        // Criar um thread para verificar se ja recebeu os pacotes todos
        Thread userFileSender = new Thread(new UserFileSender(users, p.getIdUser()));
        userFileSender.start();
    }

    public void packetType5(Packet p) throws IOException {
        users.addFragment(p.getIdUser(), p);
    }

    public Packet packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        return new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
    }

    public void packetType7(Packet p) {
        servers.removeServer(p.getIpOrigem());
    }

    public void packetType9(Packet p) {
        servers.updateTime(p.getIpOrigem());
    }
}
