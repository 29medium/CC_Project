import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Gateway {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(80);
        DatagramSocket ds = new DatagramSocket(87);

        ServerSet servers = new ServerSet();

        while(true) {
            Socket s = ss.accept();

            System.out.println("Entrou");

            Thread userHandler = new Thread(new UserHandler(ds, s, servers));
            userHandler.start();
        }
    }
}
