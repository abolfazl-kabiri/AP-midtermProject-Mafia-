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
    private boolean isAwake;


    public PlayerHandler( Server server,Socket socket) {
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

    public Role getPlayerRole() {
        return playerRole;
    }

    String msg = "";
    Message message = null;

    public void run() {
        setup();

        waitPlayer();

        handleRole();

       handleIntroduction();
//
//        do {
//            try {
//                message = (Message) in.readObject();
//                if(message != null){
//                    System.out.println(message.getText());
//                    server.broadcast(message.getText(),this);
//                }
//            } catch (ClassNotFoundException | IOException c){
//                c.printStackTrace();
//            }
//        } while (true);
    }


    public String getPlayerName() {
        return playerName;
    }

    public void setMsg(String msg){
        this.msg = msg;
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
    }

    private void handleRole() {

        sendMessage(playerRole);
        sendMessage("your role is \"" + playerRole.toString() + "\"");
    }

    private void handleIntroduction() {
        msg = null;
        waitPlayer();
        if(msg != null){
            sendMessage(msg);
        }
    }

    private void waitPlayer(){
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private void setup(){

        handleName();

        handleReady();

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
