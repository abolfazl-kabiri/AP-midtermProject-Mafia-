import java.io.*;
import java.net.Socket;

public class PlayerHandler extends Thread{

    private BufferedReader reader;
    private Server server;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PrintWriter writer;

    public PlayerHandler( Server server,Socket socket)
    {
        this.server = server;
        this.socket = socket;

    }

    public void run()
    {
        String msg;
        try {
            out = socket.getOutputStream();
            in = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            writer = new PrintWriter(out,true);

            sendMessage("enter your name: ");
            msg = reader.readLine();
            while (msg == null)
                msg = reader.readLine();

            if(server.checkName(msg))
                sendMessage("welcome " + msg);
            else
            {
                while (!(server.checkName(msg))){
                    sendMessage("this name is already taken");
                    sendMessage("enter a new name: ");
                    msg = reader.readLine();
                    while (msg == null)
                        msg = reader.readLine();
                }
                sendMessage("welcome " + msg);
            }

            sendMessage("write \"ready\" to start match");
            msg = reader.readLine();
            while (msg == null)
                msg = reader.readLine();

            while (!(msg.equalsIgnoreCase("ready"))){
                msg = reader.readLine();
            }
            sendMessage("ok wait for other players");


            do {
                 msg = reader.readLine();
                if(msg != null )
                {
                    System.out.println("received");
                    System.out.println(msg);
                    server.broadcast(msg,this);
                }
            } while (true);
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    public void sendMessage(String msg)
    {
        if(msg != null)
        {
            writer.println(msg);
        }
    }
}
