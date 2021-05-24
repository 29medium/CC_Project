import java.io.*;
import java.net.*;

public class Gateway {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        DatagramSocket ds = new DatagramSocket(8888);

        PacketQueue queue = new PacketQueue();
        ServerList servers = new ServerList();
        UserList users = new UserList();
        int userCounter=0; // cada user vai ter um id para guardar na userlist

        Thread keepAlive = new Thread(new KeepAlive(ds, servers));
        keepAlive.start();

        // Thread para receber do FFS
        Thread receiver = new Thread(new ReceiverGateway(ds, servers, queue, users));
        Thread sender = new Thread(new SenderGateway(ds, servers, queue));

        receiver.start();
        sender.start();

        while(true) {
            Socket s = ss.accept();

            users.addSocket(userCounter++, s);

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String userPacket = in.readLine();
            System.out.println(userPacket);
            String[] tokens = userPacket.split(" ");

            ServerData sd = servers.getServer();
            queue.add(new Packet(1, InetAddress.getLocalHost().getHostAddress(), sd.getIp(), 8888, sd.getPort(), Packet.getIdTransferenciaCounter(), userCounter-1, 0, tokens[1].getBytes()));
        }
    }
}