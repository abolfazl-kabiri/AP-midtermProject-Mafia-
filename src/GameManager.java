import java.util.*;


public class GameManager extends Thread {

   private Vector<PlayerHandler> players;
   private int numberOfPlayers;
   private int numberOfMafias;
   private int numberOfCitizens;
   private ArrayList<Role> roles;
   private Server server;
   private PlayerHandler mafiaTarget;
   private PlayerHandler healedMafia;


    public GameManager(Vector<PlayerHandler> players, int numberOfPlayers, Server server) {
        this.players = players;
        this.server = server;
        this.numberOfPlayers = numberOfPlayers;
        numberOfMafias = numberOfPlayers / 3;
        numberOfCitizens = numberOfPlayers - numberOfMafias;
        roles = new ArrayList<>();
        createRoles();
    }

    public void run(){
        if(allWaiting())
            setPlayersRoles();

        sleepGame(2000);

        introductions();

        sleepGame(2000);

        chat();

        sleepGame(5000);

        //here at first the hashMap of votes should be cleared
        votes();

        night();


    }



    boolean canStartNight = false;
    public void night(){
        while (!outOfVote)
        {}
        System.out.println("It is night");
        server.notifySinglePlayer(server.findByRole("Mayor"));
        sleepGame(10000);
        while (!allWaiting())
        {}

        notifyPlayers();
        sleepGame(10000);
        mafiaTime();



    }

    private void mafiaTime(){
        server.mafiaConsult();

        sleepGame(10000);
        if(server.checkAlive("Godfather")){
            String target = server.findByRole("Godfather").actionCall();
            System.out.println("godfather choice: " + target);
            this.mafiaTarget = server.findHandler(target);
            sleepGame(5000);
        } else {
            server.findVictim();
            String target = server.getVictim();
            this.mafiaTarget = server.findHandler(target);
            sleepGame(5000);
        }
        System.out.println(mafiaTarget.getPlayerName());

        if(server.checkAlive("DrLecter")){
            String healed = server.findByRole("DrLecter").actionCall();
            System.out.println("lecter choice: " + healed);
            this.healedMafia = server.findHandler(healed);
            sleepGame(5000);
        } else {
            System.out.println("lecter has been killed");
            this.healedMafia = null;
        }

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

    public boolean allWaiting(){
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
        System.out.println("It is day\n");

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                endChat();
            }
        };
        timer.schedule(timerTask,10*1000);

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
            player.sendMessage("chat time over");
            player.chatTime = false;
        }
        this.exitChat = true;
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


    private void sleepGame( int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

