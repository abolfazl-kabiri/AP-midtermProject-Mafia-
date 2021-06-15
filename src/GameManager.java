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
   private PlayerHandler healedByDoctor;
   private PlayerHandler sniperTarget;
   private PlayerHandler psychologistTarget;
   private boolean checkRemovedRoles;


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

        while (true){
            sleepGame(2000);

            chat();

            sleepGame(5000);

            votes();

            update();

            if(!continueGame())
                break;
            night();

            update();

            if (!continueGame())
                break;
        }

    }


    public boolean continueGame(){
        if(numberOfMafias >= numberOfCitizens){
            System.out.println("mafia is winner");
            for(PlayerHandler player : players)
                player.sendMessage("mafia is winner");
            return false;
        } else if(numberOfMafias == 0){
            System.out.println("city is winner");
            for(PlayerHandler player : players)
                player.sendMessage("city is winner");
            return false;
        } else
            return true;
    }

    private void updatePlayers(){
        int counter = 0;
        for(PlayerHandler player : players){
            if(player.playerIsAlive())
                counter++;
        }
        this.numberOfPlayers = counter;
    }

    private void updateMafias(){
        int counter = 0;
        for(PlayerHandler player : players){
            if(player.getPlayerRole() instanceof Mafia && player.playerIsAlive())
                counter++;
        }
        this.numberOfMafias = counter;
    }

    private void updateCitizens(){
        int counter = 0;
        for(PlayerHandler player : players){
            if(player.getPlayerRole() instanceof Citizen && player.playerIsAlive())
                counter++;
        }
        this.numberOfCitizens = counter;
    }

    private void update(){
        sleepGame(15000);
        updatePlayers();
        updateMafias();
        updateCitizens();
        System.out.println("\nnumber of mafia: " + numberOfMafias);
        System.out.println("number of citizens: " + numberOfCitizens);
    }

    boolean canStartNight = false;
    public void night(){
        while (!outOfVote)
        {}
        System.out.println("\nIt is night");
        sleepGame(10000);
        while (!allWaiting())
        {}

        notifyPlayers();
        sleepGame(10000);

        mafiaTime();

        citizenTime();

        nightConclusion();

        notifyPlayers();

        canStartNight = false;

    }

    private void mafiaTime(){
        server.mafiaConsult();

        sleepGame(6000);
        if(server.checkAlive("Godfather")){
            String target = server.findByRole("Godfather").actionCall();
            System.out.println("godfather choice: " + target);
            this.mafiaTarget = server.findHandler(target);
            sleepGame(5000);
        } else {
            System.out.println("Godfather has been killed");
            server.findVictim();
            String target = server.getVictim();
            this.mafiaTarget = server.findHandler(target);
            System.out.println(mafiaTarget.getPlayerName() + " chose by voting");
            sleepGame(5000);
        }

        if(server.checkAlive("DrLecter")){
            String healed = server.findByRole("DrLecter").actionCall();
            System.out.println("lecter choice: " + healed);
            this.healedMafia = server.findHandler(healed);
            sleepGame(5000);
        } else {
            System.out.println("lecter has been killed");
            this.healedMafia = null;
            sleepGame(5000);
        }

    }

    private void citizenTime(){

        doctorTime();

        detectiveTime();

        sniperTime();

        psychologistTime();

        bulletproofTime();
    }

    private void doctorTime(){
        if(server.checkAlive("Doctor")){
            String doctorHealed = server.findByRole("Doctor").actionCall();
            System.out.println("doctor healed : " + doctorHealed);
            this.healedByDoctor = server.findHandler(doctorHealed);
            sleepGame(5000);
        } else {
            System.out.println("doctor has been killed");
            this.healedByDoctor = null;
            sleepGame(5000);
        }
    }

    private void detectiveTime(){
        if(server.checkAlive("Detective")){
            String checkedRole = server.findByRole("Detective").actionCall();
            System.out.println("detective checked " + checkedRole);
            sleepGame(5000);
        } else {
            System.out.println("detective has been killed");
            sleepGame(5000);
        }

    }

    private void sniperTime(){
        if(server.checkAlive("Sniper")){
            String sniperChoice = server.findByRole("Sniper").actionCall();
            sleepGame(10 * 1000);
            if(!sniperChoice.equalsIgnoreCase("no")){
                System.out.println("sniper choice: "  + sniperChoice);
                this.sniperTarget = server.sniperConclusion(sniperChoice);
                System.out.println("sniper victim: " + sniperTarget.getPlayerName());
                sleepGame(5000);
            }else{
                System.out.println("sniper did not shot");
                this.sniperTarget = null;
                sleepGame(5000);
            }
        } else {
            System.out.println("sniper has been killed");
            this.sniperTarget = null;
            sleepGame(5000);
        }

    }

    private void psychologistTime(){
        if(server.checkAlive("Psychologist")){
            String psychoChoice = server.findByRole("Psychologist").actionCall();

            if(!psychoChoice.equalsIgnoreCase("no")){
                System.out.println("Psychologist choice: "  + psychoChoice);
                this.psychologistTarget = server.findHandler(psychoChoice);
                sleepGame(5000);
            } else {
                System.out.println("Psychologist did not mute");
                this.psychologistTarget = null;
                sleepGame(5000);
            }
        } else {
            System.out.println("Psychologist has been killed");
            this.psychologistTarget = null;
            sleepGame(5000);
        }
    }

    private void bulletproofTime(){
        if(server.checkAlive("Bulletproof")){
            String bulletChoice = server.findByRole("Bulletproof").actionCall();
            if(bulletChoice.equalsIgnoreCase("no")){
                System.out.println("Bulletproof didn't check");
                this.checkRemovedRoles = false;
                sleepGame(5000);
            } else {
                System.out.println("Bulletproof accepted");
                this.checkRemovedRoles = true;
                sleepGame(5000);
            }
        } else {
            this.checkRemovedRoles = false;
            sleepGame(5000);
        }
    }

    private void nightConclusion(){
        if(mafiaTarget != null && mafiaTarget != healedByDoctor){
            mafiaTarget.getPlayerRole().decreaseHealth();
            if(mafiaTarget.getPlayerRole().getHealth() == 0){
                server.talkingToVictim(mafiaTarget);
            }
        }
        if(sniperTarget != null  && sniperTarget != healedMafia && sniperTarget.playerIsAlive()){
            sniperTarget.getPlayerRole().decreaseHealth();
            if(sniperTarget.getPlayerRole().getHealth() == 0){
                server.talkingToVictim(sniperTarget);
            }
        }
        if(checkRemovedRoles){
            //removed roles should be sent to all
            ArrayList<Role> removedRoles = server.getRemovedRoles();
            Collections.shuffle(removedRoles);
            String roles = "";
            for (Role role : removedRoles)
                roles += role + "\n";
            System.out.println("\nkilled roles\n" + roles);
            for (PlayerHandler player : players)
                player.sendMessage("\nkilled roles\n" + roles);
            checkRemovedRoles = false;

        }

        if(psychologistTarget != null && psychologistTarget.playerIsAlive()) {
            psychologistTarget.setMuted(true);
            System.out.println(psychologistTarget.getPlayerName() + " is muted for next round");
            // a message should be sent to the player and others

            psychologistTarget.sendMessage("you are muted next round");
            for (PlayerHandler player : players){
                if(player != psychologistTarget)
                    player.sendMessage(psychologistTarget.getPlayerName() + " is muted for next round");
            }
        }
        server.getVotes().clear();
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
        synchronized (player){
            players.remove(player);
        }

    }

    public boolean allWaiting(){
        boolean waiting = true;
        synchronized (players){
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
        System.out.println();
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
        exitChat = false;
        System.out.println("It is day\n");

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                endChat();
            }
        };
        timer.schedule(timerTask,5*60*1000);

        while (!server.allReady()) {}

        if (server.allReady() && !exitChat){
            timer.cancel();
            endChat();
        }

    }

    private void endChat(){
        System.out.println("chat is over");

        for(PlayerHandler player : players){
            player.chatTime = false;
            player.sendMessage("chat time over");

        }

        while (!allWaiting())
        {}
        notifyPlayers();
        this.exitChat = true;
    }


    private boolean outOfVote = false;
    private void votes(){

        outOfVote = false;

        server.getVotes().clear();

        while (!allWaiting()) { }

        System.out.println("\nvoting");
        sleepGame(3000);
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

