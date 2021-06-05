import java.io.*;
import java.net.Socket;

public class PlayerHandler extends Thread{

    private Server server;
    private Socket socket;
    private String playerName;
    private Role playerRole;
    private boolean isReady;
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
        isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPlayerRole(Role playerRole) {
        this.playerRole = playerRole;
    }

    String msg = "";
    Message message = null;

    public void run() {
        setup();

        handleRole();

        sendMessage("game started");

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



    private void handleName(){
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
            this.playerName = msg;
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
    }

    private void handleReady(){
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

        setReady(true);
        sendMessage("ok wait for other players");

        try{
            Thread.sleep(10000);
        } catch (InterruptedException in){
            in.printStackTrace();
        }
    }

    private void handleRole() {
        while (playerRole == null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

        sendMessage(playerRole);
        sendMessage("your role is \"" + playerRole.toString() + "\"");
    }

    private void setup(){

        handleName();

        handleReady();

        while (!(server.canStartGame())){
            try{
                Thread.sleep(500);
            } catch (InterruptedException in){
                in.printStackTrace();
            }
        }
    }

    public void sendMessage(String msg) {
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

    public void sendMessage(Role role){
        try{
            if(role != null)
                out.writeObject(role);
        } catch (IOException io){
            io.printStackTrace();
        }
    }
}
