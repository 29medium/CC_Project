import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;

class ReceiverFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;

    public ReceiverFFS(DatagramSocket ds, PacketQueue pq, String ipGateway, int portaGateway) {
        this.ds = ds;
        this.pq = pq;
        this.ipGateway = ipGateway;
        this.portaGateway = portaGateway;
    }

    public void run() {
        try {
            pq.add(packetType6());
        } catch (UnknownHostException ignored) { }

        while (true) {
            try {
                byte[] arr = new byte[Packet.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

		        byte[] conteudoPacote = new byte[dp.getLength()];
		        System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());
                Packet p = new Packet(conteudoPacote); // Cria um pacote com as merdas recebidas do gateway

                System.out.println(p.toString());

                // Intrepreta e cria novo pacote após interpretação
                Packet newp;
                switch (p.getTipo()) {
                    case 1:
                        newp = packetType1(p);
                        break;
                    default:
		                newp = new Packet(11,InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888,portaGateway,Packet.getIdTransferenciaCounter(),-1,0,"Erro em alguma coisa".getBytes(StandardCharsets.UTF_8));
                        break;
                }

                // Adiciona novo pacote a queue
                pq.add(newp);
            } catch (IOException ignored) {}
        }
    }

    public Packet packetType1(Packet p) throws IOException {

	    File file = new File("/home/core" + p.getDataString());

	    // Pegar na data, tentar ir buscar o ficheiro
        if (file.exists() && file.isFile()) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new Packet(2, InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,Packet.getIdTransferenciaCounter(),p.getIdUser(),0, bytes); // Se encontrar o ficheiro devolver tipo 2 e na data vai o ficheiro
        }
        else {
            return new Packet(3,InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,Packet.getIdTransferenciaCounter(),p.getIdUser(),0,"Ficheiro Nao Encontrado".getBytes(StandardCharsets.UTF_8));// Se não encontrar o ficheiro devolver tipo 3 e na data vai uma mensagem de erro
        }
    }

    public Packet packetType6() throws UnknownHostException {
        // Ao iniciar uma ligação (quando esta thread é criada), o FFs informa o Gateway que se ligou a ele
        return new Packet(6, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, Packet.getIdTransferenciaCounter(), -1, 0, "FFs pretende estabelecer ligaçao com o Gateway".getBytes(StandardCharsets.UTF_8));
    }
}
