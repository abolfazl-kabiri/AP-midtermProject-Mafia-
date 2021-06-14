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
    private ArrayList<Role> removedRoles;
    private GameManager gameManager;

    public Server() {
        playerHandlers = new Vector<>();
        playerNames = new ArrayList<>();
        port = 2022;
        votes = new HashMap<>();
        removedRoles = new ArrayList<>();
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


        gameManager = new GameManager(playerHandlers,numberOfPlayers, this);
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

    public String getCitizenList(){
        String citizenList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerRole() instanceof Citizen)
                citizenList += player.getPlayerName() + "\n";
        }
        return citizenList;
    }

    public String getMafiaList(){
        String mafiaList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerRole() instanceof Mafia)
                mafiaList += player.getPlayerName() + "\n";
        }
        return mafiaList;
    }

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

    public boolean acceptableMafiaConsult(String name){
        boolean accepted = false;
        if(getCitizenList().contains(name))
            accepted = true;
        return accepted;
    }

    public boolean acceptableMafiaHeal(String name){
        boolean accepted = false;
        if(getMafiaList().contains(name)){
            PlayerHandler player = findHandler(name);
            if(player.getPlayerRole() instanceof DrLector && player.isHealed())
                accepted = false;
            else{
                accepted = true;
                player.setHealed(true);
            }

        }
        else
            accepted = false;
        return accepted;
    }

    public boolean acceptableDoctorHeal(String name){
        boolean accepted = false;
        if(getList().contains(name)){
            PlayerHandler player = findHandler(name);
            if(player.getPlayerRole() instanceof Doctor && player.isHealed())
                accepted = false;
            else{
                accepted = true;
                player.setHealed(true);
            }
        }
        else
            accepted = false;
        return accepted;
    }

    public boolean acceptableCheckRole(String name){
        boolean accept = false;
        String detectiveName = findByRole("Detective").getPlayerName();
        if(getList().contains(name) && (!name.equals(detectiveName)))
            accept = true;
        return accept;
    }

    public boolean acceptableSniperShot(String name){
        boolean accept = false;
        String sniperName = findByRole("Sniper").getPlayerName();
        if((getList().contains(name) && (!name.equals(sniperName))) || (name.equalsIgnoreCase("no")) )
            accept = true;
        return accept;
    }

    public boolean acceptablePsychoChoice(String name){
        boolean accept = false;
        String psychoName = findByRole("Psychologist").getPlayerName();
        if((getList().contains(name) && (!name.equals(psychoName))) || (name.equalsIgnoreCase("no")) )
            accept = true;
        return accept;
    }

    public PlayerHandler sniperConclusion(String name){
        PlayerHandler player = findHandler(name);
        if(player.getPlayerRole() instanceof Mafia)
            return player;
        else
            return findByRole("Sniper");
    }

    public Role checkRole(String name){
        Role role = findHandler(name).getPlayerRole();
        return role;
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

    public String gatherVotes(){
        String voteResult = "";
        Iterator<Map.Entry<String, String>> iterator = votes.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            voteResult += entry.getKey() + " --> " + entry.getValue() + "\n";
        }
        return voteResult;
    }

    private String victim = "";
    public void findVictim(){
        int maximumRepeat = 0;
        ArrayList<String> victims = new ArrayList<>();
        HashMap<String, Integer> voteNumber = new HashMap<>();
        for(PlayerHandler player : playerHandlers){
            int counter = 0;
            Iterator<Map.Entry<String, String>> iterator = votes.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                if(entry.getValue().equalsIgnoreCase(player.getPlayerName()))
                    counter++;
            }
            if(counter > maximumRepeat)
                maximumRepeat = counter;
            voteNumber.put(player.getPlayerName(), counter);
        }

        for(Map.Entry<String, Integer> entry : voteNumber.entrySet()){
            if(entry.getValue() == maximumRepeat)
                victims.add(entry.getKey());
        }

        if(victims.size() == 1)
           this.victim = victims.get(0);
        else{
            Random random = new Random();
            int randomKill = random.nextInt(victims.size() - 1);
            this.victim = victims.get(randomKill);
        }

        votes.clear();
        victims.clear();
    }

    public String getVictim() {
        return victim;
    }

    public PlayerHandler findHandler(String name){
        PlayerHandler playerHandler = null;
        for (PlayerHandler player : playerHandlers){
            if(player.getPlayerName().equals(name)){
                playerHandler = player;
                break;
            }
        }
        return playerHandler;
    }

    public void removePlayer(PlayerHandler player){
        try {
            player.setAlive(false);
            gameManager.removePlayer(player);
            playerHandlers.remove(player);
            playerNames.remove(player.getPlayerName());
            removedRoles.add(player.getPlayerRole());
            player.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println(player.getPlayerName() + " removed");
        for(PlayerHandler playerHandler : playerHandlers){
            playerHandler.sendMessage(player.getPlayerName() + " removed");
        }
        player = null;
    }

    public void talkingToVictim(PlayerHandler player){
        String response = player.talkToVictim();
        if(response.equals("yes")){
            player.sendMessage("you are out but you can watch game");
            player.setAlive(false);
            removedRoles.add(player.getPlayerRole());


            System.out.println(victim + " removed");
            for (PlayerHandler playerHandler : playerHandlers){
                if(playerHandler.playerIsAlive())
                    playerHandler.sendMessage(victim + " removed");
            }
        }
        else
            removePlayer(player);
    }

    public void mayorResponse(String response){
        if(response.equals("yes")){
            PlayerHandler player = findHandler(victim);
            talkingToVictim(player);

        }else{
            System.out.println("voting canceled by mayor");
            //gameManager.notifyPlayers();
            for(PlayerHandler playerHandler : playerHandlers)
                playerHandler.sendMessage("voting canceled by mayor");
            victim = "";
        }
        gameManager.canStartNight = true;
    }

    public boolean checkAlive(String role){
        boolean alive = false;
        for(PlayerHandler player : playerHandlers){
            if(player.getPlayerRole().toString().equals(role)){
                alive = player.playerIsAlive();
                break;
            }
        }
        return alive;
    }

    public void mafiaConsult(){
        for(PlayerHandler player : playerHandlers){
            if((!(player.getPlayerRole() instanceof Godfather)) && (player.getPlayerRole() instanceof Mafia)){
                storeVotes(player.getPlayerName(), player.mafiaConsult());
            }
        }
    }

    public PlayerHandler findByRole(String role){
        PlayerHandler playerHandler = null;
        for(PlayerHandler player : playerHandlers){
            if(player.getPlayerRole().toString().equals(role) && player.playerIsAlive()){
                playerHandler = player;
                break;
            }
        }
        return playerHandler;
    }

    public ArrayList<Role> getRemovedRoles() {
        return removedRoles;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.setup();
    }
}
