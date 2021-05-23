import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class FFServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(8888);
        PacketQueue pq = new PacketQueue();

        String ipGateway = args[0];
        int portaGateway = Integer.parseInt(args[1]);

        // Criar pacote a dizer que se ligou (4)
        // Adicionar pacote a queue

        Thread receiver = new Thread(new ReceiverFFS(ds, pq));
        Thread sender = new Thread(new SenderFFS(ds, pq, ipGateway, portaGateway));

        receiver.start();
        sender.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String pedido = reader.readLine();
        while (pedido!="exit") pedido = reader.readLine();

        Packet pacoteEncerrarLigacao = new Packet(7, ipGateway, portaGateway, 1, 1, "FFs ira encerrar ligacao estabelecida".getBytes(StandardCharsets.UTF_8));
        pq.add(pacoteEncerrarLigacao);
    }
}
