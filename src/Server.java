import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;


public class Server {

    private int numberOfPlayers;
    private Vector<PlayerHandler> playerHandlers;
    private ArrayList<String> playerNames;
    private int port;
    private HashMap<String,String> votes;

    public Server() {
        playerHandlers = new Vector<>();
        playerNames = new ArrayList<>();
        port = 2022;
        votes = new HashMap<>();
    }

    Scanner scanner = new Scanner(System.in);
    public void setup() {

        acceptPlayers();

        while (!(allReady())){
            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }


        GameManager gameManager = new GameManager(playerHandlers,numberOfPlayers, this);
        gameManager.start();
    }

    private void acceptPlayers(){
        System.out.print("enter number of players: ");
        numberOfPlayers = scanner.nextInt();
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("waiting for clients");
            int index = 1;
            while (index <= numberOfPlayers)
            {
                Socket socket = serverSocket.accept();
                System.out.println("new player connected");
                PlayerHandler playerHandler = new PlayerHandler( this,socket);
                playerHandlers.add(playerHandler);
                playerHandler.start();
                index++;
            }
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    public void broadcast(String msg){
        for(PlayerHandler p: playerHandlers)
            p.sendMessage(msg);
    }

    public boolean allReady(){
        int counter = 0;
        boolean start = true;
        for(int i = 0; i< playerHandlers.size(); i++){
            if((playerHandlers.get(i).isReady()))
                counter++;
        }
        if(counter < numberOfPlayers)
            start = false;
        return start;
    }

    public boolean checkName(String name){
        boolean isValid = true;
        for(String n: playerNames){
            if(n.equalsIgnoreCase(name)){
                isValid = false;
                break;
            }
        }
        if(isValid)
            addNewName(name);
        return isValid;
    }

    public void addNewName(String name){
        playerNames.add(name);
    }

    public String getList(){
        String playerList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive())
                playerList += player.getPlayerName() + "\n";
        }
        return playerList;
    }


    //this method should be checked
    public boolean acceptableVote(String target, String playerName){

        if(target.equals(playerName))
            return false;

        for (PlayerHandler player : playerHandlers){
            if((target.equalsIgnoreCase(player.getPlayerName()) && player.playerIsAlive())){
                return true;
            }
        }
        return false;
    }

    public void storeVotes(String playerName, String target){
        boolean votedBefore = false;

        Iterator<Map.Entry<String, String>> iterator = votes.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            if(playerName.equals(entry.getKey())){
                votedBefore = true;
                break;
            }
        }

        if(votedBefore)
            votes.replace(playerName,target);
        else
            votes.put(playerName,target);
    }

    private int numberOfAlivePlayers(){
        int number = 0;
        for(PlayerHandler player : playerHandlers){
            if(player.playerIsAlive())
                number++;
        }
        return number;
    }

//    String votes = "";
//    public void gatherVotes(String target, PlayerHandler player){
//        int counter = 0;
//        votes += player.getPlayerName() + " --> " + target + "\n";
//        counter++;
//        if(counter == numberOfAlivePlayers()){
//            broadcast(votes);
//            votes = "";
//        }
//    }

    public static void main(String[] args) {
        Server server = new Server();
        server.setup();
    }
}
