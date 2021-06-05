import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;


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

        GameManager gameManager = new GameManager(playerHandlers,numberOfPlayers);
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
        for(PlayerHandler p: playerHandlers)
        {
            if(playerHandler != p)
                p.sendMessage(msg);
        }
    }

    public boolean canStartGame(){
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


    public static void main(String[] args) {
        Server server = new Server();
        server.setup();
    }
}
