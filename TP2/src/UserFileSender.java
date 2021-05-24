import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF(user.getDataFile());
            out.flush();

            out.close();
            s.close();

            users.remove(user_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
