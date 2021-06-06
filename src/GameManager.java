import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class GameManager extends Thread {

   private Vector<PlayerHandler> players;
   private Vector<PlayerHandler> mafias;
   private Vector<PlayerHandler> citizens;
   private int numberOfPlayers;
   private int numberOfMafias;
   private int numberOfCitizens;
   private ArrayList<Role> roles;

    public GameManager(Vector<PlayerHandler> players, int numberOfPlayers) {
        this.players = players;
        mafias = new Vector<>();
        citizens = new Vector<>();
        this.numberOfPlayers = numberOfPlayers;
        numberOfMafias = numberOfPlayers / 3;
        numberOfCitizens = numberOfPlayers - numberOfMafias;
        roles = new ArrayList<>();
        createRoles();
        identifyCitizens();
        identifyMafias();
    }

    public void run(){
        if(allWaiting())
            setPlayersRoles();

        sleepGame(5000);

        introductions();

        System.out.println("done");
    }

    private void notifyPlayers(){
        for (PlayerHandler player : players)
        {
            synchronized (player){
                player.notify();
            }
        }
    }

    private boolean allWaiting(){
        boolean waiting = true;
        for(PlayerHandler player : players){
            synchronized (player){
                if(player.getState() != State.WAITING && player.getState() != State.TIMED_WAITING){
                    waiting = false;
                    break;
                }
            }
        }
        return waiting;
    }

    private void identifyMafias(){
        for(PlayerHandler player: players) {
            if (player.getPlayerRole() instanceof Mafia)
                mafias.add(player);
        }
    }

    private void identifyCitizens(){
        for(PlayerHandler player: players) {
            if (player.getPlayerRole() instanceof Citizen)
                citizens.add(player);
        }
    }

    private void createMafiaRoles(){
        Godfather godfather = new Godfather();
        roles.add(godfather);

        DrLector drLector = new DrLector();
        roles.add(drLector);

        for(int i = 2; i < numberOfMafias ; i++){
            SimpleMafia simpleMafia = new SimpleMafia();
            roles.add(simpleMafia);
        }
    }

    private void createCitizenRoles(){
        Bulletproof bulletproof = new Bulletproof();
        roles.add(bulletproof);

        Detective detective = new Detective();
        roles.add(detective);

        Doctor doctor = new Doctor();
        roles.add(doctor);

        Mayor mayor = new Mayor();
        roles.add(mayor);

        Psychologist psychologist = new Psychologist();
        roles.add(psychologist);

        Sniper sniper = new Sniper();
        roles.add(sniper);

        for(int i = 6; i < numberOfCitizens; i++){
            SimpleCitizen simpleCitizen = new SimpleCitizen();
            roles.add(simpleCitizen);
        }
    }

    private void createRoles(){
        createMafiaRoles();
        createCitizenRoles();
        Collections.shuffle(roles);
    }

    private void setPlayersRoles(){
        for(int i=0; i<numberOfPlayers; i++){
            players.get(i).setPlayerRole(roles.get(i));
        }
        notifyPlayers();
    }

    public void introductions(){

        while (!(allWaiting())){
            sleepGame(500);
        }
        if (allWaiting()){

            String msg = "";
            for(PlayerHandler player: players){
                if (player.getPlayerRole() instanceof Mafia)
                    msg += player.getPlayerName() + " is " + player.getPlayerRole().toString() + "\n";
            }


            for(PlayerHandler player: players) {
                if (player.getPlayerRole() instanceof Mafia)
                    player.setMsg(msg);
            }


            for(PlayerHandler player : players){
                if(player.getPlayerRole() instanceof Doctor){
                    msg = player.getPlayerName() + " is " + player.getPlayerRole() + "\n";
                    break;
                }
            }

            for (PlayerHandler player: players){
                if (player.getPlayerRole() instanceof Mayor){
                    player.setMsg(msg);
                    break;
                }
            }
            notifyPlayers();
        }
    }

    private void sleepGame( int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

