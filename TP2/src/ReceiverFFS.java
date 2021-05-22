import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
                Packet p = new Packet(arr); // Cria um pacote com as merdas recebidas do gateway

                // Intrepreta e cria novo pacote após interpretação
                Packet newp;
                switch (p.getTipo()) {
                    case 1:
                        newp = packetType1(p);
                        break;
                    default:
                        break;
                }

                // Adiciona novo pacote a queue
                pq.add(newp);
            } catch (IOException ignored) {}
        }
    }

    public Packet packetType1(Packet p) {

        // Pegar na data, tentar ir buscar o ficheiro

        // Se encontrar o ficheiro devolver tipo 2 e na data vai o ficheiro

        // Se não encontrar o ficheiro devolver tipo 3 e na data vai uma mensagem de erro

        return new Packet();
    }
}
