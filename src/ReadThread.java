import javax.management.relation.Role;
import java.io.*;

public class ReadThread extends Thread {


    private InputStream in;
    private Player player;
    private ObjectInputStream objIn;




    public ReadThread(InputStream in, Player player) {
        this.in = in;
        this.player = player;
        try {
            objIn = new ObjectInputStream(in);
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    public void run() {

            while (true)
            {
                try {
                    Object message = objIn.readObject();
                    if(message instanceof Message){
                        String msg = ((Message) message).getText();
                        System.out.println(msg);
                        if(msg.startsWith("welcome"))
                            player.setNameAccepted(true);
                    } else if( message instanceof Role){
                      Role role = (Role) message;
                      player.setRole(role);
                    }
                } catch (IOException io){
                    io.printStackTrace();
                } catch (ClassNotFoundException c){
                    c.printStackTrace();
                }
            }
    }
}
