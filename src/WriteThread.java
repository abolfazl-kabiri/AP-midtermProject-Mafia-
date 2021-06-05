import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class WriteThread extends Thread{

    Scanner scanner = new Scanner(System.in);

    private ObjectOutputStream out;
    private Player player;


    public WriteThread(ObjectOutputStream out, Player player){
        this.player = player;
        this.out = out;
    }

    public void run() {
        while (true)
        {
            System.out.println("enter message: ");
            String msg = scanner.nextLine();
            msg = player.getName() + ": " + msg;
            sendMessage(msg);
        }
    }

    private void sendMessage(String msg){
        if (msg != null) {
            try {
                out.writeObject(new Message(msg));
            } catch (IOException io){
                io.printStackTrace();
            }
        }
    }
}