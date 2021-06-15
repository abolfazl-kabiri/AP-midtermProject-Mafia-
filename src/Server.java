import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;


/**
 * The type Server.
 */
public class Server {

    private int numberOfPlayers;
    private Vector<PlayerHandler> playerHandlers;
    private ArrayList<String> playerNames;
    private int port;
    private HashMap<String,String> votes;
    private ArrayList<Role> removedRoles;
    private GameManager gameManager;
    private File file;


    /**
     * Instantiates a new Server.
     */
    public Server() {
        playerHandlers = new Vector<>();
        playerNames = new ArrayList<>();
        port = 2022;
        votes = new HashMap<>();
        removedRoles = new ArrayList<>();
        file = new File("chat.txt");
    }

    /**
     * The Scanner.
     */
    Scanner scanner = new Scanner(System.in);

    /**
     * Sets .
     */
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
        System.out.print("enter number of players (at least 8 players): ");
        numberOfPlayers = scanner.nextInt();
        while (numberOfPlayers < 8)
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

    /**
     * Broadcast.
     *
     * @param msg the msg
     */
    public void broadcast(String msg){
            try (FileWriter fileWriter = new FileWriter(file,true)) {
              fileWriter.write(msg+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        for(PlayerHandler p: playerHandlers)
            p.sendMessage(msg);
    }

    /**
     * History.
     *
     * @param player the player
     */
    public void history(PlayerHandler player){
        player.sendMessage("\nhistory");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String msg;
            while ((msg = bufferedReader.readLine()) != null){
                player.sendMessage(msg);
            }
            fileReader.close();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * All ready boolean.
     *
     * @return the boolean
     */
    public boolean allReady(){

        synchronized (playerHandlers){
            for(PlayerHandler player : playerHandlers){
                if(player.playerIsAlive() && !(player.isReady()))
                    return false;
            }
            return true;
        }
    }

    /**
     * Check name boolean.
     *
     * @param name the name
     * @return the boolean
     */
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

    /**
     * Add new name.
     *
     * @param name the name
     */
    public void addNewName(String name){
        playerNames.add(name);
    }

    /**
     * Get list string.
     *
     * @return the string
     */
    public String getList(){
        String playerList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive())
                playerList += player.getPlayerName() + "\n";
        }
        return playerList;
    }

    /**
     * Get citizen list string.
     *
     * @return the string
     */
    public String getCitizenList(){
        String citizenList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerRole() instanceof Citizen)
                citizenList += player.getPlayerName() + "\n";
        }
        return citizenList;
    }

    /**
     * Get mafia list string.
     *
     * @return the string
     */
    public String getMafiaList(){
        String mafiaList = "";
        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerRole() instanceof Mafia)
                mafiaList += player.getPlayerName() + "\n";
        }
        return mafiaList;
    }

    /**
     * Acceptable vote boolean.
     *
     * @param target     the target
     * @param playerName the player name
     * @return the boolean
     */
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

    /**
     * Acceptable mafia consult boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptableMafiaConsult(String name){
        boolean accepted = false;
        for(PlayerHandler player : playerHandlers){
            if(player.getPlayerName().equalsIgnoreCase(name) && player.playerIsAlive()){
                accepted = true;
                break;
            }
        }
        return accepted;
    }

    /**
     * Acceptable mafia heal boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptableMafiaHeal(String name){
        boolean accepted = false;

        for(PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerName().equals(name)){
                if(player.getPlayerRole() instanceof DrLector && player.isHealed())
                    accepted = false;
                else{
                    accepted = true;
                    player.setHealed(true);
                }
                break;
            }
        }

        return accepted;
    }

    /**
     * Acceptable doctor heal boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptableDoctorHeal(String name){
        boolean accepted = false;

        for (PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerName().equals(name)){
                if(player.getPlayerRole() instanceof Doctor && player.isHealed())
                    accepted = false;
                else{
                    accepted = true;
                    player.setHealed(true);
                }
                break;
            }
        }

        return accepted;
    }

    /**
     * Acceptable check role boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptableCheckRole(String name){
        boolean accept = false;
        String detectiveName = findByRole("Detective").getPlayerName();

        for(PlayerHandler player : playerHandlers){
            if(player.playerIsAlive() && player.getPlayerName().equals(name) && !name.equals(detectiveName)){
                accept = true;
                break;
            }
        }

        return accept;
    }

    /**
     * Acceptable sniper shot boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptableSniperShot(String name){
        boolean accept = false;
        String sniperName = findByRole("Sniper").getPlayerName();

        if(name.equalsIgnoreCase("no"))
            accept = true;
        else {
            for(PlayerHandler player : playerHandlers){
                if(player.playerIsAlive() && player.getPlayerName().equals(name) && !name.equals(sniperName)){
                    accept = true;
                    break;
                }
            }
        }

        return accept;
    }

    /**
     * Acceptable psycho choice boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean acceptablePsychoChoice(String name){
        boolean accept = false;
        String psychoName = findByRole("Psychologist").getPlayerName();

        if(name.equalsIgnoreCase("no"))
            accept = true;
        else{
            for(PlayerHandler player : playerHandlers){
                if(player.playerIsAlive() && player.getPlayerName().equals(name) && !name.equals(psychoName)){
                    accept = true;
                    break;
                }
            }
        }

        return accept;
    }

    /**
     * Sniper conclusion player handler.
     *
     * @param name the name
     * @return the player handler
     */
    public PlayerHandler sniperConclusion(String name){
        PlayerHandler player = findHandler(name);
        if(player.getPlayerRole() instanceof Mafia)
            return player;
        else
            return findByRole("Sniper");
    }

    /**
     * Check role role.
     *
     * @param name the name
     * @return the role
     */
    public Role checkRole(String name){
        Role role = findHandler(name).getPlayerRole();
        return role;
    }

    /**
     * Store votes.
     *
     * @param playerName the player name
     * @param target     the target
     */
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

    /**
     * Gather votes string.
     *
     * @return the string
     */
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

    /**
     * Find victim.
     */
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

        voteNumber.clear();
        votes.clear();
        victims.clear();
    }

    /**
     * Gets victim.
     *
     * @return the victim
     */
    public String getVictim() {
        return victim;
    }

    /**
     * Find handler player handler.
     *
     * @param name the name
     * @return the player handler
     */
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

    /**
     * Remove player.
     *
     * @param player the player
     */
    public void removePlayer(PlayerHandler player){
        try {
            player.setReady(true);
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
    }

    /**
     * Talking to victim.
     *
     * @param player the player
     */
    public void talkingToVictim(PlayerHandler player){

        String response = player.talkToVictim();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        if(response.equals("yes")){
            player.sendMessage("you are out but you can watch game");
            player.setAlive(false);
            removedRoles.add(player.getPlayerRole());


            System.out.println(player.getPlayerName() + " removed");
            for (PlayerHandler playerHandler : playerHandlers){
                if(playerHandler.playerIsAlive())
                    playerHandler.sendMessage(player.getPlayerName() + " removed");
            }
        }
        else
            removePlayer(player);

    }

    /**
     * Mayor response.
     *
     * @param response the response
     */
    public void mayorResponse(String response){

        if(response.equals("yes")){
            PlayerHandler player = findHandler(victim);
            talkingToVictim(player);
            victim = null;

        }else{
            System.out.println("voting canceled by mayor");
            //gameManager.notifyPlayers();
            for(PlayerHandler playerHandler : playerHandlers)
                playerHandler.sendMessage("voting canceled by mayor");
            victim = null;
        }


        gameManager.canStartNight = true;
    }

    /**
     * Check alive boolean.
     *
     * @param role the role
     * @return the boolean
     */
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

    /**
     * Mafia consult.
     */
    public void mafiaConsult(){
        for(PlayerHandler player : playerHandlers){
            if((!(player.getPlayerRole() instanceof Godfather)) && (player.getPlayerRole() instanceof Mafia) && player.playerIsAlive()){
                storeVotes(player.getPlayerName(), player.mafiaConsult());
            }
        }
    }

    /**
     * Find by role player handler.
     *
     * @param role the role
     * @return the player handler
     */
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

    /**
     * Gets removed roles.
     *
     * @return the removed roles
     */
    public ArrayList<Role> getRemovedRoles() {
        return removedRoles;
    }

    /**
     * Game continues boolean.
     *
     * @return the boolean
     */
    public boolean gameContinues(){
        return gameManager.continueGame();
    }

    /**
     * Gets votes.
     *
     * @return the votes
     */
    public HashMap<String, String> getVotes() {
        return votes;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.setup();
    }
}
