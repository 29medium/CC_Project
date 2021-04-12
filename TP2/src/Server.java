import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class ServerEcho extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public ServerEcho() throws SocketException {
        socket = new DatagramSocket(80);
    }

    public void run() {
        running = true;

        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);

                if (received.equals("end")) {
                    running = false;
                    continue;
                }
                socket.send(packet);
            }
        } catch (IOException ignored) { }

        socket.close();
    }
}

public class Server {
    public static void main(String[] args) throws SocketException {
        ServerEcho s = new ServerEcho();
        s.start();
    }
}