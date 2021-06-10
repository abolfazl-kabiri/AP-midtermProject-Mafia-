import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;


public class Server {

    private int numberOfPlayers;
    private Vector<PlayerHandler> playerHandlers;
    private ArrayList<String> playerNames;
    private int port;

    public Server() {
        playerHandlers = new Vector<>();
        playerNames = new ArrayList<>();
        port = 2022;
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

    public void broadcast(String msg, PlayerHandler playerHandler) {

        String[] msgParts = msg.split(":",2);
        if(msgParts[1].length() > 1){
            for(PlayerHandler p: playerHandlers)
            {
                if(playerHandler != p)
                    p.sendMessage(msg);
            }
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

    public boolean acceptableVote(String name){
        for (PlayerHandler player : playerHandlers){
            if((name.equalsIgnoreCase(player.getPlayerName()) && player.playerIsAlive()) || name.equalsIgnoreCase("none"))
                return true;
        }
        return false;
    }

    private int numberOfAlivePlayers(){
        int number = 0;
        for(PlayerHandler player : playerHandlers){
            if(player.playerIsAlive())
                number++;
        }
        return number;
    }

    String votes = "";
    public void gatherVotes(String target, PlayerHandler player){
        int counter = 0;
        votes += player.getPlayerName() + " --> " + target + "\n";
        counter++;
        if(counter == numberOfAlivePlayers()){
            broadcast(votes);
            votes = "";
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.setup();
    }
}
