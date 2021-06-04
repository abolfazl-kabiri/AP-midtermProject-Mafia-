import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class WriteThread extends Thread{

    Scanner scanner = new Scanner(System.in);

    private ObjectOutputStream objOut;
    private OutputStream out;
    private Player player;


    public WriteThread(OutputStream out, Player player){
        this.out = out;
        this.player = player;
        try {
            objOut = new ObjectOutputStream(out);
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    public void run()
    {
        while (!(player.isNameAccepted())){
            String name  = scanner.nextLine();
            player.setName(name);
           sendMessage(name);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

        }

        while (!(player.isReady())){
            String msg = scanner.nextLine();
            if(msg.equalsIgnoreCase("ready"))
                player.setReady(true);
            sendMessage(msg);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

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
                objOut.writeObject(new Message(msg));
            } catch (IOException io){
                io.printStackTrace();
            }
        }
    }
}
