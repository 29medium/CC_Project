import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class FragmentsRequester implements Runnable {
    private PacketQueue queue;
    private ServerList servers;
    private UserList users;
    private int idUser;
    private String filename;

    public FragmentsRequester(PacketQueue queue, ServerList servers, UserList users, int idUser, String filename) {
        this.queue = queue;
        this.servers = servers;
        this.users = users;
        this.idUser = idUser;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            boolean recievedAllPackages = false;

            System.out.println("Chunks para o ficheiro " + filename + " pedidos ao FFS\n");

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
