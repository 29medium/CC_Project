import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Gateway {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        DatagramSocket ds = new DatagramSocket(8888);
        PacketQueue queue = new PacketQueue();
        ServerList servers = new ServerList();
        UserList users = new UserList();
        int userCounter=0; // cada user vai ter um id para guardar na userlist

        System.out.println("Gateway conectou-se com o IP: " + InetAddress.getLocalHost().getHostAddress() + "\n");

        // Thread para receber do FFS
        Thread receiver = new Thread(new ReceiverGateway(ds, servers, queue, users));
        Thread sender = new Thread(new SenderGateway(ds, queue));
        Thread beacon = new Thread(new BeaconGateway(ds, servers));

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
            queue.add(new Packet(1, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), userCounter-1, 0, tokens[1].getBytes()));
        }
    }
}