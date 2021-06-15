import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Player {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int port;
    private String name;
    private Role role;
    Reader reader;
    Writer writer;

    Scanner scanner = new Scanner(System.in);

    public Player() {
        port = setPort();
    }



    public void start() {
        try {
            socket = new Socket("127.0.0.1",port);
            System.out.println("connected to server");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());


            handleName();

            handleReady();

            handleRole();

            handleIntroduction();

            reader = new Reader();
            writer = new Writer();
            startThreads();

        } catch (ConnectException c){
            System.out.println("can not connect run the program again");
        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }



    Message message = null;
    String msg = "";

    class Reader extends Thread{
        @Override
        public void run() {
            while (true)
            {
                message = getMessage();
                String msg = message.getText();
                System.out.println(msg);
                if(msg.equalsIgnoreCase("chat time over")){
                    sendMessage("finish");
                }

                if(msg.equals("vote time is over"))
                    sendMessage("finish");

                if(msg.startsWith("you are out")){
                    writer.interrupt();
                }
                if(msg.startsWith("mafia") || msg.startsWith("city"))
                    System.exit(0);
            }
        }
    }

    class Writer extends Thread{
        @Override
        public void run() {
            while (!(writer.isInterrupted())){
                msg = scanner.nextLine();
                msg = name + ": " + msg;
                sendMessage(msg);
            }
        }
    }

    private Message getMessage(){
        message = null;
        while (message == null){
            try {
                message = (Message) in.readObject();
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            } catch (EOFException e){
                System.out.println("connection lost");
                try {
                    socket.close();
                    System.exit(0);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException io){
                io.printStackTrace();
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
                pName = scanner.next();
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

    private void startThreads(){
        reader.start();
        writer.start();
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private int setPort() {
        System.out.print("enter port: ");
        try {
            port = scanner.nextInt();
        } catch (InputMismatchException input){
            System.out.println("you should enter port");
            System.out.println("run the program again");
            System.exit(0);
        }


        return port;
    }

    public static void main(String[] args) {
        Player player = new Player();
        player.start();
    }


}
