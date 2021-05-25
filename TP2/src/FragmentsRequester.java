import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Classe que implementa o FragmentsRequester, que vai enviar pedidos de chunks de certos ficheiros aos vários FFSs
 */
public class FragmentsRequester implements Runnable {
    private PacketQueue queue;
    private ServerList servers;
    private UserList users;
    private int idUser;
    private String filename;

    /**
     * Construtor do FragmentRequester
     * @param queue     Queue onde vamos adicionar os pedidos de Packets para enviar para os vários FFSs
     * @param servers   Classe onde constam informações sobre os vários servidores FFS conectados
     * @param users     Classe onde constam informações sobre os vários Users que efeturam pedidos ao Gateway
     * @param idUser    id do User que pediu um dado ficheiro (sobre o qual estamos a pedir os chuncks aos FFSs)
     * @param filename  nome do ficheiro pedido pelo User
     */
    public FragmentsRequester(PacketQueue queue, ServerList servers, UserList users, int idUser, String filename) {
        this.queue = queue;
        this.servers = servers;
        this.users = users;
        this.idUser = idUser;
        this.filename = filename;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        try {
            boolean recievedAllPackages = false;

            System.out.println("Chunks para o ficheiro " + filename + " pedidos aos FFSs\n");

            while (!recievedAllPackages) {
                Packet pnew;
                Set<Integer> remainingFragments = users.getUserData(idUser).getRemaingFragments();

                if (!remainingFragments.isEmpty()) {
                    for (int i : remainingFragments) {
                        ServerData sd = servers.getServer();
                        pnew = new Packet(4, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), idUser, i, filename.getBytes(StandardCharsets.UTF_8));
                        queue.add(pnew);
                    }
                    Thread.sleep(1000);
                }
                if(users.hasUser(idUser))
                    recievedAllPackages = users.getUserData(idUser).noMoreFragments();
                else
                    recievedAllPackages = true;
            }
        } catch (UnknownHostException | InterruptedException ignored) {
        }
    }
}
