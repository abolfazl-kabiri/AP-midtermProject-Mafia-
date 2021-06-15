import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class PlayerHandler extends Thread{

    private Server server;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String playerName;
    private Role playerRole;
    private Thread chat;
    private Thread vote;
    private boolean isReady;
    private boolean isAlive;
    private boolean healed;
    private boolean muted;


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
        healed = false;
        muted = false;
    }

    String msg = "";
    Message message = null;

    public void run() {
        setup();

        waitPlayer();

        handleRole();

        handleIntroduction();



        while (true){

            waitPlayer();

            handleChatTime();

            waitPlayer();

            unmute();

            vote();

            if (!server.gameContinues())
                break;

            waitPlayer();

            night();

            if(!server.gameContinues())
                break;

        }
    }

    private void unmute(){
        if(muted)
            this.muted = false;
    }

    private void setup(){

        handleName();

        handleReady();

    }

    public Socket getSocket() {
        return socket;
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

        if(server.checkName(msg) && msg.length() > 0){
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
        if(playerIsAlive()){
            sendMessage("\nIt is day\nyou can chat for 5 minutes or you can send \"ready\" to stop sending");
            isReady = false;
            if(muted)
                isReady = true;
        } else {
            sendMessage("chat time");
            isReady = true;
        }


        class ChatHandler extends Thread{

            private Message getMessage(){
                message = null;
                try {
                    while (message == null){
                        message = (Message) in.readObject();
                    }
                }catch (SocketException s){
                    System.out.println(playerName + " disconnected");
                    server.removePlayer(getPlayer());
                } catch (IOException | ClassNotFoundException e){
                    e.printStackTrace();
                }

                return message;
            }


            @Override
            public void run() {
                while (chatTime && (!muted) && playerIsAlive()){
                    message = getMessage();
                    if(message == null)
                        return;
                    msg = message.getText();
                    System.out.println(msg);

                    String [] msgToken = msg.split(" ",2);
                    if(msgToken.length > 1 && msgToken[1].equalsIgnoreCase("ready")){
                        isReady = true;
                        return;
                    }
                    if(msgToken.length > 1 && msgToken[1].equalsIgnoreCase("history")){
                        server.history(getPlayer());
                    }

                    if(msgToken.length > 1 && msgToken[1].equalsIgnoreCase("exit")){
                        server.removePlayer(getPlayer());
                    }

                    if(msgToken.length > 1  && (msgToken[1].equals("finish") || msgToken[1].equals("history")))
                        continue;
                    else
                        server.broadcast(msg);

                }
                setReady(true);
                return;

            }
        }

        chat = new ChatHandler();

        chat.start();
        waitPlayer();
    }

    private PlayerHandler getPlayer(){
        return  this;
    }

    boolean voteTime;
    private void vote(){
        voteTime = true;

        if(playerIsAlive()){
            sendMessage("vote time\nyou have 30 seconds\nfollowing players are alive\nwrite the name to vote");

            String alivePlayers = server.getList();
            sendMessage(alivePlayers);
        } else {
            sendMessage("vote time");
        }


        class VoteHandler extends Thread{

            private Message getMessage(){
                message = null;
                while (message == null){
                    try {
                        message = (Message) in.readObject();
                    }catch (SocketException s){
                        System.out.println("player disconnected");
                    } catch (IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    }
                }
                return message;
            }

            @Override
            public void run() {
                String target;
                while (voteTime && playerIsAlive()){
                    message = getMessage();
                    target = message.getText();
                    String[] targetTokens = target.split(" ",2);
                    if(targetTokens.length > 1)
                        target = targetTokens[1].trim();

                    if(target.equals("finish"))
                    { }

                    else if(server.acceptableVote(target, playerName)){
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
            Thread.sleep(2500);
        }catch (InterruptedException inter){
            inter.printStackTrace();
        }
        //after this result of voting should be sent to players

        msg = server.gatherVotes();
        sendMessage(msg);


        waitPlayer();

        msg = server.getVictim();
        sendMessage(msg +  " is the victim");

        if(!(playerRole instanceof Mayor)){
            sendMessage("wait for mayor");

        } else if(playerIsAlive()){
           msg = playerRole.action(out,in,server);
           server.mayorResponse(msg);

        } else{
            System.out.println("mayor has been killed");
            server.mayorResponse("yes");
        }
    }

    public String talkToVictim(){
        String response = "";
        sendMessage("do you want to watch the rest?[yes/no]");
        message = getMessage();
        response = message.getText();
        String[] responseTokens = response.split(" ",2);

        if(responseTokens[1] != null)
            response = responseTokens[1];
        else
            response = "no";

        return response;
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

    public void night(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        sendMessage("It is night");
        waitPlayer();

    }

    public String mafiaConsult(){
        String target = ((Mafia)playerRole).mafiaChat(out,in,server);
        return target;
    }

    public String actionCall(){
        return playerRole.action(out,in,server);
    }

    public void sendMessage(String msg) {
        try {
            if(msg != null)
            {
                Message message = new Message(msg);
                out.writeObject(message);
            }
        } catch (SocketException e){
            System.exit(0);
        } catch (IOException io){
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

    public void setAlive(boolean alive) {
        isAlive = alive;
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

    public boolean isHealed() {
        return healed;
    }

    public void setHealed(boolean healed) {
        this.healed = healed;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
