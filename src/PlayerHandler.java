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
    private Thread vote;


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

    String msg = "";
    Message message = null;

    public void run() {
        setup();

        waitPlayer();

        handleRole();

        handleIntroduction();

        waitPlayer();

        handleChatTime();

        waitPlayer();

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


    boolean chatTime;
    public void handleChatTime(){
        chatTime = true;
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
                while (chatTime){
                    message = getMessage();
                    msg = message.getText();
                    System.out.println(msg);
//                    String [] msgToken = msg.split(":",2);
//                    if(msgToken[1].length() == 6 && msgToken[1].contains("ready")){
//                        isReady = true;
//                        return;
//                    }
                    server.broadcast(msg);
                }
                setReady(true);
                return;

            }
        }

        chat = new ChatHandler();

        chat.start();
    }


    boolean voteTime;
    private void vote(){
        voteTime = true;
        sendMessage("vote time\nyou have 30 seconds\nfollowing players are alive\nwrite the name to vote");

        String alivePlayers = server.getList();
        sendMessage(alivePlayers);


        class VoteHandler extends Thread{

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
                String target;
                while (voteTime){
                    message = getMessage();
                    target = message.getText();
                    String[] targetTokens = target.split(" ",2);
                    target = targetTokens[1].trim();

                    if(server.acceptableVote(target, playerName)){
                        sendMessage("accepted");
                        server.storeVotes(playerName,target);
                    }
                    else
                        sendMessage("unacceptable try again");
                }
                return;
            }
        }

        vote = new VoteHandler();
        vote.start();

        waitPlayer();
        try {
            Thread.sleep(8000);
        }catch (InterruptedException inter){
            inter.printStackTrace();
        }
        System.out.println("woke up");
        //after this result of voting should be sent to players



        msg = server.gatherVotes();
        sendMessage(msg);

        // here i need a new wait

        waitPlayer();
        System.out.println("notified");

        msg = server.getVictim();
        sendMessage(msg +  " is the victim");

        if(!(playerRole instanceof Mayor)){
            sendMessage("wait for mayor");
            waitPlayer();
        } else {
           msg = playerRole.action(out,in,server);
           server.mayorResponse(msg);
           waitPlayer();
        }

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
}
