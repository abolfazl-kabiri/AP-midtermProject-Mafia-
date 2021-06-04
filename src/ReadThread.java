import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadThread extends Thread {

    private BufferedReader reader;
    private InputStream in;
    private Player player;


    public ReadThread(InputStream in, Player player) {
        this.in = in;
        reader = new BufferedReader(new InputStreamReader(in));
        this.player = player;
    }

    public void run() {
        try {
            while (true)
            {
                String msg = reader.readLine();
                System.out.println(msg);
                if(msg.startsWith("welcome"))
                    player.setNameAccepted(true);
            }
        } catch (IOException io){
            io.printStackTrace();
        }
    }
}
