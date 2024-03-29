import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Classe que implementa o FFServer
 */
public class FFServer {
    public static boolean EXIT = false;
    public static String ROOTPATH = "/home/core";

    /**
     * Método que inicializa o FFServer
     *
     * @param args                  Argumentos para a conecção ao Gateway
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket ds = new DatagramSocket(8888);
        PacketQueue pq = new PacketQueue();
        String ipGateway = args[0];
        int portaGateway = Integer.parseInt(args[1]);

        System.out.println("FFS conectou-se com o IP: " + InetAddress.getLocalHost().getHostAddress() + "\n");

        Thread receiver = new Thread(new ReceiverFFS(ds, pq, ipGateway, portaGateway));
        Thread sender = new Thread(new SenderFFS(ds, pq));
        Thread beacon = new Thread(new BeaconFFS(pq, ipGateway, portaGateway));

        receiver.start();
        sender.start();
        beacon.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String pedido = reader.readLine();

        while (!pedido.equals("exit"))
            pedido = reader.readLine();

        Packet pacoteEncerrarLigacao = new Packet(7, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, -1, 1, "FFs ira encerrar ligacao estabelecida".getBytes(StandardCharsets.UTF_8));
        pq.addFirst(pacoteEncerrarLigacao);

        System.out.println("Pedido de encerramento enviado ao Gateway\n");

        Thread.sleep(1000);

        pq.signalCon();

        System.exit(0);
    }
}
