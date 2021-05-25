import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

class FragmentsRequester implements Runnable {
    private PacketQueue queue;
    private ServerList servers;
    private UserList users;
    private int idUser;
    private int nrChuncks;
    private String filename;

    public FragmentsRequester(PacketQueue queue, ServerList servers, UserList users, int idUser, int nrChuncks, String filename) {
        this.queue = queue;
        this.servers = servers;
        this.users = users;
        this.idUser = idUser;
        this.nrChuncks = nrChuncks;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            boolean recievedAllPackages = false;

            while (!recievedAllPackages) {

                Packet pnew;
                Set<Integer> remainingFragments = users.getUserData(idUser).getRemaingFragments();
		
		if(!remainingFragments.isEmpty()) {
                	for (int i : remainingFragments) {
                    		ServerData sd = servers.getServer();
                    		pnew = new Packet(4, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), idUser, i, filename.getBytes(StandardCharsets.UTF_8));
                    		queue.add(pnew);
                	}
			Thread.sleep(5000);
		}
                recievedAllPackages = users.getUserData(idUser).noMoreFragments();
            }
        } catch (UnknownHostException | InterruptedException | NullPointerException ignored) { }
    }
}

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

                //System.out.println(p.toString());

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

                System.out.println(servers.toString());
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
        Thread fragmentsRequester = new Thread(new FragmentsRequester(queue, servers, users, p.getIdUser(), chuncks, tokens[0]));
        fragmentsRequester.start();

        // Criar um Thread para verificar se ja recebeu os pacotes todos
        Thread userFileSender = new Thread(new UserFileSender(users, p.getIdUser()));
        userFileSender.start();
    }

    public void packetType3(Packet p) throws IOException {
        Socket s = users.getSocket(p.getIdUser());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.out.println("Ficheiro não existe");
        out.write(" ");
        out.flush();

        out.close();
        s.close();
    }

    public void packetType5(Packet p) throws IOException {
        users.addFragment(p.getIdUser(), p);
    }

    public void packetType6(Packet p) throws UnknownHostException {
        if(!servers.isServer(p.getIpOrigem())) {
            servers.addServer(p.getPortaOrigem(), p.getIpOrigem());
        }

        Packet pnew = new Packet(8, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Ligacao Estabelecida".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    public void packetType7(Packet p) throws UnknownHostException {
        servers.removeServer(p.getIpOrigem());

        Packet pnew = new Packet(10, InetAddress.getLocalHost().getHostAddress(), p.getIpOrigem(), 8888, p.getPortaOrigem(), -1, 0, "Coneccao encerrada".getBytes(StandardCharsets.UTF_8));
        queue.add(pnew);
    }

    public void packetType9(Packet p) {
        servers.updateTime(p.getIpOrigem());
    }
}
