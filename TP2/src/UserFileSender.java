import java.io.*;
import java.net.Socket;

public class UserFileSender implements Runnable {
    private UserList users;
    private int user_id;

    public UserFileSender(UserList users, int user_id) {
        this.users = users;
        this.user_id = user_id;
    }

    @Override
    public void run() {
        try {
            UserData user = users.getUserData(user_id);

            user.isFull();

            Socket s = user.getSocket();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            for(int i=0; i<user.getChucks(); i++)
                out.write(user.getDataChunk(i));
            out.flush();

            out.close();
            s.close();

            users.remove(user_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
