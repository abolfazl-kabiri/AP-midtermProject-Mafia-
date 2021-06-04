import javax.management.relation.Role;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player {


    private InputStream in;
    private OutputStream out;
    private int port;
    private String name;
    private boolean nameAccepted;
    private boolean isReady;
    private Role role;

    Scanner scanner = new Scanner(System.in);

    public Player() {
        port = setPort();
        nameAccepted = false;
        isReady = false;
    }

    public void start()
    {
        try {
            Socket socket = new Socket("127.0.0.1",port);
            System.out.println("connected to server");
            in = socket.getInputStream();
            out = socket.getOutputStream();

            new ReadThread(in,this).start();
            new WriteThread(out,this).start();
        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }


    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isNameAccepted() {
        return nameAccepted;
    }

    public void setNameAccepted(boolean nameAccepted) {
        this.nameAccepted = nameAccepted;
    }

    public void setName(String name){
        this.name = name;
    }

    private int setPort()
    {
        System.out.print("enter port: ");
        port = scanner.nextInt();
        //check statement





        return port;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        Player player = new Player();
        player.start();
    }
}
