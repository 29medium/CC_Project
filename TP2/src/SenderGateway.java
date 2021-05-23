import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private BufferedReader in;
    private final Socket s;
    private final ServerList servers;

    public SenderGateway(DatagramSocket ds, Socket s, ServerList servers) throws IOException {
        this.ds = ds;
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.s = s;
        this.servers = servers;
    }

    public void run() {
        try {
	
            String userPacket = in.readLine();
	        System.out.println(userPacket);
            String[] tokens = userPacket.split(" ");
		
            Packet p = new Packet(1, "10.1.1.2", 88, 1, 1, tokens[1].getBytes());
            System.out.println(p.toString());
	    byte[] buf = p.packetToBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(p.getIpDestino()),88);
	        System.out.println(p.toString());

            ds.send(packet);
        } catch (IOException ignored) {}
    }
}
