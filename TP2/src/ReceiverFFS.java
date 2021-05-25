import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

class ReceiverFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;
    private volatile boolean exit;

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

        while (!FFServer.EXIT) {
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
                        pq.add(newp);
                        break;
                    case 4:
                        newp = packetType4(p);
                        pq.add(newp);
                        break;
                    case 8:
                        System.out.println("Conectado com sucesso");
                        break;
                    case 10:
                        System.out.println("Conecção encerrada");
                        FFServer.EXIT = true;
                        break;
                    default:
		                newp = new Packet(11,InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888,portaGateway,-1,0,"Erro em alguma coisa".getBytes(StandardCharsets.UTF_8));
                        pq.add(newp);
		                break;
                }
            } catch (IOException ignored) {}
        }
    }

    public Packet packetType1(Packet p) throws IOException {
	    File file = new File("/home/core" + p.getDataString());
	    // Pegar na data, tentar ir buscar o ficheiro
        if (file.exists() && file.isFile()) {
            long size = file.length();
            String data = p.getDataString() + "#SIZE#" + size;
            return new Packet(2, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, p.getIdUser(), 0, data.getBytes(StandardCharsets.UTF_8)); // Se encontrar o ficheiro devolver tipo 2 e na data vai o ficheiro
        }
        else
            return new Packet(3,InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,p.getIdUser(),0,"Ficheiro Nao Encontrado".getBytes(StandardCharsets.UTF_8));// Se não encontrar o ficheiro devolver tipo 3 e na data vai uma mensagem de erro
    }

    public Packet packetType4(Packet p) throws IOException {
        File file = new File("/home/core" + p.getDataString());

        byte[] bytesFile = Files.readAllBytes(file.toPath());

        int offset = p.getChucnkTransferencia() * Packet.MAX_SIZE_DATA;
        int size = (int) file.length();
        int chunkSize;

        if((offset + Packet.MAX_SIZE_DATA) > size) {
            chunkSize = size - offset;
        }
        else
            chunkSize = Packet.MAX_SIZE_DATA;

        byte[] bytesData = new byte[chunkSize];
        System.arraycopy(bytesFile, offset, bytesData, 0, chunkSize);
        return new Packet(5, InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,p.getIdUser(),p.getChucnkTransferencia(), bytesData);
    }

    public Packet packetType6() throws UnknownHostException {
        // Ao iniciar uma ligação (quando esta thread é criada), o FFs informa o Gateway que se ligou a ele
        return new Packet(6, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, -1, 0, "FFs pretende estabelecer ligaçao com o Gateway".getBytes(StandardCharsets.UTF_8));
    }
}
