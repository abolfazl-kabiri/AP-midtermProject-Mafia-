import java.util.*;


public class GameManager extends Thread {

   private Vector<PlayerHandler> players;
   private Vector<PlayerHandler> mafias;
   private Vector<PlayerHandler> citizens;
   private int numberOfPlayers;
   private int numberOfMafias;
   private int numberOfCitizens;
   private ArrayList<Role> roles;
   private Server server;


    public GameManager(Vector<PlayerHandler> players, int numberOfPlayers, Server server) {
        this.players = players;
        this.server = server;
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

        sleepGame(2000);

        introductions();

        sleepGame(2000);

        chat();

        sleepGame(5000);

        votes();

       // night();
    }


    private boolean outOfVote = false;
    private void votes(){

        outOfVote = false;


        while (!allWaiting()) { }

        notifyPlayers();

        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                endVote();
            }
        };
        timer.schedule(timerTask,30 * 1000);


        while (!(allWaiting() && outOfVote))
        {}

        server.findVictim();
        sleepGame(2000);
        notifyPlayers();
    }

    private void endVote(){
        System.out.println("vote time finished");
        for (PlayerHandler player : players){
            player.voteTime = false;
            player.sendMessage("vote time is over");

        }

        while (!allWaiting())
        {}
        notifyPlayers();
        this.outOfVote = true;
    }

    boolean canStartNight = false;
    public void night(){
        System.out.println("wants to join");
        while (!canStartNight){}
        System.out.println("in night");
    }

    public void notifyPlayers(){
        for (PlayerHandler player : players)
        {
            synchronized (player){
                player.notify();
            }
        }
    }

    public void removePlayer(PlayerHandler player){
        players.remove(player);
    }

    private boolean allWaiting(){
        boolean waiting = true;
        for(PlayerHandler player : players){
            synchronized (player){
                if(!player.playerIsAlive())
                    continue;
                else if(player.getState() != State.WAITING){
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
            System.out.println(players.get(i).getPlayerName() + " --> " + roles.get(i));
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


    private boolean exitChat = false;
    private void chat(){

        while (!(allWaiting())){ }
        notifyPlayers();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                endChat();
            }
        };
        timer.schedule(timerTask,20*1000);

        while (!server.allReady()) {}

        if (server.allReady() && !exitChat){
            timer.cancel();
            endChat();
        }

    }

    private void endChat(){
        System.out.println("chat is over");

        for(PlayerHandler player : players){
            //kill chat thread of player threads
            player.chatTime = false;
            player.sendMessage("chat time over");
        }
        this.exitChat = true;
    }

    private void sleepGame( int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

