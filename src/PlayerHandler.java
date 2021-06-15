import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * The type Player handler.
 */
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


    /**
     * Instantiates a new Player handler.
     *
     * @param server the server
     * @param socket the socket
     */
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

    /**
     * The Msg.
     */
    String msg = "";
    /**
     * The Message.
     */
    Message message = null;

    public void run() {
        setup();

        waitPlayer();

        handleRole();

        handleIntroduction();



        while (true){

            waitPlayer(); //1

            handleChatTime();

            waitPlayer();//3

            unmute();

            vote();

            if (!server.gameContinues())
                break;

            waitPlayer(); //6

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

    /**
     * Gets socket.
     *
     * @return the socket
     */
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

    /**
     * Gets player name.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Set msg.
     *
     * @param msg the msg
     */
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


    /**
     * The Chat time.
     */
    boolean chatTime;

    /**
     * Handle chat time.
     */
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
        waitPlayer(); //2
    }

    private PlayerHandler getPlayer(){
        return  this;
    }

    /**
     * The Vote time.
     */
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

        waitPlayer(); //4
        try {
            Thread.sleep(5000);
        }catch (InterruptedException inter){
            inter.printStackTrace();
        }
        //after this result of voting should be sent to players

        msg = server.gatherVotes();
        sendMessage(msg);


        waitPlayer(); //5

        msg = server.getVictim();
        sendMessage(msg +  " is the victim");


        if(playerRole instanceof Mayor && !playerIsAlive()){
            System.out.println("mayor has been killed");
            server.mayorResponse("yes");
        }

        if(playerRole instanceof Mayor && playerIsAlive()){
            msg = playerRole.action(out,in,server);
            server.mayorResponse(msg);
        }

        else
            sendMessage("wait for mayor");


    }

    /**
     * Talk to victim string.
     *
     * @return the string
     */
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

    /**
     * Wait player.
     */
    public void waitPlayer(){
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    /**
     * Night.
     */
    public void night(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        sendMessage("It is night");
      //  waitPlayer(); //7

    }

    /**
     * Mafia consult string.
     *
     * @return the string
     */
    public String mafiaConsult(){
        String target = ((Mafia)playerRole).mafiaChat(out,in,server);
        return target;
    }

    /**
     * Action call string.
     *
     * @return the string
     */
    public String actionCall(){
        return playerRole.action(out,in,server);
    }

    /**
     * Send message.
     *
     * @param msg the msg
     */
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

    /**
     * Send message.
     *
     * @param role the role
     */
    public void sendMessage(Role role){
        try{
            if(role != null)
                out.writeObject(role);
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    /**
     * Player is alive boolean.
     *
     * @return the boolean
     */
    public boolean playerIsAlive(){
        return isAlive;
    }

    /**
     * Sets alive.
     *
     * @param alive the alive
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    /**
     * Is ready boolean.
     *
     * @return the boolean
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Set ready.
     *
     * @param ready the ready
     */
    public void setReady(boolean ready){
        this.isReady = ready;
    }

    /**
     * Sets player name.
     *
     * @param playerName the player name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Sets player role.
     *
     * @param playerRole the player role
     */
    public void setPlayerRole(Role playerRole) {
        this.playerRole = playerRole;
    }

    /**
     * Gets player role.
     *
     * @return the player role
     */
    public Role getPlayerRole() {
        return playerRole;
    }

    /**
     * Is healed boolean.
     *
     * @return the boolean
     */
    public boolean isHealed() {
        return healed;
    }

    /**
     * Sets healed.
     *
     * @param healed the healed
     */
    public void setHealed(boolean healed) {
        this.healed = healed;
    }

    /**
     * Sets muted.
     *
     * @param muted the muted
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
