import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class WriteThread extends Thread{

    Scanner scanner = new Scanner(System.in);

    private OutputStream out;
    private PrintWriter writer;
    private Player player;

    public WriteThread(OutputStream out, Player player){
        this.out = out;
        writer = new PrintWriter(out,true);
        this.player = player;
    }

    public void run()
    {
        while (!(player.isNameAccepted())){
            String name  = scanner.nextLine();
            player.setName(name);
            writer.println(name);
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
            writer.println(msg);
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
            writer.println(msg);
        }
    }
}
