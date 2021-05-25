import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Classe que implementa o Receiver do FFServer
 */
class ReceiverFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;
    private String ipGateway;
    private int portaGateway;

    /**
     * Construtor da classe ReceiverFFS
     *
     * @param ds            DatagramSocket utilizado para receber os pacotes
     * @param pq            PacketQueue utilizada para guardar os pacotes a enviar
     * @param ipGateway     IP do Gateway
     * @param portaGateway  Porta do Gateway
     */
    public ReceiverFFS(DatagramSocket ds, PacketQueue pq, String ipGateway, int portaGateway) {
        this.ds = ds;
        this.pq = pq;
        this.ipGateway = ipGateway;
        this.portaGateway = portaGateway;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        try {
            packetType6();
        } catch (UnknownHostException ignored) { }

        while (!FFServer.EXIT) {
            try {
                byte[] arr = new byte[Packet.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

		        byte[] conteudoPacote = new byte[dp.getLength()];
		        System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());
                Packet p = new Packet(conteudoPacote); // Cria um pacote com as merdas recebidas do gateway

                Packet newp;
                switch (p.getTipo()) {
                    case 1:
                        packetType1(p);
                        break;
                    case 4:
                        packetType4(p);
                        break;
                    case 8:
                        System.out.println("Gateway confirmou conecção do FFS\n");
                        break;
                    case 10:
                        System.out.println("Gateway confirmou encerramento do FFS\n");
                        FFServer.EXIT = true;
                        break;
                    default:
		                pq.add(new Packet(11,InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888,portaGateway,-1,0,"Erro em alguma coisa".getBytes(StandardCharsets.UTF_8)));
		                break;
                }
            } catch (IOException ignored) {}
        }
    }

    /**
     * Método que processa o pacote do tipo 1
     *
     * @param p             Pacote recebido do tipo 1
     * @throws IOException
     */
    public void packetType1(Packet p) throws IOException {
	    File file = new File(FFServer.ROOTPATH + p.getDataString());

        System.out.println("Gateway pediu informação do ficheiro: " + p.getDataString() + "\n");
        Packet pnew;

        if (file.exists() && file.isFile()) {
            long size = file.length();
            String data = p.getDataString() + "#SIZE#" + size;
            pnew = new Packet(2, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, p.getIdUser(), 0, data.getBytes(StandardCharsets.UTF_8)); // Se encontrar o ficheiro devolver tipo 2 e na data vai o ficheiro
        }
        else
            pnew = new Packet(3,InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,p.getIdUser(),0, p.getData());// Se não encontrar o ficheiro devolver tipo 3 e na data vai uma mensagem de erro

        pq.add(pnew);
    }

    /**
     * Método que processa o pacote do tipo 4
     *
     * @param p             Pacote do tipo 4
     * @throws IOException
     */
    public void packetType4(Packet p) throws IOException {
        File file = new File(FFServer.ROOTPATH + p.getDataString());

        System.out.println("Gateway pediu envio de um chunk do ficheiro " + p.getDataString() + "\n");

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
        pq.add(new Packet(5, InetAddress.getLocalHost().getHostAddress(),ipGateway,8888,portaGateway,p.getIdUser(),p.getChucnkTransferencia(), bytesData));
    }

    /**
     * Método que processa o pacote do tipo 6
     *
     * @throws UnknownHostException
     */
    public void packetType6() throws UnknownHostException {
        pq.add(new Packet(6, InetAddress.getLocalHost().getHostAddress(), ipGateway, 8888, portaGateway, -1, 0, "FFs pretende estabelecer ligaçao com o Gateway".getBytes(StandardCharsets.UTF_8)));
    }
}
