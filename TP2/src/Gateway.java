import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Gateway {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(80);
        DatagramSocket ds = new DatagramSocket(88);

        ServerList servers = new ServerList();
        UserList users = new UserList();
        int userCounter=0; // cada user vai ter um id para guardar na userlist

        // Criar aqui uma thread para o keep alive

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