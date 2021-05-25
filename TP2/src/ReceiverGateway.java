import java.io.*;
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
                Packet p = new Packet(conteudoPacote, InetAddress.getLocalHost().getHostAddress()); // Cria um pacote com as merdas recebidas do gateway

                switch (p.getTipo()) {
                    case 2:
                        packetType2(p);
                        break;
                    case 3:
                        packetType3(p);
                        break;
                    case 5:
                        packetType5(p);
                        break;
                    case 6:
                        packetType6(p);
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
            } catch (Exception ignored) {
            }
        }
    }

    public void packetType2(Packet p) throws UnknownHostException {
        String[] tokens = p.getDataString().split("#SIZE#");

        int chuncks = Integer.parseInt(tokens[1]) / Packet.MAX_SIZE_DATA;

        if(Integer.parseInt(tokens[1]) % Packet.MAX_SIZE_DATA > 0)
            chuncks++;

        users.setChunks(p.getIdUser(), chuncks);

        users.addRemainingFragmentUser(p.getIdUser(), chuncks);

        // Criar um Thread para enviar os pedidos de Fragmentos
        Thread fragmentsRequester = new Thread(new FragmentsRequester(queue, servers, users, p.getIdUser(), tokens[0]));
        fragmentsRequester.start();

        // Criar um Thread para verificar se ja recebeu os pacotes todos
        Thread userFileSender = new Thread(new UserFileSender(users, p.getIdUser()));
        userFileSender.start();
    }

    public void packetType3(Packet p) throws IOException {
        Socket s = users.getSocket(p.getIdUser());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.out.println("Ficheiro " + p.getDataString() + " não existe\n");
        out.write("File not found\n");
        out.flush();

        out.close();
        s.close();
    }

    public void packetType5(Packet p) {
        users.addFragment(p.getIdUser(), p);
    }

    public void packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        System.out.println("FFS " + p.getIpOrigem() + " pediu para se conectar\n");

        Packet pnew = new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    public void packetType7(Packet p) throws UnknownHostException {
        servers.removeServer(p.getIpOrigem());

        System.out.println("FFS " + p.getIpOrigem() + " pediu para se desconectar\n");

        Packet pnew = new Packet(10, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Coneccao encerrada".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    public void packetType9(Packet p) {
        servers.updateTime(p.getIpOrigem());

        System.out.println("Beacon do FFS " + p.getIpOrigem() + " recebido\n");
    }
}
