import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Classe que implementa o RecieverGateway, onde são recebidos pacotes dos diferentes servidores FFS
 */
public class ReceiverGateway implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;
    private ServerList servers;
    private UserList users;

    /**
     * Construtor do RecieverGateway
     * @param ds        DatagramSocket onde pacotes são recebidos dos servidores
     * @param servers   Classe onde constam informações sobre os vários servidores FFS conectados
     * @param queue     Queue de pacotes onde vamos inrtoduzir os pacotes a serem enviados
     * @param users     Classe onde constam informações sobre os vários Users que efeturam pedidos ao Gateway
     */
    public ReceiverGateway(DatagramSocket ds, ServerList servers, PacketQueue queue, UserList users){
        this.ds = ds;
        this.queue = queue;
        this.servers = servers;
        this.users = users;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        while(true) {
            try {
                byte[] arr = new byte[Packet.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                byte[] conteudoPacote = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());
                Packet p = new Packet(conteudoPacote); // Cria um pacote com as merdas recebidas do gateway

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


    /**
     * Método que processa o pacote do tipo 2
     * @param p                         Pacote do tipo 2
     * @throws UnknownHostException
     */
    public void packetType2(Packet p) throws UnknownHostException {
        String[] tokens = p.getDataString().split("#SIZE#");

        int chuncks = Integer.parseInt(tokens[1]) / Packet.MAX_SIZE_DATA;

        if(Integer.parseInt(tokens[1]) % Packet.MAX_SIZE_DATA > 0)
            chuncks++;

        users.setChunks(p.getIdUser(), chuncks);

        users.addRemainingFragmentUser(p.getIdUser(), chuncks);

        Thread fragmentsRequester = new Thread(new FragmentsRequester(queue, servers, users, p.getIdUser(), tokens[0]));
        fragmentsRequester.start();
    }

    /**
     * Método que processa o pacote do tipo 3
     * @param p                         Pacote do tipo 3
     * @throws UnknownHostException
     */
    public void packetType3(Packet p) throws IOException {
        Socket s = users.getSocket(p.getIdUser());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.out.println("Ficheiro " + p.getDataString() + " não existe\n");
        out.write("File not found\n");
        out.flush();

        out.close();
        s.close();
    }

    /**
     * Método que processa o pacote do tipo 5
     * @param p                         Pacote do tipo 5
     * @throws UnknownHostException
     */
    public void packetType5(Packet p) {
        users.addFragment(p.getIdUser(), p);
    }

    /**
     * Método que processa o pacote do tipo 6
     * @param p                         Pacote do tipo 6
     * @throws UnknownHostException
     */
    public void packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        System.out.println("FFS " + p.getIpOrigem() + " pediu para se conectar\n");

        Packet pnew = new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    /**
     * Método que processa o pacote do tipo 7
     * @param p                         Pacote do tipo 7
     * @throws UnknownHostException
     */
    public void packetType7(Packet p) throws UnknownHostException {
        servers.removeServer(p.getIpOrigem());

        System.out.println("FFS " + p.getIpOrigem() + " pediu para se desconectar\n");

        Packet pnew = new Packet(10, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Coneccao encerrada".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    /**
     * Método que processa o pacote do tipo 9
     * @param p                         Pacote do tipo 9
     * @throws UnknownHostException
     */
    public void packetType9(Packet p) {
        servers.updateTime(p.getIpOrigem());

        System.out.println("Beacon do FFS " + p.getIpOrigem() + " recebido\n");
    }
}
