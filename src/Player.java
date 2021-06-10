import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int port;
    private String name;
    private Role role;
    private boolean isAwake;

    Scanner scanner = new Scanner(System.in);

    public Player() {
        port = setPort();
    }



    public void start() {
        try {
            Socket socket = new Socket("127.0.0.1",port);
            System.out.println("connected to server");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());


            handleName();

            handleReady();

            handleRole();

            handleIntroduction();

            handleChat();

            //handleVote();


        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }



    Message message = null;
    String msg = "";

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

    private void sendMessage(String msg){
        try {
            out.writeObject(new Message(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleName(){
        while (true){
            message = getMessage();
            msg = message.getText();
            System.out.println(msg);

            String pName = null;
            while (pName == null || pName.length() < 1)
                pName = scanner.nextLine();
            sendMessage(pName);

            message = getMessage();
            msg = message.getText();
            System.out.println(msg);
            if(msg.startsWith("welcome"))
            {
                this.name = pName;
                break;
            }
        }
    }

    private void handleReady(){
        while (true){
            message = getMessage();
            msg = message.getText();
            System.out.println(msg);

            while (!(msg.equalsIgnoreCase("ready"))){
                msg = scanner.nextLine();
                sendMessage(msg);
            }

            message = getMessage();
            msg = message.getText();
            System.out.println(msg);
            if(msg.startsWith("ok wait"))
                break;
        }
    }

    private void handleRole() {
        Role r = null;
        while (r == null){
            try {
                r = (Role) in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        setRole(r);

        System.out.println("your role is " + role.toString() +"\n");

    }

    private void handleIntroduction(){
        if(role instanceof Mafia || role instanceof Mayor){
            message = getMessage();
            msg = message.getText();
            System.out.println(msg);
        }
    }

    private void handleChat() {

        this.isAwake = true;

        class Reader extends Thread{
            @Override
            public void run() {
                while (isAwake)
                {
                    message = getMessage();
                    String msg = message.getText();
                    System.out.println(msg);
                    if(msg.contains("chat time over"))
                        return;
                }
            }
        }


        class Writer extends Thread{
            @Override
            public void run() {
                while (true){
                    msg = scanner.nextLine();
                    if(msg.equalsIgnoreCase("ready"))
                    {
                        msg = name + ": " + msg;
                        sendMessage(msg);
                        return;
                    }else {
                        msg = name + ": " + msg;
                        sendMessage(msg);
                    }
                }
            }
        }

        Reader reader = new Reader();
        Writer writer = new Writer();

        reader.start();
        writer.start();

        while (reader.isAlive())
        {}
        if(writer.isAlive())
            writer.interrupt();

    }

    private void handleVote(){

        System.out.println("entered in vote");
        message = getMessage();
        msg = message.getText();
        System.out.println(msg);


        message = getMessage();
        msg = message.getText();
        System.out.println(msg);


        scanner.nextLine();
        System.out.println("enter your target");
        String target = scanner.nextLine();
        sendMessage(target);
        System.out.println("target sent");

        message = getMessage();
        while (message.getText().startsWith("unacceptable")){
            System.out.println(message.getText());
            target = scanner.next();
            sendMessage(target);

            message = getMessage();
        }

        if(message.getText().equals("accepted"))
            System.out.println(message.getText());
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private int setPort() {
        System.out.print("enter port: ");
        port = scanner.nextInt();
        //check statement





        return port;
    }

    private void sleepPlayer(int millis){
        try{
            Thread.sleep(millis);
        } catch (InterruptedException inter){
            inter.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Player player = new Player();
        player.start();
    }


}
