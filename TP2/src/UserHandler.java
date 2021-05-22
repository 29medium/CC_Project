import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;

public class UserHandler implements Runnable {
    private final DatagramSocket ds;
    private final Socket s;
    private final ServerSet servers;
    private final BufferedReader in;
    private final DataOutputStream out;

    public UserHandler(DatagramSocket ds, Socket s, ServerSet servers) throws IOException {
        this.ds = ds;
        this.s = s;
        this.servers = servers;
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new DataOutputStream(s.getOutputStream());
    }

    public void run() {
        Thread sender = new Thread(new SenderGateway(ds,in));
        Thread receiver = new Thread(new ReceiverGateway(ds,out));

        sender.start();
        receiver.start();
    }
}