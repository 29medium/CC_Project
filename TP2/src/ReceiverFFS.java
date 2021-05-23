import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;

class ReceiverFFS implements Runnable {
    private DatagramSocket ds;
    private PacketQueue pq;

    public ReceiverFFS(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    public void run() {
        while (true) {
            try {
                byte[] arr = new byte[1000];
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);
		
		byte[] conteudoPacote = new byte[dp.getLength()];
		System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());
                Packet p = new Packet(conteudoPacote); // Cria um pacote com as merdas recebidas do gateway

                // Intrepreta e cria novo pacote após interpretação
                Packet newp;
                switch (p.getTipo()) {
                    case 1:
                        newp = packetType1(p);
                        break;
                    default:
		        newp = new Packet(8,"10.1.1.1",8080,1,1,"Erro em alguma coisa".getBytes(StandardCharsets.UTF_8));
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
            return new Packet(2,"10.1.1.1",8080,1,1, bytes); // Se encontrar o ficheiro devolver tipo 2 e na data vai o ficheiro
        }
        else {
            return new Packet(3,"10.1.1.1",8080,1,1,"Ficheiro Nao Encontrado".getBytes(StandardCharsets.UTF_8));// Se não encontrar o ficheiro devolver tipo 3 e na data vai uma mensagem de erro
        }
    }

}
