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
            if(!Thread.currentThread().isInterrupted())
            {
                String msg = scanner.nextLine();
                if(msg.trim().length() > 0){
                    msg = player.getName() + ": " + msg;
                    sendMessage(msg);
                }
            }
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