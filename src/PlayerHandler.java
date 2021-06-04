import java.io.*;
import java.net.Socket;

public class PlayerHandler extends Thread{

    private Server server;
    private Socket socket;
    private String playerName;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public PlayerHandler( Server server,Socket socket)
    {
        this.server = server;
        this.socket = socket;
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException io){
            io.printStackTrace();
        }

    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    String msg = "";
    Message message = null;

    public void run()
    {
        setup();
        do {
            try {
                message = (Message) in.readObject();
                if(message != null){
                    System.out.println(message.getText());
                    server.broadcast(message.getText(),this);
                }
            } catch (ClassNotFoundException | IOException c){
                c.printStackTrace();
            }
        } while (true);
    }

    private void setup(){
        sendMessage("enter your name: ");
        try {
            message = (Message) in.readObject();
            while (message == null)
                message = (Message) in.readObject();
            msg = message.getText();
        } catch (ClassNotFoundException | IOException c){
            c.printStackTrace();
        }

        if(server.checkName(msg)){
            sendMessage("welcome " + msg);
            setPlayerName(msg);
        }
        else
        {
            while (!(server.checkName(msg))){
                sendMessage("this name is already taken");
                sendMessage("enter a new name: ");

                try {
                    message = (Message) in.readObject();
                    while (message == null)
                        message = (Message) in.readObject();
                    msg = message.getText();
                } catch (ClassNotFoundException | IOException c){
                    c.printStackTrace();
                }

            }
            sendMessage("welcome " + msg);
            setPlayerName(msg);
        }

        sendMessage("write \"ready\" to start match");

        try {
            message = (Message) in.readObject();
            while (message == null)
                message = (Message) in.readObject();
            msg = message.getText();
            while (!(msg.equalsIgnoreCase("ready"))){
                message = (Message) in.readObject();
                msg = message.getText();
            }
        } catch (ClassNotFoundException | IOException c){
            c.printStackTrace();
        }

        sendMessage("ok wait for other players");

    }

    public void sendMessage(String msg)
    {
        try {
            if(msg != null)
            {
                Message message = new Message(msg);
                out.writeObject(message);
            }
        }  catch (IOException io){
            io.printStackTrace();
        }
    }
}
