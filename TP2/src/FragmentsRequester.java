import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
    private String payload;

    /**
     * Construtor do FragmentRequester
     * @param queue     Queue onde vamos adicionar os pedidos de Packets para enviar para os vários FFSs
     * @param servers   Classe onde constam informações sobre os vários servidores FFS conectados
     * @param users     Classe onde constam informações sobre os vários Users que efeturam pedidos ao Gateway
     * @param idUser    id do User que pediu um dado ficheiro (sobre o qual estamos a pedir os chuncks aos FFSs)
     * @param payload   payload a enviar
     */
    public FragmentsRequester(PacketQueue queue, ServerList servers, UserList users, int idUser, String payload) {
        this.queue = queue;
        this.servers = servers;
        this.users = users;
        this.idUser = idUser;
        this.payload = payload;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        try {
            boolean recievedAllPackages = false;

            String[] tokens = payload.split("#SIZE#");
            System.out.println("Chunks para o ficheiro " + tokens[0] + " pedidos aos FFSs\n");

            UserData user = users.getUserData(idUser);

            while (!recievedAllPackages) {
                Packet pnew;
                Set<Integer> remainingFragments = user.getRemaingFragments();

                if (!remainingFragments.isEmpty()) {
                    for (int i : remainingFragments) {
                        ServerData sd = servers.getServer();
                        pnew = new Packet(4, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), idUser, i, payload.getBytes(StandardCharsets.UTF_8));
                        queue.add(pnew);
                    }
                    int sleep_time = ((remainingFragments.size()*Packet.MAX_SIZE_DATA) / 5000000) + 1;
                    Thread.sleep((long) sleep_time * 1000);
                }
                recievedAllPackages = user.noMoreFragments();
            }

            Socket s = user.getSocket();
            BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());

            for(int i=0; i<user.getChucks(); i++) {
                byte[] arr = user.getDataChunk(i);
                out.write(arr, 0, arr.length);
                out.flush();
            }

            System.out.println("Ficheiro enviado ao utilizador\n");

            out.close();
            s.close();

            users.remove(idUser);
        } catch (InterruptedException | IOException ignored) { }
    }
}
