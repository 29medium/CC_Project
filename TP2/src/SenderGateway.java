import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class SenderGateway implements Runnable {
    private DatagramSocket ds;
    private BufferedReader in;

    public SenderGateway(DatagramSocket ds, BufferedReader in) {
        this.ds = ds;
        this.in = in;
    }

    public void run() {
        try {
            String msg = "Cona";
            byte[] buf = msg.getBytes();
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), 80);

            ds.send(packet);
        } catch (IOException ignored) {}
    }
}
