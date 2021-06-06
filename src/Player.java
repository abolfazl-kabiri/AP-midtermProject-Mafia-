import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player {


    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int port;
    private String name;
    private boolean isReady;
    private Role role;

    Scanner scanner = new Scanner(System.in);

    public Player() {
        port = setPort();
        isReady = false;
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

//            try {
//                wait();
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }

//            new ReadThread(in,this).start();
//            new WriteThread(out,this).start();

        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }


    Message message = null;
    String msg = "";
    private void handleName(){
        try{
            while (true){
                message = (Message) in.readObject();
                while (message == null)
                    message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);

                String pName = null;
                while (pName == null || pName.length() < 1)
                    pName = scanner.nextLine();
                out.writeObject(new Message(pName));

                message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);
                if(msg.startsWith("welcome"))
                {
                    this.name = pName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleReady(){

        try{
            while (true){
                message = (Message) in.readObject();
                while (message == null)
                    message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);

                msg = scanner.nextLine();
                out.writeObject(new Message(msg));

                message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);
                if(msg.startsWith("ok wait"))
                {
                    this.isReady = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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

        try {
            message = (Message) in.readObject();
            msg = message.getText();
            System.out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleIntroduction(){
        if(role instanceof Mafia || role instanceof Mayor){
            message = null;
            while (message == null){
                try {
                    message = (Message) in.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            } msg = message.getText();
            System.out.println(msg);
        }
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

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        Player player = new Player();
        player.start();
    }
}
