import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class KeepAlive implements Runnable{
    private DatagramSocket ds;
    private ServerList servers;

    public KeepAlive(DatagramSocket ds, ServerList servers) {
        this.ds = ds;
        this.servers = servers;
    }

    public void run() {
        while(true) {
            try {
                // Percorrer a lista e enviar um keep alive a todos os servers

                Thread.sleep(60000);
            } catch (InterruptedException ignored) { }
        }
    }
}
