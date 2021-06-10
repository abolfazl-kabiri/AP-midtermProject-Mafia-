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
    private boolean isAlive;
    private Thread chat;


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
        isAlive = true;
    }


   public boolean playerIsAlive(){
        return isAlive;
   }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready){
        this.isReady = ready;
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

       waitPlayer();

       handleChatTime();
        System.out.println("out of chat in handler");

       vote();



    }

    private Message getMessage(){
        message = null;
        while (message == null){
            try {
                message = (Message) in.readObject();
            } catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return message;
    }

    private void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException inter){
            inter.printStackTrace();
        }
    }

    private void waitForAll(){
        while (!(server.allReady())){
            sleep(500);
        }
        sleep(2000);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    private void handleName(){
        sendMessage("enter your name: ");

        message = getMessage();
        msg = message.getText();

        if(server.checkName(msg)){
            sendMessage("welcome " + msg);
            this.playerName = msg;
        }
        else
        {
            while (!(server.checkName(msg))){
                sendMessage("this name is already taken");
                sendMessage("enter a new name: ");

                message = getMessage();
                msg = message.getText();
            }
            sendMessage("welcome " + msg);
            setPlayerName(msg);
        }
    }

    private void handleReady(){
        sendMessage("write \"ready\" to start match");
            message = getMessage();
            msg = message.getText();
            while (!(msg.equalsIgnoreCase("ready"))){
                message = getMessage();
                msg = message.getText();
            }
        this.isReady = true;
        sendMessage("ok wait for other players");
    }

    private void handleRole() {
        sendMessage(playerRole);
    }

    private void handleIntroduction() {
        msg = null;
        waitPlayer();
        if(msg != null){
            sendMessage(msg);
        }
    }


    public void handleChatTime(){
        sendMessage("It is day\nyou can chat for 5 minutes or you can send \"ready\" to stop sending");

        isReady = false;

        class ChatHandler extends Thread{

            private Message getMessage(){
                message = null;
                while (message == null){
                    try {
                        message = (Message) in.readObject();
                    } catch (IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    }
                }
                return message;
            }


            @Override
            public void run() {
                while (!chat.isInterrupted()){
                    message = getMessage();
                    msg = message.getText();
//                    String [] msgToken = msg.split(":",2);
//                    if(msgToken[1].length() == 6 && msgToken[1].contains("ready")){
//                        isReady = true;
//                        return;
//                    }
                    server.broadcast(msg);
                }
                return;
            }
        }

        chat = new ChatHandler();

        chat.start();
        waitPlayer();
    }

    public Thread getChat(){
        return chat;
    }

    private void vote(){
        sendMessage("vote time\nfollowing players are alive\nwrite the name to vote or write \"none\"");

        String alivePlayers = server.getList();
        sendMessage(alivePlayers);


        message = getMessage();
        String target = message.getText();
        while (!(server.acceptableVote(target))){
            sendMessage("unacceptable try again");

            message = getMessage();
            target = message.getText();
        }

        sendMessage("accepted");
        waitPlayer();
        System.out.println("second notified");
        server.gatherVotes(target,this);


    }

    public void waitPlayer(){
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
