import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que posusi informação sobre um dado User
 */
class UserData {
    private int user_id;
    private Socket s;
    private int chuncks;
    private Set<Integer> remainigFragments;
    private Map<Integer, Packet> fragments;
    private ReentrantLock lock;
    private Condition isFull;

    /**
     * Cronstrutor do UserData
     * @param user_id   Id do User
     * @param s         Socket do User
     */
    public UserData(int user_id, Socket s) {
        this.user_id = user_id;
        this.s = s;
        this.chuncks = 0;
        this.remainigFragments = new TreeSet<>();
        this.fragments = new HashMap<>();
        this.lock = new ReentrantLock();
        this.isFull = lock.newCondition();
    }

    /**
     * Função que retorna Id do User de um dado UserData
     * @return  Id do User
     */
    public int getUser_id() {
        lock.lock();
        try {
            return user_id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que retorna Socket de um dado UserData
     * @return  Socket do User
     */
    public Socket getSocket() {
        lock.lock();
        try {
            return s;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set do número de chuncks
     * @param chunks    Número de chuncks
     */
    public void setChunks(int chunks) {
        lock.lock();
        try {
            this.chuncks = chunks;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get do número de chuncks
     * @return      Número de Chuncks
     */
    public int getChucks() {
        lock.lock();
        try {
            return chuncks;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que adiciona um fragmento de um ficheiro (na forma de Packet) ao UserData
     * @param p     Packet com o fragmento de um ficheiro
     */
    public void addFragment(Packet p) {
        lock.lock();
        try {
            int chunck = p.getChucnkTransferencia();

            fragments.put(chunck, p);
            remainigFragments.remove(chunck);

            if(fragments.size() == chuncks)
                isFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que adiociona um chunck que falta receber
     * @param chunck    Inteiro que indica chunck que falta receber
     */
    public void addRemainingFragment (int chunck) {
        lock.lock();
        try {
            remainigFragments.add(chunck);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que dá clone ao Set que indica os Fragmentos que falta receber
     * @return  Set com os Fragmentos que falta receber
     */
    public Set<Integer> getRemaingFragments () {
        lock.lock();
        try {
            return new TreeSet<>(remainigFragments);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que indica se existem mais Fragmentos para receber
     * @return  Booleano que indica se existem mais Fragmentos para receber
     */
    public boolean noMoreFragments () {
        lock.lock();
        try {
            return remainigFragments.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que indica se Map com os Fragmentos recebidos está completo
     */
    public void isFull() {
        lock.lock();
        try {
            try {
                while (fragments.size() != chuncks)
                    isFull.await();
            } catch (InterruptedException e) {
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que retorna a data de um dado chucnk
     * @param chunk     Chucnk que pretendemos receber a data
     * @return          Array de Bytes que possui data de um dado chunck
     */
    public byte[] getDataChunk(int chunk) {
        lock.lock();
        try {
            return fragments.get(chunk).getData();
        } finally {
            lock.unlock();
        }
    }
}



/**
 * Classe que possui informações sobre todos os Users
 */
public class UserList {
    private Map<Integer, UserData> users;
    private ReentrantLock lock;

    /**
     * Construtor da UserList
     */
    public UserList() {
        this.users = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    /**
     * Função que dado um User retorna o seu Socket TCP
     * @param i     Id do user que pretendemos obter o Socket
     * @return      Socket do User pedido
     */
    public Socket getSocket(int i) {
        lock.lock();
        try {
            return users.get(i).getSocket();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que dado um User e um socker cria um novo UserData para esse User
     * @param i     Id do User
     * @param s     Socket do User
     */
    public void addSocket(int i, Socket s) {
        lock.lock();
        try {
            users.put(i, new UserData(i, s));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que remove um User e os seus dados da UserList
     * @param i     Id do user a remover
     */
    public void remove(int i) {
        lock.lock();
        try {
            users.remove(i);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que dado um user coloca nos seus dados o número de chuncks do ficheiro que requeriu
     * @param i         Id do User a dar set nos chuncks
     * @param chunks    número de Chuncks
     */
    public void setChunks(int i, int chunks) {
        lock.lock();
        try {
            users.get(i).setChunks(chunks);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que adiciona num User um fragmento do ficheiro (no formato de Packet) por si pedido
     * @param i     Id do User
     * @param p     Packet a adicionar
     */
    public void addFragment(int i, Packet p) {
        lock.lock();
        try {
            users.get(i).addFragment(p);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que retorna dados (UserData) de um dado User
     * @param i     Id do User
     * @return      UserData do User
     */
    public UserData getUserData(int i) {
        lock.lock();
        try {
            return users.get(i);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que povoa um Set com os fragmentos que faltam do ficheiro requirido pelo User
     * @param i         Id do User
     * @param nrChunks  Número de chuncks a adicionar
     */
    public void addRemainingFragmentUser(int i, int nrChunks) {
        lock.lock();
        try {
            for (int n = 0; n < nrChunks; n++)
                users.get(i).addRemainingFragment(n);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Função que verifica se um dado User existe na UserList
     * @param i     Id do User
     * @return      Booleano que indica se o User existe na UserList
     */
    public boolean hasUser(int i) {
        lock.lock();
        try {
            return users.get(i) != null;
        } finally {
            lock.unlock();
        }
    }
}
