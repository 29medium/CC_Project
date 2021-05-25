import java.io.*;
import java.net.*;

/**
 * Classe que implementa o SenderGateway, onde são enviados os pacotes que constam na queue de Pacotes a enviar
 */
public class Gateway {
    /**
     * Função main que corre o Gateway
     * @param args          Argumentos recebidos do Gateway
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        DatagramSocket ds = new DatagramSocket(8888);
        PacketQueue queue = new PacketQueue();
        ServerList servers = new ServerList();
        UserList users = new UserList();

        int userCounter = 0; // Cada User vai ter um id para guardar na userlist

        System.out.println("Gateway conectou-se com o IP: " + InetAddress.getLocalHost().getHostAddress() + "\n");

        // Threads criadas para para receber dos FFSs, enviar para os FFSs e para verificar beacons emitidos pelos FFSs
        Thread receiver = new Thread(new ReceiverGateway(ds, servers, queue, users));
        Thread sender = new Thread(new SenderGateway(ds, queue));
        Thread beacon = new Thread(new BeaconGateway(servers));

        // Start das threads criadas
        receiver.start();
        sender.start();
        beacon.start();

        while(true) {
            Socket s = ss.accept();

            users.addSocket(userCounter++, s);

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String userPacket = in.readLine();
            String[] tokens = userPacket.split(" ");

            ServerData sd = servers.getServer();

            if(sd!=null)
                queue.add(new Packet(1, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), userCounter-1, 0, tokens[1].getBytes()));
            else {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                System.out.println("Não existem FFSs\n");
                out.write("Server not found\n");
                out.flush();

                out.close();
                s.close();
            }
        }
    }
}