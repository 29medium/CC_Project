import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que implementa uma queue de pacotes
 */
class PacketQueue {
    private LinkedList<Packet> packets;
    private ReentrantLock lock;
    private Condition con;

    /**
     * Construtor da classe PacketQueue
     */
    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        con = lock.newCondition();
    }

    /**
     * Método que adiciona um Pacote à queue
     *
     * @param packet    Pacote a ser adicionado
     */
    public void add(Packet packet) {
        lock.lock();
        try {
            packets.add(packet);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método que adiciona um pacote ao início da queue (Alta prioriodade)
     *
     * @param packet    Pacote a ser adicionado
     */
    public void addFirst(Packet packet) {
        lock.lock();
        try {
            packets.addFirst(packet);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método que remove um pacote da queue
     *
     * @return  Pacote removido
     * @throws InterruptedException
     */
    public Packet remove() throws InterruptedException {
        lock.lock();
        try {
            while (packets.isEmpty())
                con.await();

            return packets.isEmpty() ? null : packets.remove();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método que verifica se a queue está vazia
     *
     * @return  Boleano com o resultado
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return packets.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método que envia um sinal quando a queue estiver vazia
     */
    public void signalCon() {
        lock.lock();
        try {
            while(!packets.isEmpty());

            con.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
