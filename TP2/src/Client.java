import java.net.*;

class ClientEcho {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public ClientEcho() throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public String sendEcho(String msg) {
        String received = null;
        try {
            buf = msg.getBytes();
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, address, 80);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new String(
                    packet.getData(), 0, packet.getLength());
        } catch (Exception ignored) {}
        return received;
    }

    public void close() {
        socket.close();
    }
}

public class Client {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        ClientEcho cli = new ClientEcho();
        cli.sendEcho("A Woody não sabe merda nenhuma de CC. Ja parece o Rufus");
    }
}