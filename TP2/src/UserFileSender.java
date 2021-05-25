import java.io.*;
import java.net.Socket;

/**
 * Classe que implementa o UserFileSender, que vai ser responsável por concatenar todos os bytes dos diferentes chuncks de um ficheiro e eniá-lo para o User
 */
public class UserFileSender implements Runnable {
    private UserList users;
    private int user_id;

    /**
     * Construtor do UserFileSender
     * @param users     Classe onde constam informações sobre os vários Users que efeturam pedidos ao Gateway
     * @param user_id   id do user que efetua o pedido do ficheiro pela qual esta Thread está responsável
     */
    public UserFileSender(UserList users, int user_id) {
        this.users = users;
        this.user_id = user_id;
    }

    /**
     * Método que executa a Thread
     */
    public void run() {
        try {
            UserData user = users.getUserData(user_id);

            user.isFull();

            Socket s = user.getSocket();
            BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());

            for(int i=0; i<user.getChucks(); i++) {
                byte[] arr = user.getDataChunk(i);
                out.write(arr, 0, arr.length);
                out.flush();
            }

            System.out.println("Ficheiro enviado ao utilizador\n");

            out.close();
            s.close();

            users.remove(user_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
