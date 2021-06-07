import java.io.*;

public class ReadThread extends Thread {


    private Player player;
    private ObjectInputStream in;




    public ReadThread(ObjectInputStream in, Player player) {
        this.player = player;
           this.in = in;
    }

    public void run() {
        while (true)
        {
            try {
                Message message =(Message) in.readObject();
                String msg = ((Message) message).getText();
                System.out.println(msg);
                if(msg.equals("chat time over")){
                    try {
                        synchronized (this){
                            this.wait();
                        }
                    } catch (InterruptedException inter){
                        inter.printStackTrace();
                    }
                }
            } catch (IOException io){
                io.printStackTrace();
            } catch (ClassNotFoundException c){
                c.printStackTrace();
            }
        }
    }
}
