import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Gateway {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        DatagramSocket ds = new DatagramSocket(8888);

        ServerList servers = new ServerList();
        UserList users = new UserList();
        int userCounter=0; // cada user vai ter um id para guardar na userlist

        Thread keepAlive = new Thread(new KeepAlive(ds, servers));
        keepAlive.start();

        // Thread para receber do FFS
        Thread receiver = new Thread(new ReceiverGateway(ds));
        receiver.start();

        while(true) {
            Socket s = ss.accept();

            users.addSocket(userCounter++,s);

            // Thread para enviar para o FFS (1 por cliente)
            Thread sender = new Thread(new SenderGateway(ds, s, servers));
            sender.start();
        }
    }
}